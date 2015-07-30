/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs.containers;

/**
 *
 * @author timtijssens
 */
public class RoutesInfoTemp {
    //route_short_name,service_id,date
    private String route_short_name;
    private String service_id;
    private String date;
    
    
    public RoutesInfoTemp(String route_short_name, String service_id, String date){
        this.route_short_name = route_short_name;
        this.service_id = service_id;
        this.date=date;    
    }

    /**
     * @return the route_short_name
     */
    
    public String getTrip_Id(){
        return route_short_name + service_id + 1;
    
    }
    public String getRoute_short_name() {
        return route_short_name;
    }

    /**
     * @param route_short_name the route_short_name to set
     */
    public void setRoute_short_name(String route_short_name) {
        this.route_short_name = route_short_name;
    }

    /**
     * @return the service_id
     */
    public String getService_id() {
        return service_id;
    }

    /**
     * @param service_id the service_id to set
     */
    public void setService_id(String service_id) {
        this.service_id = service_id;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }
    
            
    
}
