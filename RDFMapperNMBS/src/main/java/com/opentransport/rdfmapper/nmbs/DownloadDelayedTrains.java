/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author timtijssens
 */
public class DownloadDelayedTrains implements Runnable{
    
    private String trainName;
    private final String url;
    public DownloadDelayedTrains(String trainName,String url){
        this.trainName = trainName;
        this.url = url;
    }

    @Override 
   
    public void run() {
        try {
            downloadDelayedTrains(trainName,url);
          
        } catch (Exception e) {
            System.out.println(e);
        }
        
    }

    private void downloadDelayedTrains(String url, String trainName)  throws MalformedURLException, IOException{

            String fileName = "./delays/" +trainName +".json"; 

		 URL link = new URL(url);
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
                 //System.out.println("Finished writing JSON File");
    }


    
}
