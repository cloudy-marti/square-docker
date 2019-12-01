package fr.umlv.square.controllers;

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

import fr.umlv.square.database.entities.Application;
import fr.umlv.square.models.ApplicationsList;
import fr.umlv.square.models.AutoScale;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static fr.umlv.square.controllers.ApplicationEndPoint.getFromJson;

@Path("/auto-scale")
public class AutoScaleEndPoint {
	private final ApplicationsList appList;
	private final ApplicationEndPoint appListRoute;
	private final AutoScale data;

	@Inject
	public AutoScaleEndPoint(ApplicationsList appList, ApplicationEndPoint appListRoute, AutoScale data) {
		this.appList = appList;
		this.appListRoute = appListRoute;
		this.data = data;
	}

	/**
	 * Stop AutoScale manager.
	 * 
	 * @return Response
	 */
	@Transactional
	@Path("/stop")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response stop() {
		Map<String, Integer> map;
		try {
			map = this.data.WrapperStopAutoScale();
		} catch (IllegalStateException e) {
			return Response.status(Status.BAD_REQUEST).entity("Non-running AutoScale cannot be stopped").build();
		}
		return Response.status(Status.OK).entity(map).build();
	}

	/**
	 * Get last AutoScale actions.
	 * 
	 * @return Response
	 */
	@Transactional
	@Path("/status")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response status() {
		if (!data.isAutoScaleRunning()) {
			return Response.status(Status.BAD_REQUEST).entity("Cannot get non-running AutoScale status").build();
		}
		var map = data.wrapperUpdateStatus(this.appList);
		return Response.status(Status.OK).entity(map).build();
	}

	/**
	 * Start or update AutoScale manager.
	 * 
	 * @param obj : JsonObject to be deserialized that contains the information of
	 *            the apps and their instances wanted.
	 * @return Response
	 */
	@Transactional
	@Path("/update")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(JsonObject obj) {
		Objects.requireNonNull(obj);
		int status;
		Map<String, String> map;
		try {
			map = data.updateAutoScale(obj, this.appList);
		} catch (NumberFormatException e) {
			return Response.status(Status.BAD_REQUEST).entity("Error with the JSON").build();
		} catch (IllegalArgumentException e) {
			return Response.status(Status.BAD_REQUEST).entity("Error with the JSON").build();
		}
		return Response.status(Status.OK).entity(map).build();
	}

	public void tryUpdating() {
		new Thread(() -> {
			var map = this.data.tryUpdating();
			int i = 0;
			for (var elem : map.entrySet()) {
				this.updateApps(elem.getKey(), elem.getValue(), map.size() == i);
				i++;
			}
			this.data.isFree();
		}).start();
	}

	private void updateApps(String appName, int number, boolean isLast) {
		String array[] = appName.split(":");
		if (number < 0) {
			for (int i = 0; i > number; i--) {
				this.deployApp(array);
			}
		} else if (number > 0) {
			for (int i = number; i > 0; i--)
				this.removeApp(appName);
		}
	}

	private void deployApp(String[] array) {
		try {
			this.appListRoute.deployingApp(array);
		} catch (IOException e) {
			return;
		}
	}

	private void removeApp(String app) {
		Optional<Application> tmpApp = this.appList.getAppByNameAndPort(app);
		if (tmpApp.isEmpty()) {
			return;
		}
		try {
			this.appListRoute.stoppingApp(String.valueOf(tmpApp.get().getId()));
		} catch (IOException e) {
			return;
		}
	}
}
