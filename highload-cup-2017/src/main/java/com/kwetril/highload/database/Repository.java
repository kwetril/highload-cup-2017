package com.kwetril.highload.database;

import com.kwetril.highload.parsing.RequestParser;
import com.kwetril.highload.request.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Repository {
    private static final ArrayList<UserData> userDb = new ArrayList<>();
    private static final HashMap<Integer, Integer> idToIndex = new HashMap<>();
    private static final AtomicInteger nextIdx = new AtomicInteger(0);

    public static void addUser(UserData user) {
        int currentIdx = nextIdx.getAndAdd(1);
        idToIndex.put(user.userId, currentIdx);
        userDb.add(user);
    }

    public static String getUser(int userId) {
        Integer index = idToIndex.get(userId);
        if (index != null) {
            return userDb.get(index).toString();
        } else {
            return null;
        }
    }

    public static int numUsers() {
        return nextIdx.get();
    }

    public static boolean editUser(UserData update) {
        Integer index = idToIndex.get(update.userId);
        if (index != null) {
            UserData user = userDb.get(index);
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
}
