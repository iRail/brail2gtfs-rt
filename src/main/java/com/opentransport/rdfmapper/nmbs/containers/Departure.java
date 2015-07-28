package com.opentransport.rdfmapper.nmbs.containers;

/**
 *
 * @author Nicola De Clercq
 */
public class Departure {
    
    private String departureId;
    private String departureStationId;
    private String scheduledDepartureTime;
    private String departureDelay;
    private String actualDepartureTime;
    private String departurePlatform;
    private String arrivalStationId;
    private String scheduledArrivalTime;
    private String arrivalDelay;
    private String actualArrivalTime;
    private String arrivalPlatform;
    private String route;
    private String headsign;

    public Departure(String departureId, String departureStationId, String scheduledDepartureTime, String departureDelay, String actualDepartureTime, String departurePlatform, String arrivalStationId, String scheduledArrivalTime, String arrivalDelay, String actualArrivalTime, String arrivalPlatform, String route, String headsign) {
        this.departureId = departureId;
        this.departureStationId = departureStationId;
        this.scheduledDepartureTime = scheduledDepartureTime;
        this.departureDelay = departureDelay;
        this.actualDepartureTime = actualDepartureTime;
        this.departurePlatform = departurePlatform;
        this.arrivalStationId = arrivalStationId;
        this.scheduledArrivalTime = scheduledArrivalTime;
        this.arrivalDelay = arrivalDelay;
        this.actualArrivalTime = actualArrivalTime;
        this.arrivalPlatform = arrivalPlatform;
        this.route = route;
        this.headsign = headsign;
    }

    public String getDepartureId() {
        return departureId;
    }

    public void setDepartureId(String departureId) {
        this.departureId = departureId;
    }

    public String getDepartureStationId() {
        return departureStationId;
    }

    public void setDepartureStationId(String departureStationId) {
        this.departureStationId = departureStationId;
    }

    public String getScheduledDepartureTime() {
        return scheduledDepartureTime;
    }

    public void setScheduledDepartureTime(String scheduledDepartureTime) {
        this.scheduledDepartureTime = scheduledDepartureTime;
    }

    public String getDepartureDelay() {
        return departureDelay;
    }

    public void setDepartureDelay(String departureDelay) {
        this.departureDelay = departureDelay;
    }

    public String getActualDepartureTime() {
        return actualDepartureTime;
    }

    public void setActualDepartureTime(String actualDepartureTime) {
        this.actualDepartureTime = actualDepartureTime;
    }

    public String getDeparturePlatform() {
        return departurePlatform;
    }

    public void setDeparturePlatform(String departurePlatform) {
        this.departurePlatform = departurePlatform;
    }

    public String getArrivalStationId() {
        return arrivalStationId;
    }

    public void setArrivalStationId(String arrivalStationId) {
        this.arrivalStationId = arrivalStationId;
    }

    public String getScheduledArrivalTime() {
        return scheduledArrivalTime;
    }

    public void setScheduledArrivalTime(String scheduledArrivalTime) {
        this.scheduledArrivalTime = scheduledArrivalTime;
    }

    public String getArrivalDelay() {
        return arrivalDelay;
    }

    public void setArrivalDelay(String arrivalDelay) {
        this.arrivalDelay = arrivalDelay;
    }

    public String getActualArrivalTime() {
        return actualArrivalTime;
    }

    public void setActualArrivalTime(String actualArrivalTime) {
        this.actualArrivalTime = actualArrivalTime;
    }

    public String getArrivalPlatform() {
        return arrivalPlatform;
    }

    public void setArrivalPlatform(String arrivalPlatform) {
        this.arrivalPlatform = arrivalPlatform;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getHeadsign() {
        return headsign;
    }

    public void setHeadsign(String headsign) {
        this.headsign = headsign;
    }

    @Override
    public String toString() {
        return "Departure{" + "departureId=" + departureId + ", departureStationId=" + departureStationId + ", scheduledDepartureTime=" + scheduledDepartureTime + ", departureDelay=" + departureDelay + ", actualDepartureTime=" + actualDepartureTime + ", departurePlatform=" + departurePlatform + ", arrivalStationId=" + arrivalStationId + ", scheduledArrivalTime=" + scheduledArrivalTime + ", arrivalDelay=" + arrivalDelay + ", actualArrivalTime=" + actualArrivalTime + ", arrivalPlatform=" + arrivalPlatform + ", route=" + route + ", headsign=" + headsign + '}';
    }
    
}