/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import com.opentransport.rdfmapper.nmbs.containers.LiveRoute;
import com.opentransport.rdfmapper.nmbs.containers.StationInfo;
import com.opentransport.rdfmapper.nmbs.containers.TrainInfo;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class TrainDatabase {
    
    private Map<String,LiveRoute> database;
    private Map<String,String> mapping;
    private static volatile TrainDatabase trainDB;
    
    private TrainDatabase(){
    database = new HashMap<>();
    mapping = new HashMap<>();  
  
        try (BufferedReader br = new BufferedReader(new FileReader("NMBS_trainRoutes.txt")))
		{
 
			String sCurrentLine;
 
			while ((sCurrentLine = br.readLine()) != null) {                            
				System.out.println(sCurrentLine);
                                  LiveRoute liveRoute = null ;
                                String routeUrl = null,routeId = null,agencyId = null,longname = null, serviceInfo = null;
                              for (String retval: sCurrentLine.split(",")){                                 
                                  int position = 0 ;
                                  if (position ==0){
                                     routeUrl = retval;                                                                         
                                  }
                                  if (position ==1){
                                      agencyId = retval;                                      
                                  }
                                  if (position ==2){
                                     routeId = retval;                                      
                                  }
                                  if (position ==3){
                                      longname = retval;                                      
                                  }
                                  if (position ==4){
                                      serviceInfo = retval;                                 
                                  }                
                                
                               
                                  position++;                        
                                }   
                               // liveRoute = new LiveRoute(routeUrl, agencyId,routeId,longname,serviceInfo);                               
                                  database.put(routeId,liveRoute);
                                  mapping.put(longname,routeId);
			}
                        br.close();
                        
 
		} catch (IOException e) {
			e.printStackTrace();
		} 
        
        
        
    
          
   
     
    
    }
    
        public static TrainDatabase getInstance() {
        if (trainDB == null) {
            trainDB = new TrainDatabase();
        }
        return trainDB;
    }
    
    public LiveRoute getTrainInfo(String trainId) {
        return database.get(trainId);
    }
    
 
    
    public List<String> getAllTrainIds() {
        return new ArrayList<>(database.keySet());
    }
    
    

    
    
 
    
}
