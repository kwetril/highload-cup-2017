package com.kwetril.highload.request;

public class LocationData {
    public int locationId;
    public int distance;
    public String city;
    public String country;
    public String place;

    @Override
    public String toString() {
        return String.format("{\"id\":%s,\"country\":\"%s\",\"city\":\"%s\",\"place\":\"%s\",\"distance\":%s}",
                locationId, country, city, place, distance);
    }
}
