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
public class CalendarDate {
    
    private String service_id;
    private String date;
    private String exception_type;

    /**
     * @return the service_id
     */
    
    public CalendarDate (String service_id, String date,String exception_type){
        this.service_id = service_id;
        this.date = date;
        this.exception_type = exception_type;
    
    
    }
    public String getService_id() {
        return service_id;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @return the exception_type
     */
    public String getException_type() {
        return exception_type;
    }   
}
