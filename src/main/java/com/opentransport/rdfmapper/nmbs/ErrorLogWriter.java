/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timtijssens
 */
public class ErrorLogWriter {
    private final String fileName ="errors.txt";
    
    
    
 private void writeErrorToFile(String error) throws IOException{
       Writer output;
       output = new BufferedWriter(new FileWriter(fileName,true));  //clears file every time
        output.append("%s\r\n" + error);
       
       output.close();
    

 
 }   
 public ErrorLogWriter(){
 }
 public  void writeError(String error){
        try {
            writeErrorToFile(error);
        } catch (IOException ex) {
            Logger.getLogger(ErrorLogWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
 }
         
         
         
        
}
