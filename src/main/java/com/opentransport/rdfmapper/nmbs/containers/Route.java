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
public class Route {

    private String route_id;
    private String agency_id;
    private String route_short_name;
    private String route_long_name;
    private String route_type;

    public Route(String route_id, String agency_id, String route_short_name, String route_long_name, String route_type) {
        this.route_id = route_id;
        this.agency_id = agency_id;
        this.route_short_name = route_short_name;
        this.route_long_name = route_long_name;
        this.route_type = route_type;
    }

    /**
     * @return the route_id
     */
    public String getRoute_id() {
        return route_id;
    }

    /**
     * @return the agency_id
     */
    public String getAgency_id() {
        return agency_id;
    }

    /**
     * @return the route_short_name
     */
    public String getRoute_short_name() {
        return route_short_name;
    }

    /**
     * @return the route_long_name
     */
    public String getRoute_long_name() {
        return route_long_name;
    }

    /**
     * @return the route_type
     */
    public String getRoute_type() {
        return route_type;
    }
}
