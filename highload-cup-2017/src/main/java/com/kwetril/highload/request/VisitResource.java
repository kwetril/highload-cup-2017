package com.kwetril.highload.request;

import com.kwetril.highload.database.RepositoryProvider;
import com.kwetril.highload.parsing.RequestParser;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("visits")
public class VisitResource {
    public VisitResource() {
    }

    @GET
    @Path("{visitId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getVisit(@PathParam("visitId") int visitId) {
        VisitData visit = RepositoryProvider.repo.getVisit(visitId);
        if (visit != null) {
            return Response.status(200).entity(visit.toString()).build();
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
        if (update == null) {
            return Response.status(400).entity("{}").build();
        }
        update.visitId = visitId;
        boolean isUpdated = RepositoryProvider.repo.editVisit(update);
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
    public Response addVisit(String data) {
        VisitData visit = RequestParser.parseNewVisit(data);
        if (visit != null) {
            RepositoryProvider.repo.addVisit(visit);
            return Response.status(200).entity("{}").build();
        } else {
            return Response.status(400).entity("{}").build();
        }
    }
}
