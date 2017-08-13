package com.kwetril.highload;

import com.kwetril.highload.database.Repository;
import com.kwetril.highload.parsing.RequestParser;
import com.kwetril.highload.request.LocationData;
import com.kwetril.highload.request.UserData;
import com.kwetril.highload.request.VisitData;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.strategies.SameThreadIOStrategy;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.ext.RuntimeDelegate;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class HighLoadCup2017 {
    private static void initDb() throws Exception {
        ZipFile zipFile = new ZipFile("data.zip");
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            System.out.println(String.format("Processing entry: %s", entry.getName()));
            InputStream stream = zipFile.getInputStream(entry);
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(stream))) {
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
                                Repository.addUser(user);
                            } else {
                                throw new Exception(String.format("Couldn't parse %s", userJson));
                            }
                        }
                        System.out.println(String.format("Users added: %s", Repository.countUsers()));
                        break;
                    case "locations":
                        JSONObject locationsObject = new JSONObject(content);
                        JSONArray locations = locationsObject.getJSONArray("locations");
                        for (int i = 0; i < locations.length(); i++) {
                            JSONObject jsonObject = locations.getJSONObject(i);
                            String locationJson = jsonObject.toString();
                            LocationData location = RequestParser.parseNewLocation(locationJson);
                            if (location != null) {
                                Repository.addLocation(location);
                            } else {
                                throw new Exception(String.format("Couldn't parse %s", locationJson));
                            }
                        }
                        System.out.println(String.format("Locations added: %s", Repository.countLocations()));
                        break;
                    case "visits":
                        JSONObject visitsObject = new JSONObject(content);
                        JSONArray visits = visitsObject.getJSONArray("visits");
                        for (int i = 0; i < visits.length(); i++) {
                            JSONObject jsonObject = visits.getJSONObject(i);
                            String visitJson = jsonObject.toString();
                            VisitData visit = RequestParser.parseNewVisit(visitJson);
                            if (visit != null) {
                                Repository.addVisit(visit);
                            } else {
                                throw new Exception(String.format("Couldn't parse %s", visitJson));
                            }
                        }
                        System.out.println(String.format("Visists added: %s", Repository.countVisits()));
                        break;
                }
            }
        }
    }


    private static HttpServer initServer() {
        HttpServer server = new HttpServer();
        server.addListener(new NetworkListener("grizzly", "127.0.0.1", 8080));

        final TCPNIOTransportBuilder transportBuilder = TCPNIOTransportBuilder.newInstance();
        //transportBuilder.setIOStrategy(WorkerThreadIOStrategy.getInstance());
        transportBuilder.setIOStrategy(SameThreadIOStrategy.getInstance());
        server.getListener("grizzly").setTransport(transportBuilder.build());
        final ResourceConfig rc = new ResourceConfig().packages("com.kwetril.highload");
        rc.register(JacksonFeature.class);
        final ServerConfiguration config = server.getServerConfiguration();
        config.addHttpHandler(RuntimeDelegate.getInstance().createEndpoint(rc, GrizzlyHttpContainer.class), "/");

        return server;
    }

    public static void main(String[] args) throws Exception {
        initDb();
        final HttpServer server = initServer();
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.in.read();
        server.shutdownNow();
    }
}
