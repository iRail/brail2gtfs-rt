package com.opentransport.rdfmapper.nmbs;

import com.opentransport.rdfmapper.nmbs.containers.Station;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nicola De Clercq
 */
public class StationDatabase {

    private static volatile StationDatabase stationDB;
    private static HashMap stations = new HashMap();

    private StationDatabase() {
    }

    public static StationDatabase getInstance() {
        if (stationDB == null) {
            stationDB = new StationDatabase();
        }
        return stationDB;
    }

    public static List<String> getAllStationIdsFromGTFSFeed(){
        try {
            readTxtGetAllStations();
            return new ArrayList<>(stations.keySet());
        } catch (IOException ex) {
            Logger.getLogger(StationDatabase.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<>();
        }
    }

    private static void readTxtGetAllStations() throws FileNotFoundException, IOException{

    FileCleaner fc = new FileCleaner() ;
    fc.cleanUpFile("stops",".txt");
    BufferedReader in = new BufferedReader(new FileReader("stops.txt"));
    String line;
    in.readLine(); // header
    while((line = in.readLine()) != null)
    {
        if (!line.equals("")) {
            String[] parts = line.split(",");
            String[] stationID = parts[0].split(":");
            // Don't add platforms
            if (stationID.length == 1) {
                String statId = stationID[0];
                Station station = new Station(stationID[0], parts[1],parts[2],parts[3]);
                stations.put(station.getId(), station);
            }
        }
    }
    in.close();
    }
}
