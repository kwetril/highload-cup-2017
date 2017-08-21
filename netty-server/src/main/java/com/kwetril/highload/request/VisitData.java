package com.kwetril.highload.request;

public class VisitData {
    public int visitId;
    public int userId;
    public int locationId;
    public int mark;
    public int visitedAt;
    public String json;

    public void computeJson() {
        json = String.format("{\"id\":%s,\"user\":%s,\"location\":%s,\"mark\":%s,\"visited_at\":%s}",
                visitId, userId, locationId, mark, visitedAt);
    }

    @Override
    public String toString() {
        return json;
    }
}
