/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import com.opentransport.rdfmapper.nmbs.containers.Trip;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timtijssens
 */
public class TripReader {
    // private FileCleaner fc = new FileCleaner();
    // private HashMap trips;
    private HashMap tripByServiceId;
    
    public TripReader(){  
        // trips = new HashMap();
        tripByServiceId = new HashMap();
                
        readTripsfromGTFS();
    }
    
    private void readTripsfromGTFS() {  
        try {
            //fc.cleanUpFile("trips", ".txt");
            BufferedReader in = new BufferedReader(new FileReader("trips.txt"));
            String line;
            int lineCounter=0;
            while ((line = in.readLine()) != null) {
                if (lineCounter > 0) {
                    String[] parts = line.split(",");
                    Trip trip = new Trip(parts[0], parts[1], parts[2]);

                    // trips.put(trip.getTrip_id(), trip);
                    // NMBS GTFS has 1-1 mapping between serviceId and tripId
                    tripByServiceId.put(trip.getService_id(), trip);
                }

                lineCounter++;
            }
            
            in.close();   
        } catch (FileNotFoundException fne) {
            System.out.println("trips.txt not found");
        } catch (IOException ex) {
            Logger.getLogger(TripReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Returns trip id of trip with given route id.
     * Every route drives once per day so we find match of services that drive this day.
     * 
     * @param routeId route id of trip
     * @param cdr calendarDateReader
     * @return tripId trip id of trip
     */
    public String getTripIdFromRouteId(String routeId, CalendarDateReader cdr) {   
        ArrayList<String> possibleServiceIds = cdr.getServiceIDsOfCurrentDay();

        for (int i = 0; i < possibleServiceIds.size(); i++) {
            String serviceId =  possibleServiceIds.get(i);
            Trip trip = (Trip) tripByServiceId.get(serviceId);
            if (routeId.equals(trip.getRoute_id())) {
                return trip.getTrip_id();
            }
        }
        
        return "";
    }
}
