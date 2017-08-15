package com.kwetril.highload.request;

import com.kwetril.highload.database.RepositoryProvider;
import com.kwetril.highload.parsing.RequestParser;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("users")
public class UserResource {
    public UserResource() {
    }

    @GET
    @Path("{userId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUser(@PathParam("userId") int userId) {
        //System.out.println(String.format("get user %s", userId));
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
        System.out.println(String.format("edit user %s with data %s", userId, data));
        UserUpdate update = RequestParser.parseEditUser(data);
        update.userId = userId;
        boolean isUpdated = RepositoryProvider.repo.editUser(update);
        if (isUpdated) {
            return Response.status(200).build();
        } else {
            return Response.status(404).build();
        }
    }

    @POST
    @Path("new")
    @Consumes(MediaType.WILDCARD)
    @Produces({MediaType.APPLICATION_JSON})
    public Response addUser(String data) {
        System.out.println(String.format("add user with data %s", data));
        UserData user = RequestParser.parseNewUser(data);
        if (user != null) {
            RepositoryProvider.repo.addUser(user);
            return Response.status(200).build();
        } else {
            return Response.status(404).build();
        }
    }

    @GET
    @Path("{userId}/visits")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUserVisits(@PathParam("userId") int userId) {
        //System.out.println(String.format("get user %s", userId));
        UserData user = RepositoryProvider.repo.getUser(userId);
        if (user != null) {
            return Response.status(200).entity(user.toString()).build();
        } else {
            return Response.status(404).entity("{}").build();
        }
    }
}
