package com.opentransport.rdfmapper.nmbs.containers;

/**
 *
 * @author Nicola De Clercq
 */
public class StationInfo {
    
    private String name;
    private String id;
    private String longitude;
    private String latitude;

    public StationInfo() {}
    
    public StationInfo(String name, String id, String longitude, String latitude) {
        this.name = name;
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    @Override
    public String toString() {
        return name + " (" + longitude + ", " + latitude + ") [" + id + "]";
    }
    
}