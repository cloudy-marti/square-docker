package fr.umlv.square.controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import fr.umlv.square.database.entities.Log;
import fr.umlv.square.database.ressources.LogRessources;
import fr.umlv.square.models.ApplicationsList;
import fr.umlv.square.models.LogsApplication;

@Path("/logs")
public class LogEndPoint {

	private final ApplicationsList listApp;
	private final LogRessources logRessource;

	/**
	 * 
	 * @param app Injected value of ApplicationList
	 * @param logR Injected value of LogRessources
	 */
	@Inject
	public LogEndPoint(ApplicationsList app,  LogRessources logR) {
		this.listApp = app;
		this.logRessource = logR;
	}


	/**
	 * This endPoint return logs filtered by a date.
	 * @return Response.
	 * @param Int, in minutes the time we will withdraw right now
	 */
	@Path("/{time}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Response getLogsByTime(@PathParam("time") int time) {
		var res = this.logRessource.getByTime(getTimed(time));
		return Response.status(200).entity(LogsApplication.listToJson(res)).build();
	}

	
	/**
	 * This endPoint return logs filtered by a date and a filter.
	 * @return Response.
	 * @param time, in minutes the time we will withdraw right now
	 * @Param filter, the filter we gonna apply on the selected logs.
	 * 
	 */
	@Path("/{time}/{filter}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLogsByTimeAndFilter(@PathParam("time") int time, @PathParam("filter") String filter) {
		var res = this.logRessource.getByTimeAndFilter(getTimed(time), filter);
		return Response.status(200)
				.entity(LogsApplication.listToJson(res)).build();
	}

	/**
	 * This endPoint save logs in the database.
	 * @return Response.
	 * @param id, the id of the application to which the logs belong 
	 * @Param obj, a list of JsonObject who are the logs
	 * 
	 */
	@Path("")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public Response logs(@QueryParam("idC") String id, List<JsonObject> obj) {
		var app = this.listApp.getOneAppRunningByID(id);
		if (app.isEmpty())
			Response.status(Status.BAD_REQUEST).build();
		boolean res = Log.addLogs(obj, app.get());
		return res ? Response.status(Status.CREATED).build() : Response.status(Status.BAD_REQUEST).build();
	}
	
	
	private static OffsetDateTime getTimed(int timer) {
        TimeZone tz = TimeZone.getTimeZone("UTC"); //$NON-NLS-1$
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); //$NON-NLS-1$
        df.setTimeZone(tz);
        Instant instant = Instant.parse(df.format(new Date()));
        OffsetDateTime time = OffsetDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));	
        return time.minusMinutes(timer);	
	}
}
