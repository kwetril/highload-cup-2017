package com.kwetril.highload;

import com.kwetril.highload.database.RepositoryProvider;
import com.kwetril.highload.database.TimestampProvider;
import com.kwetril.highload.parsing.RequestParser;
import com.kwetril.highload.request.LocationData;
import com.kwetril.highload.request.UserData;
import com.kwetril.highload.request.VisitData;
import com.kwetril.highload.server.DiscardServer;
import org.json.JSONArray;
import org.json.JSONObject;

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
                            JSONObject usersObject = new JSONObject(content);
                            JSONArray users = usersObject.getJSONArray("users");
                            for (int i = 0; i < users.length(); i++) {
                                JSONObject jsonObject = users.getJSONObject(i);
                                String userJson = jsonObject.toString();
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
                            JSONObject locationsObject = new JSONObject(content);
                            JSONArray locations = locationsObject.getJSONArray("locations");
                            for (int i = 0; i < locations.length(); i++) {
                                JSONObject jsonObject = locations.getJSONObject(i);
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
                            JSONObject visitsObject = new JSONObject(content);
                            JSONArray visits = visitsObject.getJSONArray("visits");
                            for (int i = 0; i < visits.length(); i++) {
                                JSONObject jsonObject = visits.getJSONObject(i);
                                String visitJson = jsonObject.toString();
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

    private static void warmUpServer() {
    /*    try {
            long t0 = System.currentTimeMillis();
            //Thread[] warmupThreads = new Thread[10];
            //for (int t = 0; t < warmupThreads.length; t++) {
            //    warmupThreads[t] = new Thread(() -> {
            System.out.println("Start WarmUp");
            long startTime = System.currentTimeMillis();
            Client client = ClientBuilder.newClient();
            for (int i = 0; i < 10; i++) {
                int c = 0;
                for (int id : RepositoryProvider.repo.getUserIds()) {
                    WebTarget target = client.target(String.format("http://127.0.0.1:%d/users/%d", PORT, id));
                    Response response = target.request().get();
                    response.close();
                    c++;
                    if (c > 1000) {
                        break;
                    }
                }
                c = 0;
                for (int id : RepositoryProvider.repo.getLocationIds()) {
                    WebTarget target = client.target(String.format("http://127.0.0.1:%d/locations/%d", PORT, id));
                    Response response = target.request().get();
                    response.close();
                    if (c > 1000) {
                        break;
                    }
                }
                c = 0;
                for (int id : RepositoryProvider.repo.getVisitIds()) {
                    WebTarget target = client.target(String.format("http://127.0.0.1:%d/visits/%d", PORT, id));
                    Response response = target.request().get();
                    response.close();
                    if (c > 1000) {
                        break;
                    }
                }
                System.out.println("WarmUp iteration: " + i);
                if (System.currentTimeMillis() - startTime > 15000) {
                    System.out.println("Finish WarmUp");
                    break;
                }
            }
            //    });
            //}
            //for (int t = 0; t < warmupThreads.length; t++) {
            //    warmupThreads[t].start();
            //}
            //for (int t = 0; t < warmupThreads.length; t++) {
            //    warmupThreads[t].join();
            //}
            long t1 = System.currentTimeMillis();
            System.out.println(String.format("WarmUP threads joined: %.3f sec", (t1 - t0) / 1000.));
        }
        catch (Exception ex) {
            System.out.println("Error on WarmUp " + ex.getMessage());
        }*/
    }

    private static int PORT = 808;
    private static String DATA_PATH = "data.zip";
    //public static String DATA_PATH = "/tmp/data/data.zip";

    public static void main(String[] args) throws Exception {
        System.out.println("NETTY");
        if (args.length > 0) {
            PORT = 80;
            DATA_PATH = "/tmp/data/data.zip";
        }
        Locale.setDefault(Locale.US);
        initDb();
        new DiscardServer(PORT).run();
        warmUpServer();
        System.gc();
        System.out.println(String.format("Total: %s; Free: %s", Runtime.getRuntime().totalMemory() / 1024. / 1024,
                Runtime.getRuntime().freeMemory() / 1024. / 1024));
        Thread.sleep(1000 * 60 * 60);
    }
}
