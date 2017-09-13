package com.kwetril.highload.model;

public class VisitData {
    public int visitId;
    public int userId;
    public int locationId;
    public int mark;
    public int visitedAt;

    @Override
    public String toString() {
        return String.format("{\"id\":%s,\"user\":%s,\"location\":%s,\"mark\":%s,\"visited_at\":%s}",
                visitId, userId, locationId, mark, visitedAt);
    }
}
