package fr.umlv.square.controllers;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
	private int idApps;

	@Path("/list")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String list() {
		StringBuilder str = new StringBuilder();
		for(var elem : this.appList.getList())
			str.append(Application.serialize(elem));
		return str.toString();
	}

	@Path("/deploy")
	@POST
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
	@Consumes(MediaType.APPLICATION_JSON)
    public Response deploy(JsonObject obj) {
		Objects.requireNonNull(obj);

		String str;
		String array[];
		Application app;
		

		try {
			str = obj.get("app").toString();
			str = str.replace('"',' ').trim();
			array = str.split(":");
			
			if(!this.appList.appAvailable().contains(array[0]))
				return Response.status(Status.NOT_ACCEPTABLE).entity("Application doesn't exists").build();
			app = new Application(
					this.idApps,array[0],
					Integer.parseInt(array[1]),
					getUnboundedLocalPort(),
					array[0]+"-"+ (this.appList.getDeployID(array[0])));

			if(!deployDocker(app, this.port, this.host))
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();

			this.appList.add(app);
			this.appList.increment_app(array[0]);
			this.idApps++;

		} catch(NullPointerException | IndexOutOfBoundsException | NumberFormatException e) {
			return Response.status(Status.NOT_ACCEPTABLE).entity("Error with the JSON").build();
		}catch (IllegalStateException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unbounded port not found").build();
		} catch (IOException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("IO Error").build();
		}
		return Response.status(Status.CREATED).entity(Application.serialize(app)).build();
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