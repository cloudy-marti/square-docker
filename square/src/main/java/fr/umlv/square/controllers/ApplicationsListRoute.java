package fr.umlv.square.controllers;
import java.io.IOException;

import java.util.ArrayList;

import java.util.HashMap;
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

import static fr.umlv.square.docker.DockerDeploy.*;


@Path("/app")
public class ApplicationsListRoute {
	

	@Inject
	private ApplicationsList appList;

	private final HashMap<Integer, String> dockerInstances = new HashMap<>();
	
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
		Application app;

		try {
			String[] array = getFromJson(obj, "app");
			
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

			getRunningInstancesNames();
			dockerInstances.put(app.getId(), app.getDockerInst());

			System.out.println(dockerInstances.toString());

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
		Objects.requireNonNull(obj);

		//System.out.println("\nFermeture du Docker de l'app avec l'id " + obj.get("id"));
		// Stop val = new Stop(appList.getList().get(1),"4m37s");
		try {
			String[] array = getFromJson(obj, "id");

			if(!stopDockerInstance(dockerInstances.remove(Integer.parseInt(array[0])))) {
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
			System.out.println("Running containers after stop = " + dockerInstances.toString());

		} catch (NullPointerException e) {
			return Response.status(Status.NOT_ACCEPTABLE).entity("Container is no longer listed").build();
		} catch(IndexOutOfBoundsException | NumberFormatException e) {
			return Response.status(Status.NOT_ACCEPTABLE).entity("Error with the JSON").build();
		} catch (IOException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("IO Error").build();
		}

         return Response.status(Status.OK).entity("\nFermeture du Docker de l'app avec l'id " + obj.get("id")).build();
    }

	private static String[] getFromJson(JsonObject obj, String key) {
		String str = obj.get(key).toString();
		str = str.replace('"', ' ').trim();
		return str.split(":");
	}
}