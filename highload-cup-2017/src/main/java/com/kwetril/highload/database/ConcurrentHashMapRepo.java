package com.kwetril.highload.database;

import com.kwetril.highload.request.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentHashMapRepo implements IRepository {
    private final ArrayList<UserData> userCollection = new ArrayList<>(1000000);
    private final HashMap<Integer, Integer> userIdToIdx = new HashMap<>();
    private final AtomicInteger nextUserIdx = new AtomicInteger(0);

    private final ArrayList<LocationData> locationCollection = new ArrayList<>(1000000);
    private final HashMap<Integer, Integer> locationIdToIdx = new HashMap<>();
    private final AtomicInteger nextLocationIdx = new AtomicInteger(0);

    private final ArrayList<VisitData> visitCollection = new ArrayList<>(10000000);
    private final HashMap<Integer, Integer> visitIdToIdx = new HashMap<>();
    private final AtomicInteger nextVisitIdx = new AtomicInteger(0);

    public void addUser(UserData user) {
        int currentIdx = nextUserIdx.getAndAdd(1);
        userCollection.add(user);
        userCollection.set(currentIdx, user);
        userIdToIdx.put(user.userId, currentIdx);
    }

    public String getUser(int userId) {
        Integer index = userIdToIdx.get(userId);
        if (index != null) {
            return userCollection.get(index).toString();
        } else {
            return null;
        }
    }

    public int countUsers() {
        return nextUserIdx.get();
    }

    public boolean editUser(UserUpdate update) {
        Integer index = userIdToIdx.get(update.userId);
        if (index != null) {
            UserData user = userCollection.get(index);
            if (update.firstName != null) {
                user.firstName = update.firstName;
            }
            if (update.lastName != null) {
                user.lastName = update.lastName;
            }
            if (update.gender != null) {
                user.gender = update.gender;
            }
            if (update.email != null) {
                user.email = update.email;
            }
            if (update.isBirthDateUpdated) {
                user.birthDate = update.birthDate;
            }
            return true;
        } else {
            return false;
        }
    }

    public void addLocation(LocationData location) {
        int currentIdx = nextLocationIdx.getAndAdd(1);
        locationIdToIdx.put(location.locationId, currentIdx);
        locationCollection.add(location);
    }

    public String getLocation(int locationId) {
        Integer index = locationIdToIdx.get(locationId);
        if (index != null) {
            return locationCollection.get(index).toString();
        } else {
            return null;
        }
    }

    public int countLocations() {
        return nextLocationIdx.get();
    }

    public boolean editLocation(LocationUpdate update) {
        Integer index = locationIdToIdx.get(update.locationId);
        if (index != null) {
            LocationData location = locationCollection.get(index);
            if (update.country != null) {
                location.country = update.country;
            }
            if (update.city != null) {
                location.city = update.city;
            }
            if (update.place != null) {
                location.place = update.place;
            }
            if (update.isDistanceUpdated) {
                location.distance = update.distance;
            }
            return true;
        } else {
            return false;
        }
    }

    public void addVisit(VisitData visit) {
        int currentIdx = nextVisitIdx.getAndAdd(1);
        visitIdToIdx.put(visit.visitId, currentIdx);
        visitCollection.add(visit);
    }

    public String getVisit(int visitId) {
        Integer index = visitIdToIdx.get(visitId);
        if (index != null) {
            return visitCollection.get(index).toString();
        } else {
            return null;
        }
    }

    public int countVisits() {
        return nextVisitIdx.get();
    }

    public boolean editVisit(VisitUpdate update) {
        Integer index = visitIdToIdx.get(update.visitId);
        if (index != null) {
            VisitData visit = visitCollection.get(index);
            if (update.isUserUpdated) {
                visit.userId = update.userId;
            }
            if (update.isLocationUpdated) {
                visit.locationId = update.locationId;
            }
            if (update.isMarkUpdated) {
                visit.mark = update.mark;
            }
            if (update.isVisitedAtUpdated) {
                visit.visitedAt = update.visitedAt;
            }
            return true;
        } else {
            return false;
        }
    }
}
