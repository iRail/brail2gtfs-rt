package com.opentransport.rdfmapper.nmbs.containers;

import java.util.List;

/**
 *
 * @author Nicola De Clercq
 */
public class TrainInfo {
    
    private String trainNumber;
    private String destination;
    private List<Stop> stops;
    
    public TrainInfo() {}
    
    public TrainInfo(String trainNumber, String destination, List<Stop> stops) {
        this.trainNumber = trainNumber;
        this.destination = destination;
        this.stops = stops;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public List<Stop> getStops() {
        return stops;
    }

    public void setStops(List<Stop> stops) {
        this.stops = stops;
    }
    
}