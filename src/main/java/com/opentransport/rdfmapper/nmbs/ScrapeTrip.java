/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import com.opentransport.rdfmapper.nmbs.containers.GtfsRealtime;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author timtijssens
 */
public class ScrapeTrip {

    private String url; //"http://api.irail.be/vehicle/?id=BE.NMBS.IC511&format=json";
    private GtfsRealtime.FeedMessage.Builder feedMessage = GtfsRealtime.FeedMessage.newBuilder();
    private GtfsRealtime.FeedHeader.Builder feedHeader = GtfsRealtime.FeedHeader.newBuilder();
    private ErrorLogWriter errorWriter = new ErrorLogWriter();
    //private RoutesReader rr;
    private TripReader tr;
    private CalendarDateReader cdr;
            
    private int NUMBER_OF_CONNECTIONS_TO_IRAIL_API = 10;
    
    public ScrapeTrip() {
        feedHeader.setGtfsRealtimeVersion("1.0");
        feedHeader.setIncrementality(GtfsRealtime.FeedHeader.Incrementality.FULL_DATASET);
        //Unix Style
        feedHeader.setTimestamp(System.currentTimeMillis() / 1000L);
        feedMessage.setHeader(feedHeader);
    }
    
    void startScrape(Map trainDelays) {
        String trainName;
        Iterator iterator = trainDelays.entrySet().iterator();
        int i = 0;
        boolean cancelled = false;
        
        // Download vehicleinformation from iRail API
        requestJsons(trainDelays);

        while (iterator.hasNext()) {
            Map.Entry mapEntry = (Map.Entry) iterator.next();

            trainName = returnCorrectTrainFormat((String) mapEntry.getKey());
            
            // All entries say that the route is cancelled. This is not correct
            //String delay = (String) mapEntry.getValue();
            //cancelled = delay.equals("Afgeschaft");

            //Parse the Json and add it to the Feed 
            File f = new File("./delays/" + trainName + ".json");
            if (f.exists() && !f.isDirectory()) {
                GtfsRealtime.FeedEntity.Builder feedEntity = parseJson(i, "./delays/" + trainName + ".json", cancelled, trainName);
                feedMessage.addEntity(i, feedEntity);
                System.out.println(trainName + " has been processed");
                i++;
            } else {
                errorWriter.writeError("File Not Found " + "./delays/" + trainName + ".json");
            }
        }
        
        //Write File  
        try {
            FileOutputStream output = new FileOutputStream("trip_updates.pb");
            feedMessage.build().writeTo(output);
            output.close();
            System.out.println("GTFS RT Tripupdate file written successful");
        } catch (Exception e) {
            System.out.println(e);
            errorWriter.writeError(e.toString());
        }
    }
    
    //public void setRoutesReader(RoutesReader rr) {
    //    this.rr = rr;
    //}
    
    public void setTripReader(TripReader tr) {
        this.tr = tr;
    }
    
    public void setCalendarDateReader(CalendarDateReader cdr) {
        this.cdr = cdr;
    }
    
