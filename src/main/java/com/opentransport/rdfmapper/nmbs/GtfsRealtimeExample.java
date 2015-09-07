/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import java.net.URL;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author timtijssens
 */
public class GtfsRealtimeExample {
    
    public GtfsRealtimeExample(String filename) throws Exception {
        System.out.println("Testing " + filename);
        
        InputStream is;

        try {
            is = new FileInputStream(filename);
            FeedMessage feed = FeedMessage.parseFrom(is);

            for (FeedEntity entity : feed.getEntityList()) {
              if (entity.hasTripUpdate()) {
                System.out.println(entity.getTripUpdate());
              }
                if (entity.hasAlert()) {
                    System.out.println(entity.getAlert());
                }
            }
            is.close(); 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
      }    
}
