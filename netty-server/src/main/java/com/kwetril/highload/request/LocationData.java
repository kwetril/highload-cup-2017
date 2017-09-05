package com.kwetril.highload.request;

public class LocationData {
    public int locationId;
    public int distance;
    public String city;
    public String country;
    public String place;
    public String json;


    public void computeJson() {
        json = String.format("{\"id\":%s,\"country\":\"%s\",\"city\":\"%s\",\"place\":\"%s\",\"distance\":%s}",
                locationId, country, city, place, distance);
    }

    @Override
    public String toString() {
        return json;
        //return String.format("{\"id\":%s,\"country\":\"%s\",\"city\":\"%s\",\"place\":\"%s\",\"distance\":%s}",
        //        locationId, country, city, place, distance);
    }
}
