package com.kwetril.highload;

import com.kwetril.highload.parsing.RequestParser;
import com.kwetril.highload.request.UserData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HighLoadCup2017 {
    private static ArrayList<UserData> userDb = new ArrayList<UserData>();
    private static HashMap<Integer, Integer> idToIndex = new HashMap<Integer, Integer>();
    private static AtomicInteger nextIdx = new AtomicInteger(0);

    private static void initDb() throws Exception {
        String content = new String(Files.readAllBytes(Paths.get("data", "users_1.json")));
        JSONObject usersObject = new JSONObject(content);
        JSONArray users = usersObject.getJSONArray("users");
        for (int i = 0; i < users.length(); i++) {
            JSONObject jsonObject = users.getJSONObject(i);
            String userJson = jsonObject.toString();
            if (!newUser(userJson)) {
                throw new Exception(String.format("Couldn't parse %s", userJson));
            }
        }
        System.out.println(String.format("Users added: %s", nextIdx.get()));
    }

    private static boolean newUser(String userJson) {
        UserData user = RequestParser.parseNewUser(userJson);
        if (user == null) {
            return false;
        } else {
            int currentIdx = nextIdx.getAndAdd(1);
            idToIndex.put(user.userId, currentIdx);
            userDb.add(user);
            return true;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Hello world");
        initDb();
    }
}
