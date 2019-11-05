package fr.umlv.square.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import fr.umlv.square.models.Application;
import fr.umlv.square.models.LogsApplication;







@Path("/logs")
public class LogsListRoute {

	private List<LogsApplication> list = new ArrayList<LogsApplication>();
	public LogsListRoute() {
		Application a = new Application(201,"todomvc:8082", "todomvc", 8082,15201,"todomvc-12");
		Application b = new Application(202,"todomvc:8082", "todomvc",8082,15202,"todomvc-13");
		Application c = new Application(203,"todomvc:8082", "todomvc",8082,15203,"todomvc-14");

		list.add(new LogsApplication(a, "message de test", "2019-10-15T23:58:00.000Z"));
		list.add(new LogsApplication(b, "message de test", "2019-11-15T23:58:00.000Z"));
		list.add(new LogsApplication(c, "message de test", "2019-12-15T23:58:00.000Z"));
	}

	@Path("/{time}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTime(@PathParam("time") int time) {
		System.out.println(time);
		 
		return Response.status(200).entity(LogsApplication.getListMapped(list)).build();
	}
	
	@Path("/{time}/{filter}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTimeFiltered(@PathParam("time") int time, @PathParam("filter") int id) {
		System.out.println("time : " + time + " et id:" + id);
		LogsApplication a = list.get(0);
		return Response.status(200).entity(a.toMap()).build() ;
	}
	
	@Path("")
	@POST	
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
    public Response logs(List<JsonObject> obj) {
		 System.out.println("i received Logs !");
		 System.out.println(obj.toString());
         return Response.status(Status.CREATED).entity(list.get(1)).build();
    }
}
