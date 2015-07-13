/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import com.opentransport.rdfmapper.nmbs.containers.LiveRoute;
import java.io.FileReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.IOException;





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
