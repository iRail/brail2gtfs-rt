/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs.containers;

import java.util.List;

/**
 *
 * @author timtijssens
 */
public class LiveRoute {
    
    private String route_url;
    private int stop_Number;
    private String vehicle_no;
    private List<TrainStop> TrainStops ;

    
    public LiveRoute(String route_url, int stop_number, String vehicle_no,List<TrainStop> stops){
        
    this.route_url=route_url;
    this.stop_Number = stop_number;
    this.vehicle_no = vehicle_no;
    this.TrainStops = stops;
    
    }
    /**
     * @return the route_url
     */
    public String getRoute_url() {
        return route_url;
    }

    /**
     * @param route_url the route_url to set
     */
    public void setRoute_url(String route_url) {
        this.route_url = route_url;
    }

    /**
     * @return the stop_Number
     */
    public int getStop_Number() {
        return stop_Number;
    }

    /**
     * @param stop_Number the stop_Number to set
     */
    public void setStop_Number(int stop_Number) {
        this.stop_Number = stop_Number;
    }

    /**
     * @return the vehicle_no
     */
    public String getVehicle_no() {
        return vehicle_no;
    }

    /**
     * @param vehicle_no the vehicle_no to set
     */
    public void setVehicle_no(String vehicle_no) {
        this.vehicle_no = vehicle_no;
    }

    /**
     * @return the TrainStops
     */
    public List<TrainStop> getTrainStops() {
        return TrainStops;
    }

    /**
     * @param TrainStops the TrainStops to set
     */
    public void setTrainStops(List<TrainStop> TrainStops) {
        this.TrainStops = TrainStops;
    }
    
    
    
    
    
    
}
