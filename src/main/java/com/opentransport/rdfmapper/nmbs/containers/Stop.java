package com.opentransport.rdfmapper.nmbs.containers;

/**
 *
 * @author Nicola De Clercq
 */
public class Stop {
    
    private String name;
    private String platform;
    private String arrivalTime;
    private String arrivalDelay;
    private String departureTime;
    private String departureDelay;
    
    public Stop() {}
    
    public Stop(String name, String platform, String arrivalTime, String arrivalDelay, String departureTime, String departureDelay) {
        this.name = name;
        this.platform = platform;
        this.arrivalTime = arrivalTime;
        this.arrivalDelay = arrivalDelay;
        this.departureTime = departureTime;
        this.departureDelay = departureDelay;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getArrivalDelay() {
        return arrivalDelay;
    }

    public void setArrivalDelay(String arrivalDelay) {
        this.arrivalDelay = arrivalDelay;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getDepartureDelay() {
        return departureDelay;
    }

    public void setDepartureDelay(String departureDelay) {
        this.departureDelay = departureDelay;
    }
    
}