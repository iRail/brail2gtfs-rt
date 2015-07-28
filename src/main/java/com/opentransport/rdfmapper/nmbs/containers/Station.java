package com.opentransport.rdfmapper.nmbs.containers;

/**
 *
 * @author Nicola De Clercq
 */
public class Station {
    
    private String id;
    private String name;
    private String latitude;
    private String longitude;

    public Station(String id, String name, String latitude, String longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Station{" + "id=" + id + ", name=" + name + ", latitude=" + latitude + ", longitude=" + longitude + '}';
    }

}