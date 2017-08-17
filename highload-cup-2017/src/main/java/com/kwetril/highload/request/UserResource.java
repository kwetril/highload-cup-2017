package com.kwetril.highload.request;

import com.kwetril.highload.database.RepositoryProvider;
import com.kwetril.highload.parsing.RequestParser;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

@Path("users")
public class UserResource {
    public UserResource() {
    }

    @GET
    @Path("{userId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUser(@PathParam("userId") int userId) {
        UserData user = RepositoryProvider.repo.getUser(userId);
        if (user != null) {
            return Response.status(200).entity(user.toString()).build();
        } else {
            return Response.status(404).entity("{}").build();
        }
    }

    @POST
    @Path("{userId}")
    @Consumes(MediaType.WILDCARD)
    @Produces({MediaType.APPLICATION_JSON})
    public Response editUser(@PathParam("userId") int userId, String data) {
        UserUpdate update = RequestParser.parseEditUser(data);
        if (update == null) {
            return Response.status(400).entity("{}").build();
        }
        update.userId = userId;
        boolean isUpdated = RepositoryProvider.repo.editUser(update);
        if (isUpdated) {
            return Response.status(200).entity("{}").build();
        } else {
            return Response.status(404).entity("{}").build();
        }
    }

    @POST
    @Path("new")
    @Consumes(MediaType.WILDCARD)
    @Produces({MediaType.APPLICATION_JSON})
    public Response addUser(String data) {
        UserData user = RequestParser.parseNewUser(data);
        if (user != null) {
            boolean userAdded = RepositoryProvider.repo.addUser(user);
            if (userAdded) {
                return Response.status(200).entity("{}").build();
            } else {
                return Response.status(400).entity("{}").build();
            }
        } else {
            return Response.status(400).entity("{}").build();
        }
    }

    @GET
    @Path("{userId}/visits")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUserVisits(@PathParam("userId") int userId,
                                  @QueryParam("fromDate") String fromDateStr,
                                  @QueryParam("toDate") String toDateStr,
                                  @QueryParam("country") String country,
                                  @QueryParam("toDistance") String toDistanceStr) {
        try {
            boolean hasFromDate = fromDateStr != null;
            long fromDate = 0;
            if (hasFromDate) fromDate = Long.parseLong(fromDateStr);
            boolean hasToDate = toDateStr != null;
            long toDate = 0;
            if (hasToDate) toDate = Long.parseLong(toDateStr);
            boolean hasDistance = toDistanceStr != null;
            int distance = 0;
            if (hasDistance) distance = Integer.parseInt(toDistanceStr);
            ArrayList<UserVisitData> userVisits = RepositoryProvider.repo.getUserVisits(userId,
                    hasFromDate, fromDate, hasToDate, toDate, country, hasDistance, distance);
            if (userVisits != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("{\"visits\":[");
                userVisits.sort((a, b) -> Long.compare(a.visitedAt, b.visitedAt));
                int n = userVisits.size();
                for (int i = 0; i < n; i++) {
                    UserVisitData uv = userVisits.get(i);
                    sb.append(uv.toString());
                    if (i < n - 1) {
                        sb.append(',');
                    }
                }
                sb.append("]}");
                return Response.status(200).entity(sb.toString()).build();
            } else {
                return Response.status(400).entity("{}").build();
            }
        } catch (Exception ex) {
            return Response.status(400).entity("{}").build();
        }
    }
}
