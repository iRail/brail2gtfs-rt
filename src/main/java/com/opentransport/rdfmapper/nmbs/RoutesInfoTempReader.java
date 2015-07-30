/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import com.opentransport.rdfmapper.nmbs.containers.RoutesInfoTemp;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timtijssens
 */
public class RoutesInfoTempReader {
    
    
    private HashMap RoutesInfo = new HashMap();
    private ErrorLogWriter errorWriter =  new  ErrorLogWriter();
    private String getTrip_Id ;
    private int teller = 0;
    String tripId="";
    
    public String getTrip_id(String route_id){
  
    RoutesInfoTemp routeInfo ;
        
       routeInfo = (RoutesInfoTemp) RoutesInfo.get(route_id);
       // RouteInfo = (RoutesInfoTemp) RoutesInfo.get("IC118");
        teller ++;
       
        try {
            
          tripId=   routeInfo.getTrip_Id();
        } catch (Exception e) {
            System.out.println(route_id);
            System.out.println(e);
            
        }
       
       return tripId;
       
       
    
    }   
    
    public RoutesInfoTempReader(){
        try {
            Readtxt();
        } catch (IOException ex) {
            errorWriter.writeError(ex.toString());
            Logger.getLogger(RoutesInfoTempReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    };
    
    private void Readtxt() throws FileNotFoundException, IOException{
    BufferedReader in = new BufferedReader(new FileReader("routes_info.tmp.txt"));
    String line;
    int lineCounter=0;
    while((line = in.readLine()) != null)
    {
        if (lineCounter > 0) {
            
            String[] parts = line.split(",");
            RoutesInfoTemp RouteInfo = new RoutesInfoTemp(parts[0], parts[1], parts[2]);
            RoutesInfo.put(RouteInfo.getRoute_short_name(), RouteInfo);
        }
        
        lineCounter++;
    }
    in.close(); 
       
    
    }
    
    
    
}
