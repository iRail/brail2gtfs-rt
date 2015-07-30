/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import com.opentransport.rdfmapper.nmbs.containers.Routes;
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
public class RoutesReader {
    
    private HashMap Routes = new HashMap();
    private ErrorLogWriter errorWriter =  new  ErrorLogWriter();
    private String getTrip_Id ;
    private int teller = 0;
    String routeLongName="";
    
    
    
    
    public String getRouteLongName(String route_id){
  
    Routes route ;
        
       route = (Routes) Routes.get(route_id);
       // RouteInfo = (RoutesInfoTemp) RoutesInfo.get("IC118");
        teller ++;
       
        try {
            
          routeLongName= route.getRoute_long_name();
        } catch (Exception e) {
            System.out.println(route_id);
            System.out.println(e);
            
        }
       
       return routeLongName;
       
       
    
    }   
    
    public RoutesReader(){
        try {
            Readtxt();
        } catch (IOException ex) {
            errorWriter.writeError(ex.toString());
            Logger.getLogger(RoutesInfoTempReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    
    }
    
    private void Readtxt() throws FileNotFoundException, IOException{
    BufferedReader in = new BufferedReader(new FileReader("routes.txt"));
    String line;
    int lineCounter=0;
    while((line = in.readLine()) != null)
    {
        if (lineCounter > 0) {
            
            String[] parts = line.split(",");
            Routes route = new Routes(parts[0], parts[1], parts[2],parts[3],parts[4]);
            
            Routes.put(route.getRoute_id(), route);
        }
        
        lineCounter++;
    }
    in.close(); 
       
       
    
    }
    
}
