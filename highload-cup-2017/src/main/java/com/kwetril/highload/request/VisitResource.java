package com.kwetril.highload.request;

import com.kwetril.highload.database.Repository;
import com.kwetril.highload.parsing.RequestParser;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("visit")
public class VisitResource {
    public VisitResource() {
    }

    @GET
    @Path("{visitId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getVisit(@PathParam("visitId") int visitId) {
        String visit = Repository.getVisit(visitId);
        if (visit != null) {
            return Response.status(200).entity(visit).build();
        } else {
            return Response.status(404).entity("{}").build();
        }
    }

    @POST
    @Path("{visitId}")
    @Consumes(MediaType.WILDCARD)
    @Produces({MediaType.APPLICATION_JSON})
    public Response editVisit(@PathParam("visitId") int visitId, String data) {
        VisitUpdate update = RequestParser.parseEditVisit(data);
        update.visitId = visitId;
        boolean isUpdated = Repository.editVisit(update);
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
    public Response addVisit(String data) {
        VisitData visit = RequestParser.parseNewVisit(data);
        if (visit != null) {
            Repository.addVisit(visit);
            return Response.status(200).build();
        } else {
            return Response.status(404).build();
        }
    }
}