    private void requestJsons(Map trainDelays) {
        String trainName;
        Iterator iterator = trainDelays.entrySet().iterator();

        ExecutorService pool = Executors.newFixedThreadPool(NUMBER_OF_CONNECTIONS_TO_IRAIL_API);
        while (iterator.hasNext()) {
            Map.Entry mapEntry = (Map.Entry) iterator.next();
            trainName = returnCorrectTrainFormat((String) mapEntry.getKey());
            url = "http://api.irail.be/vehicle/?id=BE.NMBS." + trainName + "&format=json";
            System.out.println("Requesting: " + url);
            pool.submit(new DownloadDelayedTrains(trainName, url));
        }
        pool.shutdown();
        
        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            // all tasks have now finished (unless an exception is thrown abo
        } catch (InterruptedException ex) {
            Logger.getLogger(ScrapeTrip.class.getName()).log(Level.SEVERE, null, ex);
            errorWriter.writeError(ex.toString());
        }
    }
    
    private GtfsRealtime.FeedEntity.Builder parseJson(int identifier, String fileName, boolean cancelled, String trainName) {
        GtfsRealtime.FeedEntity.Builder feedEntity = GtfsRealtime.FeedEntity.newBuilder();
        feedEntity.setId(Integer.toString(identifier));
        feedEntity.setIsDeleted(false);

        //Data that doesnt Update
        GtfsRealtime.TripUpdate.Builder tripUpdate = GtfsRealtime.TripUpdate.newBuilder();
        GtfsRealtime.VehicleDescriptor.Builder vehicleDescription = GtfsRealtime.VehicleDescriptor.newBuilder();
        GtfsRealtime.TripDescriptor.Builder tripDescription = GtfsRealtime.TripDescriptor.newBuilder();
        GtfsRealtime.TripUpdate.StopTimeUpdate.Builder stopTimeUpdate = GtfsRealtime.TripUpdate.StopTimeUpdate.newBuilder();

        //Each StopTime Update contains StopTimeEvents witht the stop Arrival and Departure Time 
        GtfsRealtime.TripUpdate.StopTimeEvent.Builder stopTimeArrival = GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder();
        GtfsRealtime.TripUpdate.StopTimeEvent.Builder stopTimeDeparture = GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder();

        JSONParser parser = new JSONParser();
        try {
            FileReader fr = new FileReader(fileName);
            JSONObject json = (JSONObject) parser.parse(fr);
            String trainId = (String) json.get("vehicle");
            //Setting the VehicleData
            String routeId = "routes:" + trainName;
            vehicleDescription.setId(routeId);
            // label is set after processing stops, because routes.txt can be incomplete
            //vehicleDescription.setLabel(rr.getRouteLongName("routes:" + trainName));
            vehicleDescription.setLicensePlate(trainId);

            //Handling Departure Date
            String unixSeconds = (String) json.get("timestamp");
            Long unixSec = Long.parseLong(unixSeconds);

            Date date = new Date(unixSec * 1000L); // *1000 is to convert seconds to milliseconds
            SimpleDateFormat sdfStartDate = new SimpleDateFormat("yyyyMMdd"); // the format of your date
            sdfStartDate.setTimeZone(TimeZone.getTimeZone("GMT+2")); // give a timezone reference for formating
            SimpleDateFormat sdfStartTimeHour = new SimpleDateFormat("HH:mm:ss");

            String formattedDepartureDate = sdfStartDate.format(date);
            String formattedDepartureHour = sdfStartTimeHour.format(date);

            // Setting the Trip Description
            tripDescription.setStartTime(formattedDepartureHour);
            //YYYYMMDD format
            tripDescription.setStartDate(formattedDepartureDate);
            tripDescription.setRouteId(routeId);

            String tripId = tr.getTripIdFromRouteId(routeId, cdr);
            System.out.println("Trip id set: " + tripId);
            
            if (cancelled) {
                tripDescription.setScheduleRelationship(GtfsRealtime.TripDescriptor.ScheduleRelationship.CANCELED);
            }

            //Get Information about stops
            JSONObject rootStop = (JSONObject) json.get("stops");
            JSONArray stops = (JSONArray) rootStop.get("stop");
            String delay = "0";
            int maxDelay = 0;
            
            String firstStopName = "";
            String lastStopName = "";
                    
            for (int i = 0; i < stops.size(); i++) {
                //Information about the stops
                JSONObject stop = (JSONObject) stops.get(i);
                // String stopSeq = (String) stop.get("id");

                stopTimeUpdate.setStopSequence(i);
                try {

                    JSONObject station = (JSONObject) stop.get("stationinfo");
                    // tripDescription.setRouteId((String) station.get("@id"));

                    String stopId = (String) station.get("id");
                    stopId = "stops:" + stopId.replaceFirst("[^0-9]+", "") + ":0";
                    //TODO: if stopid is empty than get from gtfs
                    stopTimeUpdate.setStopId(stopId);
                    
                    // Constructing route long name from first and last stop
                    if (i == 0) {
                        firstStopName = (String) station.get("standardname");
                    } else if (i == stops.size() - 1) {
                        lastStopName = (String) station.get("standardname");
                    }

                } catch (Exception e) {
                    errorWriter.writeError(e.toString() + fileName);
                    System.out.println(fileName);
                    System.out.println(e);
                }

                // arrival time delay
                delay = (String) stop.get("delay");
                System.out.println("Delay" + delay);
                int delayInt = Integer.parseInt(delay);
                if (maxDelay < delayInt) {
                    maxDelay = delayInt;
                }
                
                stopTimeArrival.setDelay(delayInt);
                String arrivalTime = (String) stop.get("time");
                stopTimeArrival.setTime(Long.parseLong(arrivalTime));
                stopTimeUpdate.setArrival(stopTimeArrival);
                // iRail API doesn't return departuretimes
                //stopTimeDeparture.setDelay(Integer.parseInt(delay));
                //stopTimeUpdate.setDeparture(stopTimeDeparture);
                tripUpdate.addStopTimeUpdate(stopTimeUpdate);
            }
            
            String route_long_name = firstStopName + " - " + lastStopName;
            vehicleDescription.setLabel(route_long_name);
            tripUpdate.setTrip(tripDescription);
            tripUpdate.setVehicle(vehicleDescription);
            tripUpdate.setDelay(maxDelay);
            feedEntity.setTripUpdate(tripUpdate);

            fr.close();
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
            errorWriter.writeError(ex.toString());

        } catch (IOException ex) {
            System.out.println("IO exception" + ex);

        } catch (ParseException ex) {
            System.out.println("Parse exception " + ex + " " + fileName);
        } catch (NullPointerException npe) {
            System.out.println(npe.toString());
        }
        
        return feedEntity;
    }

    private String returnCorrectTrainFormat(String trainName) {
        String[] splitted = trainName.split("\\s+");
        if (splitted.length > 1) {
            trainName = "";
        }
        
        for (int i = 0; i <= splitted.length - 1; i++) {
            trainName += splitted[i];
        }
        
        trainName = checkTrainDouble(trainName);
        return trainName;
    }

    //This function checks if a train name occurs twice in the same string
    private String checkTrainDouble(String trainName) {
        try {
            String trainTypeIdentifier = trainName.substring(0, 3);
            if (trainName.lastIndexOf(trainTypeIdentifier) >= 4) {
                trainName = trainName.substring(0, trainName.lastIndexOf(trainTypeIdentifier));
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        
        return trainName;
    }
}
