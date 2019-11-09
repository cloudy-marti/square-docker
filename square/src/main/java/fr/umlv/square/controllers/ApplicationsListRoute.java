package fr.umlv.square.controllers;
import java.util.ArrayList;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import fr.umlv.square.models.Application;
import fr.umlv.square.models.Stop;


@Path("/app")
public class ApplicationsListRoute {
	private static ArrayList<Application> list = new ArrayList<Application>();
	
	public ApplicationsListRoute() {
		list.add(new Application(201,"todomvc",8082,15201,"todomvc-12"));
		list.add(new Application(202,"todomvc",8082,15202,"todomvc-13"));
		list.add(new Application(203,"todomvc",8082,15203,"todomvc-14"));

	}
	
	@Path("/list")
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<Application>list() {
        return list;
    }
	
	@Path("/deploy")
	@POST	
    @Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
    public Response deploy(JsonObject obj) {
		String str;
		String array[];
		 try {
			 str = obj.get("app").toString();
			 array = str.split(":");
			 list.add(new Application(204,array[0],Integer.parseInt(array[1]),15204,"le nom"));
		 }catch(NullPointerException | IndexOutOfBoundsException | NumberFormatException e) {
			 return Response.status(Status.NOT_ACCEPTABLE).entity("Error with the JSON").build();
		 }
		 return Response.status(Status.CREATED).build();
    }
	
	@Path("/stop")
	@POST	
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
    public Response stop(JsonObject obj) {
		 System.out.println("\nFermeture du Docker de l'app avec l'id " + obj.get("id"));
		 Stop val = new Stop(list.get(1),"4m37s");
		 
         return Response.status(Status.OK).entity(val.toMap()).build();
    }
}