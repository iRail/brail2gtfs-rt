/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;


import com.opentransport.rdfmapper.nmbs.containers.GtfsRealtime;
import com.opentransport.rdfmapper.nmbs.containers.GtfsRealtime.FeedEntity;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timtijssens
 */
class AddTripDemoUpdate{
    
       //This function fills in the a Update Message
    static GtfsRealtime.FeedMessage.Builder PromptForUpdate()throws IOException{
        
        
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
        
        //Each Stops created new data
        GtfsRealtime.TripUpdate.StopTimeUpdate.Builder stopTimeUpdate = GtfsRealtime.TripUpdate.StopTimeUpdate.newBuilder();
        //Each StopTime Update contains StopTimeEvents witht the stop Arrival and Departure Time 
        GtfsRealtime.TripUpdate.StopTimeEvent.Builder stopTimeArrival = GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder();
        GtfsRealtime.TripUpdate.StopTimeEvent.Builder stopTimeDeparture = GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder();
        
        //Setting the demo data
        //Setting the VehicleData
        vehicleDescription.setId("IC 511");
        vehicleDescription.setLabel("http://www.belgianrail.be/as/hafas-res/img/products/ic.png");
        vehicleDescription.setLicensePlate("Operated By the NMBS");
        
        // Setting the Trip Description
        tripDescription.setStartTime("11:40:00");
        //YYYYMMDD format
        tripDescription.setStartDate("20150710");
        tripDescription.setDirectionId(2);
        tripDescription.setRouteId("Direction Eupen");
        tripDescription.setTripId("1");
        
        //Setting the StopTimeUpdate 
        //must be the same as in stop_times.txt in the corresponding GTFS Feed        
        stopTimeUpdate.setStopSequence(0);
        //must be the same as in stops.txt in the corresponding GTFS Feed
        stopTimeUpdate.setStopId("0");
        
        // Setting the Arrival and Departure 
        stopTimeArrival.setDelay(0);
        stopTimeArrival.setTime(1436522359);
        
        stopTimeDeparture.setDelay(0);
        stopTimeDeparture.setTime(1436522359);
                
        //Add the stop times to the StopTimeUpdate 
        stopTimeUpdate.setArrival(stopTimeArrival);
        stopTimeUpdate.setDeparture(stopTimeDeparture); 
        tripUpdate.addStopTimeUpdate(stopTimeUpdate);
        
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
        tripUpdate.addStopTimeUpdate(stopTimeUpdate);

        
        //schedule_relationship
        // Set the data for the tripUpdate
        
        tripUpdate.setTrip(tripDescription);
        
        tripUpdate.setVehicle(vehicleDescription);
        feedEntity.setTripUpdate(tripUpdate);
        feedMessage.setHeader(feedHeader);
        
        //feedMessage.setEntity(1, feedEntity);
        feedMessage.addEntity(0, feedEntity);
       
        // Set the update for a  certain stop first is the stop id , second is stopTimeUpdate object
        // For example in this case from Oostende to Eupen stop 2 would correspond with Brugge
        
        
        
        
        
        
        
        tripUpdate.setDelay(0);
        
        
        
        return feedMessage;

    
    };

 

}

    

