/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import java.net.URL;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;

/**
 *
 * @author timtijssens
 */
public class GtfsRealtimeExample {
    
    public GtfsRealtimeExample(String filename) throws Exception {
        System.out.println("Testing Real Time Example");
    URL url = new URL("http://localhost:8000/" +filename);
    FeedMessage feed = FeedMessage.parseFrom(url.openStream());
    for (FeedEntity entity : feed.getEntityList()) {
      if (entity.hasTripUpdate()) {
        System.out.println(entity.getTripUpdate());
      }
        if (entity.hasAlert()) {
            System.out.println(entity.getAlert());
            
        }
    }
  }
    
}
