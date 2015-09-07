/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import com.opentransport.rdfmapper.nmbs.containers.Route;
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

    private HashMap routes;

    public RoutesReader() {
        routes = new HashMap();
        
        readTxt();
    }

    public String getRouteLongName(String route_id) {
        Route route = (Route) routes.get(route_id);

        try {
            return route.getRoute_long_name();
        } catch (Exception e) {
            System.out.println("Route_id not found in routes.txt: " + route_id);
        }

        return "";
    }

    private void readTxt() {
        try {
            BufferedReader in = new BufferedReader(new FileReader("routes.txt"));
            String line;
            int lineCounter = 0;
            while ((line = in.readLine()) != null) {
                if (lineCounter > 0) {
                    String[] parts = line.split(",");
                    Route route = new Route(parts[0], parts[1], parts[2], parts[3], parts[4]);

                    routes.put(route.getRoute_id(), route);
                }

                lineCounter++;
            }
            in.close();
        } catch (FileNotFoundException fe) {
            System.out.println("Failed to open routes.txt");
        } catch (IOException ioe) {
            System.out.println("Failed to read routes.txt");
        }
    }
}
