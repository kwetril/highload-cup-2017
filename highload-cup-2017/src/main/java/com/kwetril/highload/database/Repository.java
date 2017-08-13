package com.kwetril.highload.database;

import com.kwetril.highload.request.LocationData;
import com.kwetril.highload.request.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Repository {
    private static final ArrayList<UserData> userCollection = new ArrayList<>();
    private static final HashMap<Integer, Integer> userIdToIdx = new HashMap<>();
    private static final AtomicInteger nextUserIdx = new AtomicInteger(0);

    private static final ArrayList<LocationData> locationCollection = new ArrayList<>();
    private static final HashMap<Integer, Integer> locationIdToIdx = new HashMap<>();
    private static final AtomicInteger nextLocationIdx = new AtomicInteger(0);

    public static void addUser(UserData user) {
        int currentIdx = nextUserIdx.getAndAdd(1);
        userIdToIdx.put(user.userId, currentIdx);
        userCollection.add(user);
    }

    public static String getUser(int userId) {
        Integer index = userIdToIdx.get(userId);
        if (index != null) {
            return userCollection.get(index).toString();
        } else {
            return null;
        }
    }

    public static int countUsers() {
        return nextUserIdx.get();
    }

    public static boolean editUser(UserData update) {
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
            if (update.birthDate != 0) {
                user.birthDate = update.birthDate;
            }
            return true;
        } else {
            return false;
        }
    }

    public static void addLocation(LocationData location) {
        int currentIdx = nextLocationIdx.getAndAdd(1);
        locationIdToIdx.put(location.locationId, currentIdx);
        locationCollection.add(location);
    }

    public static String getLocation(int locationId) {
        Integer index = locationIdToIdx.get(locationId);
        if (index != null) {
            return locationCollection.get(index).toString();
        } else {
            return null;
        }
    }

    public static int countLocations() {
        return nextLocationIdx.get();
    }

    public static boolean editLocation(LocationData update) {
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
            if (update.distance != 0) {
                location.distance = update.distance;
            }
            return true;
        } else {
            return false;
        }
    }
}
