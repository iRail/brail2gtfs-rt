package com.opentransport.rdfmapper.nmbs.containers;

/**
 *
 * @author Nicola De Clercq
 */
public class Service {
    
    private String destination;
    private String scheduledDepartureTime;
    private String delay;
    private String actualDepartureTime;
    private String trainNumber;
    private String platform;
    private NextStop nextStop;
    
    public Service() {}

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getScheduledDepartureTime() {
        return scheduledDepartureTime;
    }

    public void setScheduledDepartureTime(String scheduledDepartureTime) {
        this.scheduledDepartureTime = scheduledDepartureTime;
    }

    public String getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public NextStop getNextStop() {
        return nextStop;
    }

    public void setNextStop(NextStop nextStop) {
        this.nextStop = nextStop;
    }

    public String getActualDepartureTime() {
        return actualDepartureTime;
    }

    public void setActualDepartureTime(String actualDepartureTime) {
        this.actualDepartureTime = actualDepartureTime;
    }

    @Override
    public String toString() {
        String plus = "";
        if (delay.matches("[0-9]+")) {
            plus = "+";
        }
        return scheduledDepartureTime + " " + plus + delay + "  \t" + destination + "\t"
                + trainNumber + "\t" + platform + "\t" + nextStop;
    }
    
}