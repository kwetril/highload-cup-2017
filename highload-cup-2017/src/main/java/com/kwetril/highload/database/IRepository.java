package com.kwetril.highload.database;

import com.kwetril.highload.request.*;

public interface IRepository {
    boolean addUser(UserData user);

    String getUser(int userId);

    int countUsers();

    boolean editUser(UserUpdate update);

    boolean addLocation(LocationData location);

    String getLocation(int locationId);

    int countLocations();

    boolean editLocation(LocationUpdate update);

    boolean addVisit(VisitData visit);

    String getVisit(int visitId);

    int countVisits();

    boolean editVisit(VisitUpdate update);
}
