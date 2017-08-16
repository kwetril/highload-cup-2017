package com.kwetril.highload.database;

import com.kwetril.highload.request.*;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapRepo implements IRepository {
    private final ConcurrentHashMap<Integer, UserData> userCollection = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, LocationData> locationCollection = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, VisitData> visitCollection = new ConcurrentHashMap<>();

    private final Object visitUpdateLock = new Object();
    private final ConcurrentHashMap<Integer, Set<Integer>> userVisitsCollection = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Set<Integer> > locationVisitsCollection = new ConcurrentHashMap<>();

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

    public UserData getUser(int userId) {
        return userCollection.get(userId);
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

    public LocationData getLocation(int locationId) {
        return locationCollection.get(locationId);
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
        synchronized (visitUpdateLock) {
            VisitData existingVisit = visitCollection.get(visit.userId);
            if (existingVisit == null) {
                visitCollection.put(visit.visitId, visit);
                if (getUser(visit.userId) != null && getLocation(visit.locationId) != null) {
                    Set<Integer> userVisits = userVisitsCollection.get(visit.userId);
                    if (userVisits == null) {
                        userVisits = ConcurrentHashMap.newKeySet();
                        userVisitsCollection.put(visit.userId, userVisits);
                    }
                    userVisits.add(visit.visitId);

                    Set<Integer> locationVisists = locationVisitsCollection.get(visit.locationId);
                    if (locationVisists == null) {
                        locationVisists = ConcurrentHashMap.newKeySet();
                        locationVisitsCollection.put(visit.locationId, locationVisists);
                    }
                    locationVisists.add(visit.visitId);
                }
                return true;
            }
            return false;
        }
    }

    public VisitData getVisit(int visitId) {
        return visitCollection.get(visitId);
    }

    public int countVisits() {
        return visitCollection.size();
    }

    public boolean editVisit(VisitUpdate update) {
        synchronized (visitUpdateLock) {
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

    public Iterable<UserVisitData> getUserVisits(int userId, boolean hasFromDate, long fromData,
                                                 boolean hasToDate, long toDate, String country,
                                                 boolean hasDistance, int distance) {
        UserData user = getUser(userId);
        if (user == null) {
            return null;
        }
        Set<Integer> userVisitsIds = userVisitsCollection.get(userId);
        ArrayList<UserVisitData> results = new ArrayList<>();
        if (userVisitsIds != null) {
            for (Integer visitId : userVisitsIds) {
                VisitData visit = getVisit(visitId);
                LocationData location = getLocation(visit.locationId);
                boolean filteringCondition = true;
                if (filteringCondition) {
                    UserVisitData userVisit = new UserVisitData();
                    userVisit.mark = visit.mark;
                    userVisit.visitedAt = visit.visitedAt;
                    userVisit.place = location.place;
                    results.add(userVisit);
                }
            }
        }
        return results;
    }
}
