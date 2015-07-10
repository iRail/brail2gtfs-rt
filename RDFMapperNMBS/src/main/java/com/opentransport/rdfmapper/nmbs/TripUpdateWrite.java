/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import com.opentransport.rdfmapper.nmbs.containers.GtfsRealtime;
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
    static GtfsRealtime.TripUpdate.Builder PromptForUpdate()throws IOException{
        GtfsRealtime.TripUpdate.Builder tripUpdate =  GtfsRealtime.TripUpdate.newBuilder();
        GtfsRealtime.VehicleDescriptor.Builder vehicleDescription = GtfsRealtime.VehicleDescriptor.newBuilder();
        GtfsRealtime.TripDescriptor.Builder tripDescription = GtfsRealtime.TripDescriptor.newBuilder();
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
        
        tripUpdate.addStopTimeUpdate(stopTimeUpdate);
        //Should be improved using addStopTimeUpdate;
        
        
        
        
        tripUpdate.setDelay(2);
        
        
        
        return tripUpdate;

    
    };

 

}

    

