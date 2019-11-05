package fr.umlv.square.controllers;


import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import fr.umlv.square.models.AutoScale;


@Path("/auto-scale")
public class AutoScaleListRoute {

	private AutoScale data = new AutoScale();

	@Path("/stop")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response stop() {
		data.addToMap("todomvc:8082", "need to start 1 instance(s)");
		data.addToMap("demo:8083", "need to stop 2 instance(s)");
		return Response.status(200).entity(data.getMap()).build();
	}

	@Path("/status")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response status() {
		data.addToMap("todomvc:8082", "no action");
		data.addToMap("demo:8083", "need to stop 2 instance(s)");
		return Response.status(200).entity(data.getMap()).build();
	}

	@Path("/update")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(JsonObject obj) {
		System.out.println(obj);
		data.addToMap("todomvc:8082", "no action");
		data.addToMap("demo:8083", "need to stop 2 instance(s)");
		return Response.status(Status.OK).entity(data.getMap()).build();
	}
}
