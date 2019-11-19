package fr.umlv.square.controllers;

import java.util.List;

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

import fr.umlv.square.database.Log;
import fr.umlv.square.models.ApplicationsList;
import fr.umlv.square.models.LogsApplication;

@Path("/logs")
@SuppressWarnings("static-method")
public class LogsListRoute {

	@Inject
	ApplicationsList listApp;

	@Path("/{time}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Response getTime(@PathParam("time") int time) {
		return Response.status(200).entity(LogsApplication.listToJson(Log.getByTime(time))).build();
	}

	
	@Path("/{time}/{filter}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTimeFiltered(@PathParam("time") int time, @PathParam("filter") String filter) {
		return Response.status(200)
				.entity(LogsApplication.listToJson(Log.getByTimeAndFilter(time, filter))).build();
	}

	@Path("")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public Response logs(@QueryParam("idC") String id, List<JsonObject> obj) {
		var app = this.listApp.getOneAppRunningByID(id);
		if (app.isEmpty())
			Response.status(Status.NOT_ACCEPTABLE).build();
		boolean res = Log.addLogs(obj, app.get());
		return res ? Response.status(Status.CREATED).build() : Response.status(Status.NOT_ACCEPTABLE).build();
	}
}
