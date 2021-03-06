package fr.umlv.square.controllers;

import java.io.IOException;

import java.util.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import fr.umlv.square.models.ApplicationStop;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import fr.umlv.square.database.entities.Application;
import fr.umlv.square.docker.SynchronizedDeploy;
import fr.umlv.square.models.ApplicationsList;
import static fr.umlv.square.docker.DockerDeploy.*;

@ApplicationScoped
@Path("/app")
public class ApplicationEndPoint {

	private final ApplicationsList appList;
	private final String port;
	private final String host;
	private final String path;
	private final SynchronizedDeploy deploy;

	/**
	 * Constructor
	 * @param port get value from application.properties
	 * @param host get value from application.properties
	 * @param path get value from application.properties
	 * @param appList get the inject value of applicationsList
	 */
	@Inject
	public ApplicationEndPoint(@ConfigProperty(name = "quarkus.http.port") String port,
			@ConfigProperty(name = "quarkus.http.host") String host,
			@ConfigProperty(name = "docker.path.value") String path, ApplicationsList appList) {
		this.deploy = new SynchronizedDeploy();
		this.host = host;
		this.port = port;
		this.appList = appList;
		this.path = path;
	}

	/**
	 * Gets a list of running containers, managed by Square, with a GET request.
	 * 
	 * @return Response
	 */
	@Path("/list")
	@GET
	@Transactional
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<JsonObject> list() {
		this.check_init();
		var list = this.appList.getList();
		ArrayList<JsonObject> str = new ArrayList<>(list.size());
		for (int i = 0; i < list.size(); i++) {
			str.add(Application.serialize(list.get(i)));
		}
		return str;
	}

	/**
	 * Starts a container with the wanted application and port exposed by the
	 * container with a POST request.
	 * 
	 * @param obj : JsonObject to be deserialized and converted into an Application
	 *            object.
	 * @return Response
	 */
	@Transactional
	@Path("/deploy")
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deploy(JsonObject obj) {
		Objects.requireNonNull(obj);
		this.check_init();
		try {
			return this.deployApp(getFromJson(obj, "app"));
		} catch (NullPointerException | IndexOutOfBoundsException | NumberFormatException e) {
			return Response.status(Status.BAD_REQUEST).entity("Error with the JSON").build();
		} catch (IllegalStateException e) {
			return Response.status(Status.BAD_REQUEST).entity("Unbounded port not found").build();
		} catch (IOException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("IO Error").build();
		}
	}
	
	/**
	 * Wrapper to deploy an app from the auto-scale
	 * @param array which contains port and name of app
	 * @throws IOException
	 */
	@Transactional
	public void deployingApp(String[] array) throws IOException {
		this.deployApp(array);
	}

	private Response deployApp(String[] array) throws IOException {
		Application app;
		if (!this.appList.appAvailable().contains(array[0]))
			return Response.status(Status.BAD_REQUEST).entity("Application doesn't exists").build();
		app = new Application(this.appList.getCountAndInc(), array[0], Integer.parseInt(array[1]), getUnboundedLocalPort(),
				array[0] + "-" + (this.appList.getDeployID(array[0], Integer.parseInt(array[1]))));
		if (!this.deploy.deployApp(app, this.port, this.host, this.path))
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		this.appList.add(app);
		app.addInBDD();
		return Response.status(Status.CREATED).entity(Application.serialize(app)).build();
	}

	/**
	 * Stops a docker-container from its application's ID with a POST request.
	 * 
	 * @param obj : JsonObject to be deserialized
	 * @return Response
	 */
	@Transactional
	@Path("/stop")
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response stop(JsonObject obj) {
		Objects.requireNonNull(obj);
		this.check_init();
		try {
			return this.stopApp(getFromJson(obj, "id")[0]);
		} catch (NullPointerException e) {
			return Response.status(Status.BAD_REQUEST).entity("Container is no longer listed").build();
		} catch (IndexOutOfBoundsException | NumberFormatException e) {
			return Response.status(Status.BAD_REQUEST).entity("Error with the JSON").build();
		} catch (IOException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("IO Error").build();
		}
	}

	/**
	 * Wrapper to stop an application from the auto-scale
	 * @param idApp
	 * @throws IOException
	 */
	@Transactional
	public void stoppingApp(String idApp) throws IOException {
		this.stopApp(idApp);
	}

	private Response stopApp(String idApp) throws IOException {
		Optional<Application> tmp = this.appList.getAppById(Integer.parseInt(idApp));
		if (tmp.isEmpty()) {
			return Response.status(Status.BAD_REQUEST).entity("Container is no longer listed").build();
		}
		Application tmpApp = tmp.get();
		if (!stopDockerInstance(tmpApp.getDockerInst(), this.path)) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		this.appList.deleteApp(tmpApp);
		String elapsedTime = getElapsedTime(tmpApp.getStartTime(), System.currentTimeMillis());
		return Response.status(Status.OK).entity(ApplicationStop.serialize(new ApplicationStop(tmpApp, elapsedTime))).build();
	}

	/**
	 * this method return an array of string get from a JsonObject.
	 * @param obj JsonObject that contains the data
	 * @param key Key whose value we want
	 * @return array of string
	 */
	public static String[] getFromJson(JsonObject obj, String key) {
		String str = obj.get(key).toString();
		str = str.replace('"', ' ').trim();
		return str.split(":");
	}

	private void check_init() {
		this.appList.waitStillInit();
	}

	private static String getElapsedTime(long startTime, long endTime) {
		long elapsedTime = endTime - startTime;
		String timeTemplate = "%sm%ss";
		long seconds = elapsedTime / 1000;
		long minutes = seconds / 60;
		seconds = seconds - (minutes * 60);
		return String.format(timeTemplate, minutes, seconds);
	}
}