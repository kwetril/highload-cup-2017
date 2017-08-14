package com.kwetril.highload.database;

import com.kwetril.highload.request.*;

import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapRepo implements IRepository {
    private final ConcurrentHashMap<Integer, UserData> userCollection = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, LocationData> locationCollection = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, VisitData> visitCollection = new ConcurrentHashMap<>();

    public boolean addUser(UserData user) {
        UserData existingUser = userCollection.get(user.userId);
        if (existingUser == null) {
            UserData oldValue = userCollection.putIfAbsent(user.userId, user);
            if (oldValue == null) {
                return true;
            }
        }
        return false;
    }

    public String getUser(int userId) {
        UserData user = userCollection.get(userId);
        if (user != null) {
            return user.toString();
        } else {
            return null;
        }
    }

    public int countUsers() {
        return userCollection.size();
    }

    public boolean editUser(UserUpdate update) {
        UserData user = userCollection.get(update.userId);
        if (user != null) {
            userCollection.merge(update.userId, update, (oldRecord, newRec) -> {
                UserUpdate newRecord = (UserUpdate) newRec;
                if (newRecord.firstName != null) {
                    oldRecord.firstName = newRecord.firstName;
                }
                if (newRecord.lastName != null) {
                    oldRecord.lastName = newRecord.lastName;
                }
                if (newRecord.gender != null) {
                    oldRecord.gender = newRecord.gender;
                }
                if (newRecord.email != null) {
                    oldRecord.email = newRecord.email;
                }
                if (newRecord.isBirthDateUpdated) {
                    oldRecord.birthDate = newRecord.birthDate;
                }
                return oldRecord;
            });
            return true;
        } else {
            return false;
        }
    }

    public boolean addLocation(LocationData location) {
        LocationData existingLocation = locationCollection.get(location.locationId);
        if (existingLocation == null) {
            LocationData oldValue = locationCollection.putIfAbsent(location.locationId, location);
            if (oldValue == null) {
                return true;
            }
        }
        return false;
    }

    public String getLocation(int locationId) {
        LocationData location = locationCollection.get(locationId);
        if (location != null) {
            return location.toString();
        } else {
            return null;
        }
    }

    public int countLocations() {
        return locationCollection.size();
    }

    public boolean editLocation(LocationUpdate update) {
        LocationData location = locationCollection.get(update.locationId);
        if (location != null) {
            locationCollection.merge(update.locationId, update, (oldRecord, newRec) -> {
                LocationUpdate newRecord = (LocationUpdate) newRec;
                if (newRecord.country != null) {
                    oldRecord.country = newRecord.country;
                }
                if (newRecord.city != null) {
                    oldRecord.city = newRecord.city;
                }
                if (newRecord.place != null) {
                    oldRecord.place = newRecord.place;
                }
                if (newRecord.isDistanceUpdated) {
                    oldRecord.distance = newRecord.distance;
                }
                return oldRecord;
            });
            return true;
        } else {
            return false;
        }
    }

    public boolean addVisit(VisitData visit) {
        VisitData existingVisit = visitCollection.get(visit.userId);
        if (existingVisit == null) {
            VisitData oldValue = visitCollection.putIfAbsent(visit.visitId, visit);
            if (oldValue == null) {
                return true;
            }
        }
        return false;
    }

    public String getVisit(int visitId) {
        VisitData visit = visitCollection.get(visitId);
        if (visit != null) {
            return visit.toString();
        } else {
            return null;
        }
    }

    public int countVisits() {
        return visitCollection.size();
    }

    public boolean editVisit(VisitUpdate update) {
        VisitData visit = visitCollection.get(update.visitId);
        if (visit != null) {
            visitCollection.merge(update.visitId, update, (oldRecord, newRec) -> {
                VisitUpdate newRecord = (VisitUpdate) newRec;
                if (newRecord.isUserUpdated) {
                    oldRecord.userId = newRecord.userId;
                }
                if (newRecord.isLocationUpdated) {
                    oldRecord.locationId = newRecord.locationId;
                }
                if (newRecord.isMarkUpdated) {
                    oldRecord.mark = newRecord.mark;
                }
                if (newRecord.isVisitedAtUpdated) {
                    oldRecord.visitedAt = newRecord.visitedAt;
                }
                return oldRecord;
            });
            return true;
        } else {
            return false;
        }
    }
}
