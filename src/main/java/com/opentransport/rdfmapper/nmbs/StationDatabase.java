package com.opentransport.rdfmapper.nmbs;

import com.opentransport.rdfmapper.nmbs.containers.StationInfo;
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
 * @author Nicola De Clercq
 */
public class StationDatabase {
    
    private Map<String,StationInfo> database;
    private Map<String,String> mapping;
    private static volatile StationDatabase stationDB;
    
    private StationDatabase() {
        database = new HashMap<>();
        mapping = new HashMap<>();
        JSONParser parser = new JSONParser();
        try {
            FileReader fr = new FileReader("NMBS_stations.json");
            JSONObject json = (JSONObject) parser.parse(fr);
            JSONArray stations = (JSONArray) json.get("stations");
            for (int i = 0; i < stations.size(); i++) {
                JSONObject station = (JSONObject) stations.get(i);
                String name = (String) station.get("name");
                String id = (String) station.get("id");
                String longitude = (String) station.get("longitude");
                String latitude = (String) station.get("latitude");
                database.put(id,new StationInfo(name,id,longitude,latitude));
                mapping.put(name,id);
            }
            fr.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(StationDatabase.class.getName()).log(Level.SEVERE,null,ex);
        } catch (IOException | ParseException ex) {
            Logger.getLogger(StationDatabase.class.getName()).log(Level.SEVERE,null,ex);
        }
    }
    
    public static StationDatabase getInstance() {
        if (stationDB == null) {
            stationDB = new StationDatabase();
        }
        return stationDB;
    }
    
    public StationInfo getStationInfo(String stationId) {
        return database.get(stationId);
    }
    
    public String getStationId(String stationName) {
        return mapping.get(stationName);
    }
    
    public List<String> getAllStationIds() {
        return new ArrayList<>(database.keySet());
    }
    
}