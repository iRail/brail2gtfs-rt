package com.opentransport.rdfmapper.nmbs.containers;

/**
 *
 * @author Nicola De Clercq
 */
public class NextStop {
    
    private String name;
    private String scheduledArrivalTime;
    private String delay;
    private String actualArrivalTime;
    private String platform;
    
    public NextStop() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScheduledArrivalTime() {
        return scheduledArrivalTime;
    }

    public void setScheduledArrivalTime(String scheduledArrivalTime) {
        this.scheduledArrivalTime = scheduledArrivalTime;
    }

    public String getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getActualArrivalTime() {
        return actualArrivalTime;
    }

    public void setActualArrivalTime(String actualArrivalTime) {
        this.actualArrivalTime = actualArrivalTime;
    }

    @Override
    public String toString() {
        String plus = "";
        if (delay.matches("[0-9]+")) {
            plus = "+";
        }
        return "(" + name + " " + scheduledArrivalTime + " " + plus + delay + " " + platform + ")";
    }
    
}