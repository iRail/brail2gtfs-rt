/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import com.opentransport.rdfmapper.nmbs.containers.GtfsRealtime.TripDescriptor;
import com.opentransport.rdfmapper.nmbs.containers.LiveRoute;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.opentransport.rdfmapper.nmbs.containers.GtfsRealtime.TripUpdate;
import com.opentransport.rdfmapper.nmbs.containers.GtfsRealtime.TripUpdate.StopTimeEvent;
import com.opentransport.rdfmapper.nmbs.containers.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.opentransport.rdfmapper.nmbs.containers.GtfsRealtime.VehicleDescriptor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;





/**
 *
 * @author timtijssens
 */
public class LiveRouteFetcher {
    
    private Map<String,LiveRoute> database;
    private Map<String,String> mapping;
    private static volatile StationDatabase stationDB;
 

    
    private LiveRouteFetcher(String trainId) {
        database = new HashMap<>();
        mapping = new HashMap<>();
        JSONParser parser = new JSONParser();
        try {
            
   
            
            FileReader fr = new FileReader("http://api.irail.be/vehicle/?id=BE.NMBS."+trainId+"&fast=true");
            JSONObject json = (JSONObject) parser.parse(fr);
            String vehiclename = (String) json.get("vehicle");
            JSONArray trainstops = (JSONArray) json.get("stops");
            for (int i = 0; i < trainstops.size(); i++) {
                JSONObject stop = (JSONObject) trainstops.get(i);
                int stopNo = (int) stop.get("id");
                int stopDelay = (int) stop.get("delay");
                String stationName = (String) stop.get("name");
                Date arrivalTime  = (Date) stop.get("time");
               // database.put(id,new StationInfo(name,id,longitude,latitude));
               // mapping.put(name,id);
            }
            fr.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(StationDatabase.class.getName()).log(Level.SEVERE,null,ex);
        } catch (IOException | ParseException ex) {
            Logger.getLogger(StationDatabase.class.getName()).log(Level.SEVERE,null,ex);
        }
    }
    
    
    

    
    
}
class AddTripDemoUpdateold{
    
       //This function fills in the a Update Message
    static TripUpdate.Builder PromptForUpdate()throws IOException{
        TripUpdate.Builder tripUpdate =  TripUpdate.newBuilder();
        VehicleDescriptor.Builder vehicleDescription = VehicleDescriptor.newBuilder();
        TripDescriptor.Builder tripDescription = TripDescriptor.newBuilder();
        StopTimeUpdate.Builder stopTimeUpdate = StopTimeUpdate.newBuilder();
        
        //Each StopTime Update contains StopTimeEvents witht the stop Arrival and Departure Time 
        StopTimeEvent.Builder stopTimeArrival = StopTimeEvent.newBuilder();
        StopTimeEvent.Builder stopTimeDeparture = StopTimeEvent.newBuilder();
        
        
        
        
        //Setting the demo data
        //Setting the VehicleData
        vehicleDescription.setId("IC 511");
        vehicleDescription.setLabel("http://www.belgianrail.be/as/hafas-res/img/products/ic.png");
        vehicleDescription.setLicensePlate("Operated By the NMBS");
        
        // Setting the Trip Description
        tripDescription.setStartTime("11:40:00");
        //YYYYMMDD format
        tripDescription.setStartDate("20150710");
        
        //Setting the StopTimeUpdate 
        //must be the same as in stop_times.txt in the corresponding GTFS Feed
        stopTimeUpdate.setStopSequence(1);
        //must be the same as in stops.txt in the corresponding GTFS Feed
        stopTimeUpdate.setStopId("1");
        
        // Setting the Arrival and Departure 
        stopTimeArrival.setDelay(0);
        stopTimeArrival.setTime(1436522359);
        
        stopTimeDeparture.setDelay(0);
        stopTimeDeparture.setTime(1436522359);
        
        
        //Add the stop times to the StopTimeUpdate 
        stopTimeUpdate.setArrival(stopTimeArrival);
        stopTimeUpdate.setDeparture(stopTimeDeparture);
        //schedule_relationship
        
        
        
        // Set the data for the tripUpdate
        
        tripUpdate.setTrip(tripDescription);
        tripUpdate.setVehicle(vehicleDescription);
        // Set the update for a  certain stop first is the stop id , second is stopTimeUpdate object
        // For example in this case from Oostende to Eupen stop 2 would correspond with Brugge
        tripUpdate.setStopTimeUpdate(2, stopTimeUpdate);
        //Should be improved using addStopTimeUpdate;
        
        
        
        
        tripUpdate.setDelay(2);
        
        
        
        return tripUpdate;

    
    };
    
 

}
