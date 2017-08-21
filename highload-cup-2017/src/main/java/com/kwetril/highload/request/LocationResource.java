package com.kwetril.highload.request;

import com.kwetril.highload.database.RepositoryProvider;
import com.kwetril.highload.database.TimestampProvider;
import com.kwetril.highload.parsing.RequestParser;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.TimeZone;

@Path("locations")
public class LocationResource {
    public LocationResource() {
    }

    @GET
    @Path("{locationId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getLocation(@PathParam("locationId") int locationId) {
        LocationData location = RepositoryProvider.repo.getLocation(locationId);
        if (location != null) {
            return Response.status(200).entity(location.toString()).build();
        } else {
            return Response.status(404).entity("{}").build();
        }
    }

    @POST
    @Path("{locationId}")
    @Consumes(MediaType.WILDCARD)
    @Produces({MediaType.APPLICATION_JSON})
    public Response editLocation(@PathParam("locationId") int locationId, String data) {
        LocationUpdate update = RequestParser.parseEditLocation(data);
        if (update == null) {
            return Response.status(400).entity("{}").build();
        }
        update.locationId = locationId;
        boolean isUpdated = RepositoryProvider.repo.editLocation(update);
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
    public Response addLocation(String data) {
        LocationData location = RequestParser.parseNewLocation(data);
        if (location != null) {
            RepositoryProvider.repo.addLocation(location);
            return Response.status(200).entity("{}").build();
        } else {
            return Response.status(400).entity("{}").build();
        }
    }

    @GET
    @Path("{locationId}/avg")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUserVisits(@PathParam("locationId") int locationId,
                                  @QueryParam("fromDate") String fromDateStr,
                                  @QueryParam("toDate") String toDateStr,
                                  @QueryParam("fromAge") String fromAgeStr,
                                  @QueryParam("toAge") String toAgeStr,
                                  @QueryParam("gender") String gender) {
        try {
            if (gender != null && (gender.length() > 1 || (gender.charAt(0) != 'm' && gender.charAt(0) != 'f'))) {
                return Response.status(400).entity("{}").build();
            }
            long fromDate = 0, toDate = 0, fromAge = 0, toAge = 0;
            long currentMillis = TimestampProvider.timestamp * 1000;
            boolean hasFromDate = fromDateStr != null;
            if (hasFromDate) fromDate = Long.parseLong(fromDateStr);
            boolean hasToDate = toDateStr != null;
            if (hasToDate) toDate = Long.parseLong(toDateStr);
            boolean hasFromAge = fromAgeStr != null;
            if (hasFromAge) {
                fromAge = Long.parseLong(fromAgeStr);
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(currentMillis);
                calendar.add(Calendar.YEAR, (int) -fromAge);
                fromAge = calendar.getTimeInMillis() / 1000;
            }
            boolean hasToAge = toAgeStr != null;
            if (hasToAge) {
                toAge = Long.parseLong(toAgeStr);
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(currentMillis);
                calendar.add(Calendar.YEAR, (int) -toAge);
                toAge = calendar.getTimeInMillis() / 1000;
            }
            double avg = RepositoryProvider.repo.getLocationMark(locationId,
                    hasFromDate, fromDate, hasToDate, toDate,
                    hasFromAge, fromAge, hasToAge, toAge,
                    gender);
            if (avg >= -0.5) {
                return Response.status(200).entity(String.format("{\"avg\":%.5f}", avg)).build();
            } else {
                return Response.status(404).entity("{}").build();
            }
        } catch (Exception ex) {
            return Response.status(400).entity("{}").build();
        }
    }
}
