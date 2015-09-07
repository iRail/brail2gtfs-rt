/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;

/**
 *
 * @author timtijssens
 */
public class DownloadDelayedTrains implements Runnable {

    private String trainName;
    private final String url;
    private ErrorLogWriter errorWriter = new ErrorLogWriter();

    public DownloadDelayedTrains(String trainName, String url) {
        this.trainName = trainName;
        this.url = url;
    }

    @Override
    public void run() {
        try {
            String fileName = "./delays/" + trainName + ".json";
            downloadDelayedTrains(url, fileName);
            removeEmptyLines(fileName);
        } catch (Exception e) {
            // System.out.println(e);           
            errorWriter.writeError(e.toString());
        }
    }

    private void downloadDelayedTrains(String url, String fileName) throws MalformedURLException, IOException {
        System.out.println("Writing " + fileName);
        URL link = new URL(url);
        InputStream in = new BufferedInputStream(link.openStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n = 0;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        out.close();
        in.close();
        
        byte[] response = out.toByteArray();
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(response);
        fos.close();
    }
    
    // Removes whitespaces before the data begins
    private void removeEmptyLines(String fileName) throws FileNotFoundException, IOException {
        System.out.println("Removing empty lines from " + fileName);
        String fileNameTemp = fileName + ".temp";
        
        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);
        FileWriter fw = new FileWriter(fileNameTemp);
        String line;

        while ((line = br.readLine()) != null) {
            line = line.trim(); // remove leading and trailing whitespace
            if (!line.equals("")) // don't write out blank lines
            {
                fw.write(line, 0, line.length());
            }
        }
        
        fr.close();
        fw.close();
        
        // Everything is copied to temporary file so delete original
        File f = new File(fileName);
        f.delete();
        
        f = new File(fileNameTemp);
        
        // rename new file
        boolean success = f.renameTo(new File(fileName));
        
        if (success) {
            System.out.println("File " + fileName + " successfully rewritten");
        } else {
            System.out.println("Something went wrong with rewriting " + fileName);
        }
    }
}
