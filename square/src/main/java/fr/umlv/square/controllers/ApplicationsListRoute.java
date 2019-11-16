package fr.umlv.square.controllers;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import fr.umlv.square.models.Application;
import fr.umlv.square.models.ApplicationsList;
import fr.umlv.square.models.Stop;

import static fr.umlv.square.docker.DockerDeploy.*;


@Path("/app")
public class ApplicationsListRoute {
	

	@Inject
	private ApplicationsList appList;
	
	@ConfigProperty(name = "quarkus.http.port")
	private String port;
	@ConfigProperty(name = "quarkus.http.host")
	private String host;
	
	@Path("/list")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<Application> list() {
		return this.appList.getList();
	}

	@Path("/deploy")
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
    public Response deploy(JsonObject obj) {
		Objects.requireNonNull(obj);

		String str;
		String array[];

		try {
			str = obj.get("app").toString();
			str = str.replace('"',' ').trim();
			array = str.split(":");

			Application app = new Application(205,array[0],Integer.parseInt(array[1]), getUnboundedLocalPort(),"docker-"+205);

			this.appList.add(app);

			deployDocker(app, port, host);

		} catch(NullPointerException  e) {
			return Response.status(Status.NOT_ACCEPTABLE).entity("Error with the JSON").build();
		} catch (IndexOutOfBoundsException e) {
			return Response.status(Status.NOT_ACCEPTABLE).entity("index out of bound").build();
		} catch (NumberFormatException e) {
			return Response.status(Status.NOT_ACCEPTABLE).entity("number format exception").build();
		} catch (IllegalStateException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unbounded port not found").build();
		} catch (IOException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("IO Error").build();
		}

		return Response.status(Status.CREATED).build();
    }

	@Path("/stop")
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
    public Response stop(JsonObject obj) {
		 System.out.println("\nFermeture du Docker de l'app avec l'id " + obj.get("id"));
		 //Stop val = new Stop(appList.getList().get(1),"4m37s");
		 
         return Response.status(Status.OK).entity("coucou").build();
    }
}