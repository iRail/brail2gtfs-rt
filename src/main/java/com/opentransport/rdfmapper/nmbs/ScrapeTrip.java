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
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.time.DateUtils;
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
    private int i = 0;
            
    private int NUMBER_OF_CONNECTIONS_TO_IRAIL_API = 5;
    
    public static int countConnections = 0;
    
    public ScrapeTrip() {
        feedHeader.setGtfsRealtimeVersion("1.0");
        feedHeader.setIncrementality(GtfsRealtime.FeedHeader.Incrementality.FULL_DATASET);
        //Unix Style
        feedHeader.setTimestamp(System.currentTimeMillis() / 1000L);
        feedMessage.setHeader(feedHeader);
    }
    
    void startScrape(Map trainDelays, boolean canceled) {
        String trainName;
        Iterator iterator = trainDelays.entrySet().iterator();
        
        // Download vehicleinformation from iRail API
        requestJsons(trainDelays);

        while (iterator.hasNext()) {
            Map.Entry mapEntry = (Map.Entry) iterator.next();

            trainName = returnCorrectTrainFormat((String) mapEntry.getKey());
            
            //Parse the Json and add it to the Feed 
            File f = new File("./delays/" + trainName + ".json");
            if (f.exists() && !f.isDirectory()) {
                GtfsRealtime.FeedEntity.Builder feedEntity = parseJson(i, "./delays/" + trainName + ".json", canceled, trainName);
                feedMessage.addEntity(i, feedEntity);
                System.out.println(trainName + " has been processed");
                i++;
            } else {
                errorWriter.writeError("File Not Found " + "./delays/" + trainName + ".json");
            }
        }
    }
    
    public void writeToFile() {
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
            url = "https://api.irail.be/vehicle/?id=BE.NMBS." + trainName + "&format=json";
            System.out.println("HTTP GET - " + url);
            countConnections++;
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
    
    private GtfsRealtime.FeedEntity.Builder parseJson(int identifier, String fileName, boolean canceled, String trainName) {
        GtfsRealtime.FeedEntity.Builder feedEntity = GtfsRealtime.FeedEntity.newBuilder();
        feedEntity.setId(Integer.toString(identifier));
        feedEntity.setIsDeleted(false);

        //Data that doesnt Update
        GtfsRealtime.TripUpdate.Builder tripUpdate = GtfsRealtime.TripUpdate.newBuilder();
        GtfsRealtime.VehicleDescriptor.Builder vehicleDescription = GtfsRealtime.VehicleDescriptor.newBuilder();
        GtfsRealtime.TripDescriptor.Builder tripDescription = GtfsRealtime.TripDescriptor.newBuilder();
        GtfsRealtime.TripUpdate.StopTimeUpdate.Builder stopTimeUpdate = GtfsRealtime.TripUpdate.StopTimeUpdate.newBuilder();

        //Each StopTime Update contains StopTimeEvents with the stop Arrival and Departure Time 
        GtfsRealtime.TripUpdate.StopTimeEvent.Builder stopTimeArrival = GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder();
        GtfsRealtime.TripUpdate.StopTimeEvent.Builder stopTimeDeparture = GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder();

        JSONParser parser = new JSONParser();
        try {
            FileReader fr = new FileReader(fileName);
            JSONObject json = (JSONObject) parser.parse(fr);
            String trainId = (String) json.get("vehicle");
            //Setting the VehicleData
            String routeId = trainName;
            vehicleDescription.setId(routeId);
            vehicleDescription.setLicensePlate(trainId);

            //Handling Departure Date
            String unixSeconds = (String) json.get("timestamp");
            Long unixSec = Long.parseLong(unixSeconds);

            Date date = new Date(unixSec * 1000L); // *1000 is to convert seconds to milliseconds
            SimpleDateFormat sdfStartDate = new SimpleDateFormat("yyyyMMdd"); // the format of your date
            sdfStartDate.setTimeZone(TimeZone.getTimeZone("GMT+2")); // give a timezone reference for formating
            SimpleDateFormat sdfStartTimeHour = new SimpleDateFormat("HH:mm:ss");

            String formattedDepartureDate = sdfStartDate.format(date);
            // String formattedDepartureHour = sdfStartTimeHour.format(date);

            // Setting the Trip Description
            // tripDescription.setStartTime(formattedDepartureHour);
            //YYYYMMDD format
            tripDescription.setStartDate(formattedDepartureDate);
            tripDescription.setRouteId(routeId);

            String tripId = tr.getTripIdFromRouteId(routeId, cdr);
            tripDescription.setTripId(tripId);

            //Get Information about stops
            JSONObject rootStop = (JSONObject) json.get("stops");
            JSONArray stops = (JSONArray) rootStop.get("stop");
            String arrivalDelay = "0";
            String departureDelay = "0";
            int maxDelay = 0;
            
            String firstStopName = "";
            String lastStopName = "";
                    
            boolean wholeTripCanceled = true; // True when all stoptimes since now are canceled
            
            for (int i = 0; i < stops.size(); i++) {
                //Information about the stops
                JSONObject stop = (JSONObject) stops.get(i);
                // String stopSeq = (String) stop.get("id");

                stopTimeUpdate.setStopSequence(i);
                try {

                    JSONObject station = (JSONObject) stop.get("stationinfo");
                    // tripDescription.setRouteId((String) station.get("@id"));

                    String stopId = (String) station.get("id");
                    stopId = stopId.replaceFirst("[^0-9]+", "") + ":";
                    stopId = stopId.substring(2); // remove first '00'
                    if (!stop.get("platform").equals("") && !stop.get("platform").equals("?")) {
                        stopId += stop.get("platform");
                    } else {
                        stopId += "0";
                    }
                    
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

                // delays
                arrivalDelay = (String) stop.get("arrivalDelay");
                departureDelay = (String) stop.get("departureDelay");
                
                int arrivalDelayInt = Integer.parseInt(arrivalDelay);
                int departureDelayInt = Integer.parseInt(departureDelay);
                
                if (maxDelay < arrivalDelayInt) {
                    maxDelay = arrivalDelayInt;
                }
                if (maxDelay < departureDelayInt) {
                    maxDelay = departureDelayInt;
                }
                
                long now = System.currentTimeMillis();

                //Calculate arrival times
                long scheduledArrivalTimeUnixSeconds = Long.parseLong((String) stop.get("scheduledArrivalTime"));
                java.util.Date scheduledArrivalTime = new java.util.Date((long)scheduledArrivalTimeUnixSeconds*1000);
                // add arrivalDelay to get real arrival time
                long arrivalTimeMillis = (DateUtils.addSeconds(scheduledArrivalTime, arrivalDelayInt)).getTime(); 

                //Calculate departure times
                long scheduledDepartureTimeUnixSeconds = Long.parseLong((String) stop.get("scheduledDepartureTime"));
                java.util.Date scheduledDepartureTime = new java.util.Date((long)scheduledDepartureTimeUnixSeconds*1000);
                // add departureDelay to get real departure time
                long departureTimeMillis = (DateUtils.addSeconds(scheduledDepartureTime, departureDelayInt)).getTime(); 
                
                // If stoptime is (partially) canceled
                String isCanceled = (String) stop.get("canceled");
                if (!isCanceled.equals("0")) {
                    // Set ScheduleRelationship of stoptime to SKIPPED
                    stopTimeUpdate.setScheduleRelationship(GtfsRealtime.TripUpdate.StopTimeUpdate.ScheduleRelationship.SKIPPED);
                } else {
                    // If a current or future stoptime isn't canceled, the whole trip isn't canceled
                    if (wholeTripCanceled && arrivalTimeMillis >= now) {
                        wholeTripCanceled = false;
                    }
                }

                // Set Arrival in the object
                stopTimeArrival.setDelay(arrivalDelayInt);
                //    setTime takes parameter in seconds
                stopTimeArrival.setTime(arrivalTimeMillis/1000);
                stopTimeUpdate.setArrival(stopTimeArrival);
                // Do the same for departure
                stopTimeDeparture.setDelay(Integer.parseInt(departureDelay));
                //    setTime takes parameter in seconds
                stopTimeDeparture.setTime(departureTimeMillis/1000);
                stopTimeUpdate.setDeparture(stopTimeDeparture);
                tripUpdate.addStopTimeUpdate(stopTimeUpdate);
            }
            
            tripDescription.setScheduleRelationship(GtfsRealtime.TripDescriptor.ScheduleRelationship.SCHEDULED);
            if (wholeTripCanceled) {
                // Can be partially canceled
                tripDescription.setScheduleRelationship(GtfsRealtime.TripDescriptor.ScheduleRelationship.CANCELED);
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
