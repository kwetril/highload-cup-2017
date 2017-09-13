package com.kwetril.highload;

import com.kwetril.highload.database.RepositoryProvider;
import com.kwetril.highload.database.TimestampProvider;
import com.kwetril.highload.parsing.RequestParser;
import com.kwetril.highload.model.LocationData;
import com.kwetril.highload.model.UserData;
import com.kwetril.highload.model.VisitData;
import com.kwetril.highload.server.HigloadCupServer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Main {
    private static ArrayList<String> getCollection(String content) {
        ArrayList<String> res = new ArrayList<>();
        int firstStartBreaket = content.indexOf('{');
        int secondStartBreaket = content.indexOf('{', firstStartBreaket + 1);
        int i = secondStartBreaket;
        while (i != -1) {
            int start = i;
            int end = content.indexOf('}', i);
            res.add(content.substring(start, end + 1));
            i = content.indexOf('{', end);
        }
        return res;
    }


    private static void initDb() throws Exception {
        try {
            ZipFile zipFile = new ZipFile(DATA_PATH);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            ArrayList<ZipEntry> x = new ArrayList<>();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                x.add(entry);
            }
            x.sort((a, b) -> {
                String aType = a.getName().split("_")[0];
                int aNum = Integer.parseInt(a.getName().split("_")[1].split("\\.")[0]);
                String bType = b.getName().split("_")[0];
                int bNum = Integer.parseInt(b.getName().split("_")[1].split("\\.")[0]);
                int res = 0;
                if (aType.equals(bType)) {
                    res = Integer.compare(aNum, bNum);
                } else if (aType.equals("users")) {
                    res = -1;
                } else if (bType.equals("users")) {
                    res = 1;
                } else if (aType.equals("locations")) {
                    res = -1;
                } else if (bType.equals("locations")) {
                    res = 1;
                }
                return res;
            });
            for (ZipEntry entry : x) {
                System.out.println(String.format("Processing entry: %s", entry.getName()));
                InputStream stream = zipFile.getInputStream(entry);
                try (BufferedReader buffer = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
                    String content = buffer.lines().collect(Collectors.joining("\n"));
                    switch (entry.getName().split("_")[0]) {
                        case "users":
                            ArrayList<String> users = getCollection(content);
                            for (int i = 0; i < users.size(); i++) {
                                String userJson = users.get(i);
                                UserData user = RequestParser.parseNewUser(userJson);
                                if (user != null) {
                                    RepositoryProvider.repo.addUser(user);
                                } else {
                                    throw new Exception(String.format("Couldn't parse %s", userJson));
                                }
                            }
                            System.out.println(String.format("Users added: %s", RepositoryProvider.repo.countUsers()));
                            break;
                        case "locations":
                            ArrayList<String> locations = getCollection(content);
                            for (int i = 0; i < locations.size(); i++) {
                                String jsonObject = locations.get(i);
                                String locationJson = jsonObject.toString();
                                LocationData location = RequestParser.parseNewLocation(locationJson);
                                if (location != null) {
                                    RepositoryProvider.repo.addLocation(location);
                                } else {
                                    throw new Exception(String.format("Couldn't parse %s", locationJson));
                                }
                            }
                            System.out.println(String.format("Locations added: %s", RepositoryProvider.repo.countLocations()));
                            break;
                        case "visits":
                            ArrayList<String> visits = getCollection(content);
                            for (int i = 0; i < visits.size(); i++) {
                                String visitJson = visits.get(i);
                                VisitData visit = RequestParser.parseNewVisit(visitJson);
                                if (visit != null) {
                                    RepositoryProvider.repo.addVisit(visit);
                                } else {
                                    throw new Exception(String.format("Couldn't parse %s", visitJson));
                                }
                            }
                            System.out.println(String.format("Visists added: %s", RepositoryProvider.repo.countVisits()));
                            break;
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        try {
            TimestampProvider.timestamp = Long.parseLong(new String(Files.readAllBytes(Paths.get("/tmp/data/options.txt"))).split("\n")[0]);
            System.out.println(String.format("Timestamp %s", TimestampProvider.timestamp));
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static int PORT = 808;
    private static String DATA_PATH = "data.zip";

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            PORT = 80;
            DATA_PATH = "/tmp/data/data.zip";
        }
        Locale.setDefault(Locale.US);
        initDb();
        Thread serverThread = new Thread(() -> {
            try {
                new HigloadCupServer(PORT).run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.start();
        System.gc();
        System.out.println(String.format("Total: %s; Free: %s", Runtime.getRuntime().totalMemory() / 1024. / 1024,
                Runtime.getRuntime().freeMemory() / 1024. / 1024));
        serverThread.join();
    }
}
