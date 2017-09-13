package com.kwetril.highload.model;

public class UserVisitData {
    public int mark;
    public long visitedAt;
    public String place;

    @Override
    public String toString() {
        return String.format("{\"mark\":%d,\"visited_at\":%d,\"place\":\"%s\"}", mark, visitedAt, place);
    }
}
