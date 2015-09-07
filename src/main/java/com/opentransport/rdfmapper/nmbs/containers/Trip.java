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
public class Trip {

    private String route_id;
    private String service_id;
    private String trip_id;

    public Trip(String route_id, String service_id, String trip_id) {
        this.route_id = route_id;
        this.service_id = service_id;
        this.trip_id = trip_id;
    }

    /**
     * @return the route_id
     */
    public String getRoute_id() {
        return route_id;
    }

    /**
     * @return the service_id
     */
    public String getService_id() {
        return service_id;
    }

    /**
     * @return the trip_id
     */
    public String getTrip_id() {
        return trip_id;
    }
}
