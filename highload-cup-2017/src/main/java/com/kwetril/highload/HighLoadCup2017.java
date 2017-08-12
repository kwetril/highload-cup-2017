package com.kwetril.highload;

import com.kwetril.highload.database.Repository;
import com.kwetril.highload.parsing.RequestParser;
import com.kwetril.highload.request.UserData;
import com.kwetril.highload.request.UserResource;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HighLoadCup2017 {
    private static void initDb() throws Exception {
        String content = new String(Files.readAllBytes(Paths.get("data", "users_1.json")));
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
        System.out.println(String.format("Users added: %s", Repository.numUsers()));
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
        System.out.println("Hello world");
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
