/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
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
public class ScrapeTrip {
    
    private String urlString ="http://api.irail.be/vehicle/?id=BE.NMBS.IC511&format=json";
    private String trainId ="";
    private String outputName="vehicleTrip.json";
    
 
    private void downloadJson() throws MalformedURLException, IOException{
                String fileName = outputName; //The file that will be saved on your computer
		 URL link = new URL(urlString); //The file that you want to download
		
                  //Code to download
		 InputStream in = new BufferedInputStream(link.openStream());
		 ByteArrayOutputStream out = new ByteArrayOutputStream();
		 byte[] buf = new byte[1024];
		 int n = 0;
		 while (-1!=(n=in.read(buf)))
		 {
		    out.write(buf, 0, n);
		 }
		 out.close();
		 in.close();
		 byte[] response = out.toByteArray();
 
		 FileOutputStream fos = new FileOutputStream(fileName);
		 fos.write(response);
		 fos.close();
                    //End download code
		 
		 System.out.println("Finished writing JSON File");

	}
    
    
    
    private void scrapeJson() {
        
        
        JSONParser parser = new JSONParser();
        try {
            FileReader fr = new FileReader(outputName);
            JSONObject json = (JSONObject) parser.parse(fr);
            JSONObject Teststop = (JSONObject) json.get("stops");
            System.out.println(Teststop);
           JSONArray stops = (JSONArray) Teststop.get("stop");
            System.out.println(stops.get(1));
           
            for (int i = 0; i < stops.size(); i++) {
                
                JSONObject stop = (JSONObject) stops.get(i);
                String station = (String) stop.get("station");
                System.out.println(station);
                
                
                //database.put(id,new StationInfo(name,id,longitude,latitude));
               // mapping.put(name,id);
            }
            fr.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(StationDatabase.class.getName()).log(Level.SEVERE,null,ex);
        } catch (IOException | ParseException ex) {
            Logger.getLogger(StationDatabase.class.getName()).log(Level.SEVERE,null,ex);
        }
        
        
    
    }
    public ScrapeTrip(){
        try {
            //scrapeJson();
            downloadJson();
            scrapeJson();
        } catch (IOException ex) {
            Logger.getLogger(ScrapeTrip.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
}
