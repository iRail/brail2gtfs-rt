package com.opentransport.rdfmapper.nmbs.containers;

import java.util.List;

/**
 *
 * @author Nicola De Clercq
 */
public class LiveBoard {
    
    private StationInfo stationInfo;
    private List<Service> services;
    
    public LiveBoard() {}

    public LiveBoard(StationInfo stationInfo, List<Service> services) {
        this.stationInfo = stationInfo;
        this.services = services;
    }

    public StationInfo getStationInfo() {
        return stationInfo;
    }

    public void setStationInfo(StationInfo stationInfo) {
        this.stationInfo = stationInfo;
    }

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(stationInfo);
        for (int i = 0; i < services.size(); i++) {
            sb.append("\n").append(services.get(i));
        }
        return sb.toString();
    }
    
}