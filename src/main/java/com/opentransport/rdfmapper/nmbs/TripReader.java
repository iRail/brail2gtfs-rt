/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import com.opentransport.rdfmapper.nmbs.containers.CalendarDates;
import com.opentransport.rdfmapper.nmbs.containers.Trips;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timtijssens
 */
public class TripReader {
    private FileCleaner fc = new FileCleaner();
    private HashMap Trips = new HashMap();
    private CalendarDateReader cdr = new CalendarDateReader();
    ArrayList<String> todaysCallendarServiceId;
    
    
    public String getTripId(String routeID){
        
        todaysCallendarServiceId = cdr.getCurrentDayServiceIDs();
         
        Iterator it = Trips.entrySet().iterator();
        
        while (it.hasNext()) {
        Trips trip;
        Map.Entry pair = (Map.Entry)it.next();
        trip = (Trips) pair.getValue();
        
        for (int i = 0; i < todaysCallendarServiceId.size(); i++) {
            String serviceId =  todaysCallendarServiceId.get(i);
             if(trip.getService_id().equals(serviceId)){
                 if (trip.getRoute_id().equals(routeID)){
                               String ssd  ="";
                                return trip.getTrip_id();
                     
                 }
       
             }            
        }
             
        it.remove(); // avoids a ConcurrentModificationException
        } 
        return "";
    }
    
    public TripReader(){
    
        try {
            readCalendarfromGTFS();
        } catch (IOException ex) {
            Logger.getLogger(TripReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void readCalendarfromGTFS()throws FileNotFoundException, IOException{  
    
    fc.cleanUpFile("trips", ".txt");
        
    BufferedReader in = new BufferedReader(new FileReader("trips.txt"));
    String line;
    int lineCounter=0;
    while((line = in.readLine()) != null)
    {
        if (lineCounter > 0) {
            
            String[] parts = line.split(",");
            Trips trip = new Trips(parts[0], parts[1], parts[2]);
            
            Trips.put(trip.getTrip_id(), trip);
        }
        
        lineCounter++;
    }
    in.close();        
       
    }
    
}
