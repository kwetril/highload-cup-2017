package com.kwetril.highload.request;

public class UserVisitData {
    public int mark;
    public long visitedAt;
    public String place;

    @Override
    public String toString() {
        return String.format("{\"mark\":%d,\"visitedAt\":%d,\"place\":\"%s\"}", mark, visitedAt, place);
    }
}
