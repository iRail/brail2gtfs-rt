/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs.containers;

import java.util.Date;

/**
 *
 * @author timtijssens
 */
public class TrainStop {
    private Date arrivalTime;
    private String stationName;
    private int delay;
    private int stopNo;
    //Has to be implemented
    private Station station ;
    
    private int place;
    
    public TrainStop(int stopNo,int delay, String stationName, Date arrivalTime ){
    this.stopNo = stopNo;
    this.delay=delay;
    this.stationName = stationName;
    this.arrivalTime = arrivalTime;  
    
    }

    /**
     * @return the arrivalTime
     */
    public Date getArrivalTime() {
        return arrivalTime;
    }

    /**
     * @param arrivalTime the arrivalTime to set
     */
    public void setArrivalTime(Date arrivalTime) {
        this.arrivalTime = arrivalTime;
    }


    /**
     * @return the stationName
     */
    public String getStationName() {
        return stationName;
    }

    /**
     * @param stationName the stationName to set
     */
    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    /**
     * @return the place
     */
    public int getPlace() {
        return place;
    }

    /**
     * @param place the place to set
     */
    public void setPlace(int place) {
        this.place = place;
    }

    /**
     * @return the delay
     */
    public int getDelay() {
        return delay;
    }

    /**
     * @param delay the delay to set
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

    /**
     * @return the stopNo
     */
    public int getStopNo() {
        return stopNo;
    }

    /**
     * @param stopNo the stopNo to set
     */
    public void setStopNo(int stopNo) {
        this.stopNo = stopNo;
    }

    /**
     * @return the station
     */
    public Station getStation() {
        return station;
    }

    /**
     * @param station the station to set
     */
    public void setStation(Station station) {
        this.station = station;
    }
    
    
}
