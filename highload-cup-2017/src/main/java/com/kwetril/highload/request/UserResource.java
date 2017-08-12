package com.kwetril.highload.request;

import com.kwetril.highload.database.Repository;
import com.kwetril.highload.parsing.RequestParser;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("user")
public class UserResource {
    public UserResource() {
    }

    @GET
    @Path("{userId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUser(@PathParam("userId") int userId) {
        //System.out.println(String.format("get user %s", userId));
        String user = Repository.getUser(userId);
        if (user != null) {
            return Response.status(200).entity(user).build();
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
        UserData update = RequestParser.parseEditUser(data);
        update.userId = userId;
        boolean isUpdated = Repository.editUser(update);
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
            Repository.addUser(user);
            return Response.status(200).build();
        } else {
            return Response.status(404).build();
        }
    }
}
