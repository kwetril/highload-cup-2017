package com.kwetril.highload.database;

import com.kwetril.highload.request.*;

import java.util.ArrayList;

public interface IRepository {
    boolean addUser(UserData user);

    UserData getUser(int userId);

    Iterable<Integer> getUserIds();

    int countUsers();

    boolean editUser(UserUpdate update);

    boolean addLocation(LocationData location);

    LocationData getLocation(int locationId);

    Iterable<Integer> getLocationIds();

    int countLocations();

    boolean editLocation(LocationUpdate update);

    boolean addVisit(VisitData visit);

    VisitData getVisit(int visitId);

    Iterable<Integer> getVisitIds();

    int countVisits();

    boolean editVisit(VisitUpdate update);

    ArrayList<UserVisitData> getUserVisits(int userId,
                                           boolean hasFromDate, long fromData,
                                           boolean hasToDate, long toDate,
                                           String country,
                                           boolean hasDistance, int distance);

    double getLocationMark(int locationId,
                           boolean hasFromDate, long fromDate,
                           boolean hasToDate, long toDate,
                           boolean hasFromAge, long fromAge,
                           boolean hasToAge, long toAge,
                           String gender);
}
