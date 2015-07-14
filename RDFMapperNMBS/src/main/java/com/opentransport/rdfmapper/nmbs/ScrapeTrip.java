/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import com.opentransport.rdfmapper.nmbs.containers.GtfsRealtime;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
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
    
    private String urlString ="http://api.irail.be/vehicle/?id=BE.NMBS.IC511&format=json";
    private String trainId ="";
    private String outputName="vehicleTrip.json";
    
 
    private void downloadJson() throws MalformedURLException, IOException{
                String fileName = outputName; //The file that will be saved on your computer
		 URL link = new URL(urlString); //The file that you want to download
		
                  //Code to download
		 InputStream in = new BufferedInputStream(link.openStream());
		 ByteArrayOutputStream out = new ByteArrayOutputStream();
		 byte[] buf = new byte[1024];
		 int n = 0;
		 while (-1!=(n=in.read(buf)))
		 {
		    out.write(buf, 0, n);
		 }
		 out.close();
		 in.close();
		 byte[] response = out.toByteArray();
 
		 FileOutputStream fos = new FileOutputStream(fileName);
		 fos.write(response);
		 fos.close();
                    //End download code
		 
		 System.out.println("Finished writing JSON File");

	}
    
    
    
    private void scrapeJson() {
        
         
        GtfsRealtime.FeedMessage.Builder feedMessage =  GtfsRealtime.FeedMessage.newBuilder();        
        GtfsRealtime.FeedHeader.Builder feedHeader =GtfsRealtime.FeedHeader.newBuilder();
        feedHeader.setGtfsRealtimeVersion("1.0");
        feedHeader.setIncrementality(GtfsRealtime.FeedHeader.Incrementality.FULL_DATASET);
         //Unix Style
        feedHeader.setTimestamp(System.currentTimeMillis() / 1000L);
        GtfsRealtime.FeedEntity.Builder feedEntity = GtfsRealtime.FeedEntity.newBuilder();
         
        feedEntity.setId("1");
        feedEntity.setIsDeleted(false);
        GtfsRealtime.VehiclePosition.Builder vehiclePosition = GtfsRealtime.VehiclePosition.newBuilder();
        vehiclePosition.setStopId("fff");
         //GtfsRealtime.Alert.Builder  alert = GtfsRealtime.Alert.newBuilder();
        
         // feedEntity.setAlert(alert);

        //Data that doesnt Update
        GtfsRealtime.TripUpdate.Builder tripUpdate =  GtfsRealtime.TripUpdate.newBuilder();
        GtfsRealtime.VehicleDescriptor.Builder vehicleDescription = GtfsRealtime.VehicleDescriptor.newBuilder();
        GtfsRealtime.TripDescriptor.Builder tripDescription = GtfsRealtime.TripDescriptor.newBuilder();
        GtfsRealtime.TripUpdate.StopTimeUpdate.Builder stopTimeUpdate = GtfsRealtime.TripUpdate.StopTimeUpdate.newBuilder();
        
        //Each StopTime Update contains StopTimeEvents witht the stop Arrival and Departure Time 
        GtfsRealtime.TripUpdate.StopTimeEvent.Builder stopTimeArrival = GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder();
        GtfsRealtime.TripUpdate.StopTimeEvent.Builder stopTimeDeparture = GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder();
        
        
        JSONParser parser = new JSONParser();
        try {
            FileReader fr = new FileReader(outputName);
            JSONObject json = (JSONObject) parser.parse(fr);
            String trainId = (String) json.get("vehicle");
            //Setting the VehicleData
            vehicleDescription.setId(trainId);
            vehicleDescription.setLicensePlate(trainId);
            System.out.println(trainId);
            
            //Handling Departure Date
            
            String unixSeconds = (String) json.get("timestamp");
            Long unixSec = Long.parseLong(unixSeconds);
            
            Date date = new Date(unixSec*1000L); // *1000 is to convert seconds to milliseconds
            SimpleDateFormat sdfStartDate = new SimpleDateFormat("yyyyMMdd"); // the format of your date
            sdfStartDate.setTimeZone(TimeZone.getTimeZone("GMT+2")); // give a timezone reference for formating
            SimpleDateFormat sdfStartTimeHour = new SimpleDateFormat("HH:mm:ss");
            
            String formattedDepartureDate = sdfStartDate.format(date);
            String formattedDepartureHour= sdfStartTimeHour.format(date);
            
            // Setting the Trip Description
            tripDescription.setStartTime(formattedDepartureHour.toString());
            //YYYYMMDD format
            tripDescription.setStartDate(formattedDepartureDate.toString());
            
            //Get Information about stops
            JSONObject rootStop = (JSONObject) json.get("stops"); 
            JSONArray stops = (JSONArray) rootStop.get("stop");
            String delay = null;
           
            for (int i = 0; i < stops.size(); i++) {
                //Information about the stops
                JSONObject stop = (JSONObject) stops.get(i);              
                String stopSeq = (String)stop.get("id");
               
                stopTimeUpdate.setStopSequence(Integer.parseInt(stopSeq));
                
                JSONObject station =(JSONObject) stop.get("stationinfo");
                System.out.println(station.get("@id"));
                stopTimeUpdate.setStopId((String) station.get("@id"));
                
              
               
                //Set Delay at arrival time
                delay = (String)stop.get("delay");
                stopTimeArrival.setDelay(Integer.parseInt(delay));
                String arrivalTime = (String)stop.get("time");
                stopTimeArrival.setTime(Long.parseLong(arrivalTime));
                // To Add Departure Time + 3 minutes ? 
                stopTimeDeparture.setDelay(Integer.parseInt(delay));
                stopTimeUpdate.setArrival(stopTimeArrival);
                stopTimeUpdate.setDeparture(stopTimeDeparture);
              
  
                tripUpdate.addStopTimeUpdate(stopTimeUpdate);
                //Should be improved using addStopTimeUpdate;
               
                
                


            }
              tripUpdate.setTrip(tripDescription);
              tripUpdate.setVehicle(vehicleDescription);
              tripUpdate.setDelay(Integer.parseInt(delay));
              
              feedEntity.setTripUpdate(tripUpdate);
              feedMessage.setHeader(feedHeader);
              feedMessage.addEntity(0, feedEntity);
              
              
             try {
              FileOutputStream output = new FileOutputStream("gtfs-rt");
              tripUpdate.build().writeTo(output);
              output.close();
              System.out.println("File writen successful");
              
            } catch (Exception e) {
                 System.out.println(e);
            }
             
            
            
            fr.close();
        } catch (FileNotFoundException ex) {
          
       } catch (IOException | ParseException ex) {
            
        }
        
        
    
    }
    private String returnDateFormated(){
        
        
        return null;
    }
    public ScrapeTrip(){
        try {
            //scrapeJson();
            
                downloadJson();
            
            
            scrapeJson();
        } catch (IOException ex) {
            Logger.getLogger(ScrapeTrip.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
}
