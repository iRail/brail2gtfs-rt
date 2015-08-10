/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

/**
 *
 * @author timtijssens
 */
public class FileCleaner {
 

    public void cleanUpFile(String filename, String extension){
            fileRewrite(filename, extension);
    
    }
    
    
    private void rewriteFileSpaces(String filename,String extension) throws FileNotFoundException, IOException{
        
        FileReader fr = new FileReader(filename); 
        BufferedReader br = new BufferedReader(fr); 
        FileWriter fw = new FileWriter("temp"+extension); 
        String line;

        while((line = br.readLine()) != null)
        { 
            line = line.trim(); // remove leading and trailing whitespace
            if (!line.equals("")) // don't write out blank lines
            {
                fw.write(line, 0, line.length());
            }
        } 
        fr.close();
        fw.close();
    }
    private void fileRewrite(String fileName, String extension){
            try {
            rewriteFileSpaces(fileName,extension);
            File f = new File(fileName);
            f.delete();
            f = new File("temp"+extension);
            File newFile = new File(f.getParent(), fileName);
            Files.move(f.toPath(), newFile.toPath());
        } catch (Exception e) {
        }
    
    }
    
}
