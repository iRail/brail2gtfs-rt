/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import com.opentransport.rdfmapper.nmbs.containers.CalendarDates;
import com.opentransport.rdfmapper.nmbs.containers.Routes;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timtijssens
 */
public class CalendarDateReader {
     private HashMap CalendarDates = new HashMap();
     private ArrayList<String> currentDayServiceIDs = new ArrayList<>();
     private FileCleaner fc = new FileCleaner();
    

    public CalendarDateReader(){
         try {
             readCalendarfromGTFS();
         } catch (IOException ex) {
             Logger.getLogger(CalendarDateReader.class.getName()).log(Level.SEVERE, null, ex);
         }
    
    }

    private void readCalendarfromGTFS()throws FileNotFoundException, IOException{  
    
    fc.cleanUpFile("calendar_dates", ".txt");        
    BufferedReader in = new BufferedReader(new FileReader("calendar_dates.txt"));
    String line;
    int lineCounter=0;
    
    Date date = new Date(); 
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);    
    String month = ""+ (cal.get(Calendar.MONTH)+1);

    String day = "" + cal.get(Calendar.DAY_OF_MONTH);
    if(day.length()<2){
             day = "0"+day;
    }
    if (month.length()<2) {
                month = "0"+month;

    }
            
    String currentDay = ""+ cal.get(Calendar.YEAR) +month  + day;
    
    while((line = in.readLine()) != null)
    {
        if (lineCounter > 0) {
            
            String[] parts = line.split(",");
            if (currentDay.equals(parts[1])){
                CalendarDates calendarDate = new CalendarDates(parts[0], parts[1], parts[2]);
                CalendarDates.put(calendarDate.getService_id(), calendarDate);
            }
            
            
         
        }
        
        lineCounter++;
    }
    in.close();        
       
    }

    /**
     * @return the currentDayServiceIDs
     */
    public ArrayList<String> getCurrentDayServiceIDs() {
       Iterator it = CalendarDates.entrySet().iterator();
        while (it.hasNext()) {
            CalendarDates calendarDate;
            Map.Entry pair = (Map.Entry)it.next();
            calendarDate = (CalendarDates) pair.getValue();
            
                    currentDayServiceIDs.add(calendarDate.getService_id());

                  
            it.remove(); // avoids a ConcurrentModificationException
        }
        
        
        
        return currentDayServiceIDs;
    }
    
}
