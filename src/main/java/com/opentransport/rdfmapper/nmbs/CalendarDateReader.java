/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import com.opentransport.rdfmapper.nmbs.containers.CalendarDate;
import com.opentransport.rdfmapper.nmbs.containers.Route;
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

    private ArrayList<String> serviceIdsCurrentDate = new ArrayList<>();
    private FileCleaner fc = new FileCleaner();

    public CalendarDateReader() {
        readCalendarDatesfromGTFS();
    }

    // Make sure to delete past calendarDates from calendar_dates.txt
    // Otherwise this will be too slow!
    private void readCalendarDatesfromGTFS() {
        try {
            fc.cleanUpFile("calendar_dates", ".txt");
            BufferedReader in = new BufferedReader(new FileReader("calendar_dates.txt"));
            String line;
            int lineCounter = 0;
            String currentDate = getCurrentDate();
            boolean added = false;
            
            while ((line = in.readLine()) != null) {
                if (lineCounter > 0) {
                    String[] parts = line.split(",");
                    // System.out.println("Reading " + parts[1]);
                    if (parts[1].equals(currentDate)) {
                        CalendarDate calendarDate = new CalendarDate(parts[0], parts[1], parts[2]);
                        serviceIdsCurrentDate.add(calendarDate.getService_id());
                        added = true;
                    } else if (added) {
                        break; // Not necessary to read futher
                    }
                }
                
                lineCounter++;
            }
            System.out.println("Ready with reading calendar_dates.txt");
            in.close();
        } catch (FileNotFoundException fnfe) {
            System.out.println("Calendar_dates.txt not found");
        } catch (IOException ex) {
            Logger.getLogger(CalendarDateReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * 
     * @return string current date in YYYYMMDD format
     */
    public String getCurrentDate() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String month = "" + (cal.get(Calendar.MONTH) + 1);

        String day = "" + cal.get(Calendar.DAY_OF_MONTH);
        if (day.length() < 2) {
            day = "0" + day;
        }
        if (month.length() < 2) {
            month = "0" + month;
        }

        return cal.get(Calendar.YEAR) + month + day;
    }

    /**
     * @return currentDayServiceIDs Array of serviceIds that happen on current day
     */
    public ArrayList<String> getServiceIDsOfCurrentDay() {
        return serviceIdsCurrentDate;
    }
}
