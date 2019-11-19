package fr.umlv.square.controllers;
import java.io.IOException;

import java.util.*;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.persistence.Transient;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import fr.umlv.square.models.Stop;
import fr.umlv.square.utils.Counter;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import fr.umlv.square.database.Application;
import fr.umlv.square.models.ApplicationsList;

import static fr.umlv.square.docker.DockerDeploy.*;


@Path("/app")
public class ApplicationsListRoute {

	private final ApplicationsList appList;
	private final String port;
	private final String host;	
	
	@Inject
	public ApplicationsListRoute(
		@ConfigProperty(name = "quarkus.http.port") String port,
		@ConfigProperty(name = "quarkus.http.host") String host,
		ApplicationsList appList)
	{
		this.host = host;
		this.port = port;
		this.appList = appList;
		
	}

	@Path("/list")
	@GET	
	@Transactional
	@Produces(MediaType.APPLICATION_JSON)
	public String list() {
		this.appList.initApplicationsList();
		StringBuilder str = new StringBuilder();
		for(var elem : this.appList.getList())
			str.append(Application.serialize(elem));
		return str.toString();
	}

	@Transactional
	@Path("/deploy")
	@POST
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
	@Consumes(MediaType.APPLICATION_JSON)
    public Response deploy(JsonObject obj) {
		Objects.requireNonNull(obj);
		this.appList.initApplicationsList();
		Application app;
		try {
			String[] array = getFromJson(obj, "app");
			
			if(!this.appList.appAvailable().contains(array[0]))
				return Response.status(Status.NOT_ACCEPTABLE).entity("Application doesn't exists").build();
			
			app = new Application(
					this.appList.getCount(),
					array[0],
					Integer.parseInt(array[1]),
					getUnboundedLocalPort(),
					array[0]+"-"+ (this.appList.getDeployID(array[0])));

			if(!deployDocker(app, this.port, this.host))
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			this.appList.add(app, array[0]);
			app.addInBDD();

			getRunningInstancesNames();

		} catch(NullPointerException | IndexOutOfBoundsException | NumberFormatException e) {
			return Response.status(Status.NOT_ACCEPTABLE).entity("Error with the JSON").build();
		}catch (IllegalStateException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unbounded port not found").build();
		} catch (IOException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("IO Error").build();
		}
		return Response.status(Status.CREATED).entity(Application.serialize(app)).build();
    }

	@Transactional
	@Path("/stop")
	@POST
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
	@Consumes(MediaType.APPLICATION_JSON)
    public Response stop(JsonObject obj) {
		Objects.requireNonNull(obj);
		this.appList.initApplicationsList();
		Stop stopVal;
		try {
			String[] array = getFromJson(obj, "id");
			int id = Integer.parseInt(array[0]);

			Optional<Application> tmp = appList.getAppById(id);
			if(tmp.isEmpty()) {
				return Response.status(Status.NOT_ACCEPTABLE).entity("Container is no longer listed").build();
			}

			Application tmpApp = tmp.get();
			if(!stopDockerInstance(tmpApp.getDockerInst())) {
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
			this.appList.deleteApp(tmpApp);
			String elapsedTime = getElapsedTime(tmpApp.getStartTime(), System.currentTimeMillis());
			stopVal = new Stop(tmpApp, elapsedTime);

			tmpApp.setElapsedTime(elapsedTime);
		} catch (NullPointerException e) {
			return Response.status(Status.NOT_ACCEPTABLE).entity("Container is no longer listed").build();
		} catch(IndexOutOfBoundsException | NumberFormatException e) {
			return Response.status(Status.NOT_ACCEPTABLE).entity("Error with the JSON").build();
		} catch (IOException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("IO Error").build();
		}
         return Response.status(Status.OK).entity(Stop.serialize(stopVal)).build();
    }

	private static String[] getFromJson(JsonObject obj, String key) {
		String str = obj.get(key).toString();
		str = str.replace('"', ' ').trim();
		return str.split(":");
	}

	private static String getElapsedTime(long startTime, long endTime) {
		long elapsedTime = endTime - startTime;

		String timeTemplate = "%sm%ss";

		long seconds = elapsedTime/1000;
		long minutes = seconds/60;
		seconds = seconds - (minutes*60);

		return String.format(timeTemplate, minutes, seconds);
	}
}