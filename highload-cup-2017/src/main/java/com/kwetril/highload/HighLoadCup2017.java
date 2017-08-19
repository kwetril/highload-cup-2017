package com.kwetril.highload;

import com.kwetril.highload.database.RepositoryProvider;
import com.kwetril.highload.parsing.RequestParser;
import com.kwetril.highload.request.LocationData;
import com.kwetril.highload.request.UserData;
import com.kwetril.highload.request.VisitData;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.memory.ByteBufferManager;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.strategies.LeaderFollowerNIOStrategy;
import org.glassfish.grizzly.strategies.SameThreadIOStrategy;
import org.glassfish.grizzly.strategies.WorkerThreadIOStrategy;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.ext.RuntimeDelegate;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class HighLoadCup2017 {
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
                String bType = a.getName().split("_")[0];
                if (aType.equals("users")) {
                    return -1;
                }
                if (bType.equals("users")) {
                    return 1;
                }
                if (aType.equals("locations")) {
                    return -1;
                }
                if (bType.equals("locations")) {
                    return 1;
                }
                return 0;
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
    }


    private static HttpServer initServer() {
        HttpServer server = new HttpServer();
        server.addListener(new NetworkListener("grizzly", "0.0.0.0", PORT));

        final TCPNIOTransportBuilder transportBuilder = TCPNIOTransportBuilder.newInstance();
        transportBuilder.setIOStrategy(WorkerThreadIOStrategy.getInstance());
        TCPNIOTransport transport = transportBuilder.build();
        try {
            transport.getWorkerThreadPoolConfig().setCorePoolSize(4);
            transport.getWorkerThreadPoolConfig().setMaxPoolSize(4);
        }catch (Exception e) {
            e.printStackTrace();
        }
        server.getListener("grizzly").setTransport(transport)    ;
        final ResourceConfig rc = new ResourceConfig().packages("com.kwetril.highload");
        rc.register(JacksonFeature.class);
        final ServerConfiguration config = server.getServerConfiguration();
        config.addHttpHandler(RuntimeDelegate.getInstance().createEndpoint(rc, GrizzlyHttpContainer.class), "/");

        return server;
    }

    private static void warmUpServer() {
        try {
            long t0 = System.currentTimeMillis();
            //Thread[] warmupThreads = new Thread[10];
            //for (int t = 0; t < warmupThreads.length; t++) {
            //    warmupThreads[t] = new Thread(() -> {
                    System.out.println("Start WarmUp");
                    long startTime = System.currentTimeMillis();
                    Client client = ClientBuilder.newClient();
                    for (int i = 0; i < 10; i++) {
                        for (int id : RepositoryProvider.repo.getUserIds()) {
                            WebTarget target = client.target(String.format("http://127.0.0.1:%d/users/%d", PORT, id));
                            target.request().get().close();
                        }
                        for (int id : RepositoryProvider.repo.getLocationIds()) {
                            WebTarget target = client.target(String.format("http://127.0.0.1:%d/locations/%d", PORT, id));
                            target.request().get().close();
                        }
                        for (int id : RepositoryProvider.repo.getUserIds()) {
                            WebTarget target = client.target(String.format("http://127.0.0.1:%d/visists/%d", PORT, id));
                            target.request().get().close();
                        }
                        System.out.println("WarmUp iteration: " + i);
                        if (System.currentTimeMillis() - startTime > 10000) {
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
            System.out.println("Error on WarmUp");
        }
    }

    public static int PORT = 80;
    //public static String DATA_PATH = "data.zip";
    public static String DATA_PATH = "/tmp/data/data.zip";

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);
        final HttpServer server = initServer();
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        initDb();
        warmUpServer();
        System.gc();

        Thread.sleep(1000 * 60 * 60 * 24);
    }
}
