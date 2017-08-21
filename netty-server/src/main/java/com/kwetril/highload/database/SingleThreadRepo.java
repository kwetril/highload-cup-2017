package com.kwetril.highload.database;

import com.kwetril.highload.request.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleThreadRepo implements IRepository {
    private final int MAX_USERS = 120000;
    private final int MAX_LOCATIONS = 120000;
    private final int MAX_VISITS = 1200000;
    private final UserData[] userCollection = new UserData[MAX_USERS];
    private final LocationData[] locationCollection = new LocationData[MAX_LOCATIONS];
    private final VisitData[] visitCollection = new VisitData[MAX_VISITS];

    private final HashSet<Integer>[] userVisitsCollection = new HashSet[MAX_USERS];
    private final HashSet<Integer>[] locationVisitsCollection = new HashSet[MAX_LOCATIONS];

    private int numUsers = 0;
    private int numLocations = 0;
    private int numVisits = 0;

    public boolean addUser(UserData user) {
        UserData existingUser = userCollection[user.userId];
        if (existingUser == null) {
            userCollection[user.userId] = user;
            user.computeJson();
            numUsers++;
            return true;
        }
        return false;
    }

    public UserData getUser(int userId) {
        if (userId >= MAX_USERS) {
            return null;
        }
        return userCollection[userId];
    }

    public Iterable<Integer> getUserIds() {
        ArrayList<Integer> ids = new ArrayList<>();
        for (int i = 0; i < MAX_USERS; i++) {
            if (userCollection[i] != null) {
                ids.add(i);
            }
        }
        return ids;
    }

    public int countUsers() {
        return numUsers;
    }

    public boolean editUser(UserUpdate update) {
        UserData user = getUser(update.userId);
        if (user != null) {
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
            user.computeJson();
            return true;
        } else {
            return false;
        }
    }

    public boolean addLocation(LocationData location) {
        LocationData existingLocation = locationCollection[location.locationId];
        if (existingLocation == null) {
            locationCollection[location.locationId] = location;
            location.computeJson();
            numLocations++;
            return true;
        }
        return false;
    }

    public LocationData getLocation(int locationId) {
        if (locationId >= MAX_LOCATIONS) {
            return null;
        }
        return locationCollection[locationId];
    }

    public Iterable<Integer> getLocationIds() {
        ArrayList<Integer> ids = new ArrayList<>();
        for (int i = 0; i < MAX_LOCATIONS; i++) {
            if (locationCollection[i] != null) {
                ids.add(i);
            }
        }
        return ids;
    }

    public int countLocations() {
        return numLocations;
    }

    public boolean editLocation(LocationUpdate update) {
        LocationData location = getLocation(update.locationId);
        if (location != null) {
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
            location.computeJson();
            return true;
        } else {
            return false;
        }
    }

    public boolean addVisit(VisitData visit) {
        VisitData existingVisit = visitCollection[visit.visitId];
        if (existingVisit == null
                && getUser(visit.userId) != null
                && getLocation(visit.locationId) != null) {
            visit.computeJson();
            visitCollection[visit.visitId] = visit;
            HashSet<Integer> userVisits = userVisitsCollection[visit.userId];
            if (userVisits == null) {
                userVisits = new HashSet<>();
                userVisitsCollection[visit.userId] = userVisits;
            }
            userVisits.add(visit.visitId);

            HashSet<Integer> locationVisists = locationVisitsCollection[visit.locationId];
            if (locationVisists == null) {
                locationVisists = new HashSet<>();
                locationVisitsCollection[visit.locationId] = locationVisists;
            }
            locationVisists.add(visit.visitId);
            numVisits++;
            return true;
        }
        return false;
    }

    public VisitData getVisit(int visitId) {
        if (visitId >= MAX_VISITS) {
            return null;
        }
        return visitCollection[visitId];
    }

    public Iterable<Integer> getVisitIds() {
        ArrayList<Integer> ids = new ArrayList<>();
        for (int i = 0; i < MAX_VISITS; i++) {
            if (visitCollection[i] != null) {
                ids.add(i);
            }
        }
        return ids;
    }

    public int countVisits() {
        return numVisits;
    }

    public boolean editVisit(VisitUpdate update) {
        VisitData visit = getVisit(update.visitId);
        if (visit != null) {
            if (update.isUserUpdated && getUser(update.userId) == null) {
                return false;
            }
            if (update.isLocationUpdated && getLocation(update.locationId) == null) {
                return false;
            }
            if (update.isUserUpdated) {
                userVisitsCollection[visit.userId].remove(visit.visitId);
                HashSet<Integer> userVisits = userVisitsCollection[update.userId];
                if (userVisits == null) {
                    userVisits = new HashSet<>();
                    userVisitsCollection[update.userId] = userVisits;
                }
                userVisits.add(update.visitId);
            }
            if (update.isLocationUpdated) {
                locationVisitsCollection[visit.locationId].remove(visit.visitId);
                HashSet<Integer> locationVisits = locationVisitsCollection[update.locationId];
                if (locationVisits == null) {
                    locationVisits = new HashSet<>();
                    locationVisitsCollection[update.locationId] = locationVisits;
                }
                locationVisits.add(update.visitId);
            }
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
            visit.computeJson();
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<UserVisitData> getUserVisits(int userId, boolean hasFromDate, long fromDate,
                                                  boolean hasToDate, long toDate, String country,
                                                  boolean hasDistance, int distance) {
        UserData user = getUser(userId);
        if (user == null) {
            return null;
        }
        Set<Integer> userVisitsIds = userVisitsCollection[userId];
        ArrayList<UserVisitData> results = new ArrayList<>();
        if (userVisitsIds != null) {
            for (Integer visitId : userVisitsIds) {
                VisitData visit = getVisit(visitId);
                LocationData location = null;
                boolean filteringCondition;
                filteringCondition = (!hasFromDate || (fromDate < visit.visitedAt))
                        && (!hasToDate || (visit.visitedAt < toDate));
                if (filteringCondition) {
                    location = getLocation(visit.locationId);
                    filteringCondition = (!hasDistance || location.distance < distance)
                            && (country == null || location.country.equals(country));
                }
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

    public double getLocationMark(int locationId, boolean hasFromDate, long fromDate, boolean hasToDate, long toDate, boolean hasFromAge, long fromAge, boolean hasToAge, long toAge, String gender) {
        LocationData location = getLocation(locationId);
        if (location == null) {
            return -1;
        }
        Set<Integer> locationVisitsIds = locationVisitsCollection[locationId];
        int nResults = 0;
        int sumMarks = 0;
        if (locationVisitsIds != null) {
            for (Integer visitId : locationVisitsIds) {
                VisitData visit = getVisit(visitId);
                UserData user;
                boolean filteringCondition;
                filteringCondition = (!hasFromDate || (fromDate < visit.visitedAt))
                        && (!hasToDate || (visit.visitedAt < toDate));
                if (filteringCondition) {
                    user = getUser(visit.userId);
                    filteringCondition = (!hasFromAge || fromAge > user.birthDate)
                            && (!hasToAge || user.birthDate > toAge)
                            && (gender == null || user.gender.charAt(0) == gender.charAt(0));
                }
                if (filteringCondition) {
                    nResults++;
                    sumMarks += visit.mark;
                }
            }
        }
        if (nResults == 0) {
            return 0.0;
        } else {
            return ((double) sumMarks) / nResults;
        }
    }
}
