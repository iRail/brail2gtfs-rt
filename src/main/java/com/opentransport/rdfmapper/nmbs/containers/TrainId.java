package com.opentransport.rdfmapper.nmbs.containers;

import java.util.Objects;

/**
 *
 * @author Nicola De Clercq
 */
public class TrainId {
    
    private String trainNumber;
    private String destination;
    
    public TrainId() {}

    public TrainId(String trainNumber, String destination) {
        this.trainNumber = trainNumber;
        this.destination = destination;
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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + Objects.hashCode(this.trainNumber);
        hash = 31 * hash + Objects.hashCode(this.destination);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TrainId other = (TrainId) obj;
        if (!Objects.equals(this.trainNumber, other.trainNumber)) {
            return false;
        }
        if (!Objects.equals(this.destination, other.destination)) {
            return false;
        }
        return true;
    }
    
}