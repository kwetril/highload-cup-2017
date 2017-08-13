package com.kwetril.highload.request;

import com.kwetril.highload.database.Repository;
import com.kwetril.highload.parsing.RequestParser;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("location")
public class LocationResource {
    public LocationResource() {
    }

    @GET
    @Path("{locationId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getLocation(@PathParam("locationId") int locationId) {
        String location = Repository.getLocation(locationId);
        if (location != null) {
            return Response.status(200).entity(location).build();
        } else {
            return Response.status(404).entity("{}").build();
        }
    }

    @POST
    @Path("{locationId}")
    @Consumes(MediaType.WILDCARD)
    @Produces({MediaType.APPLICATION_JSON})
    public Response editLocation(@PathParam("locationId") int locationId, String data) {
        LocationData update = RequestParser.parseEditLocation(data);
        update.locationId = locationId;
        boolean isUpdated = Repository.editLocation(update);
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
    public Response addLocation(String data) {
        LocationData location = RequestParser.parseNewLocation(data);
        if (location != null) {
            Repository.addLocation(location);
            return Response.status(200).build();
        } else {
            return Response.status(404).build();
        }
    }
}
