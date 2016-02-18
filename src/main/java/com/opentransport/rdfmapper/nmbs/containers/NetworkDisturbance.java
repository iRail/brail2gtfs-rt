/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs.containers;

public class NetworkDisturbance {
    
    private String headerText;
    private String descriptionText;
    private String url;
    private String language;
    private long start; // Unix Epoch
    private long end; // Unix Epoch
    private int startStationId;
    private int endStationId;
    private int impactStationId;
    private String id;

    public NetworkDisturbance(String headerText, String descriptionText, String url, String language, long start, long end, int startStationId, int endStationId, int impactStationId, String id) {
        this.headerText = headerText;
        this.descriptionText = descriptionText;
        this.url = url;
        this.language = language;
        this.start = start;
        this.end = end;
        this.startStationId = startStationId;
        this.endStationId = endStationId;
        this.impactStationId = impactStationId;
        this.id = id;
    }

    public String getHeaderText() {
        return headerText;
    }

    public void setHeaderText(String headerText) {
        this.headerText = headerText;
    }

    public String getDescriptionText() {
        return descriptionText;
    }

    public void setDescriptionText(String descriptionText) {
        this.descriptionText = descriptionText;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public int getStartStationId() {
        return startStationId;
    }

    public void setStartStationId(int startStationId) {
        this.startStationId = startStationId;
    }

    public int getEndStationId() {
        return endStationId;
    }

    public void setEndStationId(int endStationId) {
        this.endStationId = endStationId;
    }
    
    public int getImpactStationId() {
        return impactStationId;
    }

    public void setImpactStationId(int impactStationId) {
        this.impactStationId = impactStationId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }    
}