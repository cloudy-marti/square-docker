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
import java.util.Objects;
import java.util.Optional;

import static fr.umlv.square.controllers.ApplicationsListRoute.getFromJson;

@Path("/auto-scale")
public class AutoScaleListRoute {

	private static final String actionTemplate;
	private static final String noAction;

	static {
		actionTemplate = "need to %s %s instance(s)";
		noAction = "no action";
	}

	private final ApplicationsList appList;

	private final ApplicationsListRoute appListRoute;

	@Inject
	public AutoScaleListRoute(ApplicationsList appList, ApplicationsListRoute appListRoute) {
		this.appList = appList;
		this.appListRoute = appListRoute;
	}
	private AutoScale data = new AutoScale();

	/**
	 * Stop AutoScale manager.
	 * @return Response
	 */
	@Transactional
	@Path("/stop")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response stop() {
		if(!data.isAutoScaleRunning()) {
			return Response.status(Status.NOT_ACCEPTABLE).entity("Non-running AutoScale cannot be stopped").build();
		}
		data.stopAutoScale();
		return Response.status(200).entity(data.getAutoScale()).build();
	}

	/**
	 * Get last AutoScale actions.
	 * @return Response
	 */
	@Transactional
	@Path("/status")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response status() {
		if(!data.isAutoScaleRunning()) {
			return Response.status(Status.NOT_ACCEPTABLE).entity("Cannot get non-running AutoScale status").build();
		}
		updateStatus();
		return Response.status(200).entity(data.getStatusMap()).build();
	}

	/**
	 * Start or update AutoScale manager.
	 * @param obj : JsonObject to be deserialized that contains the information of the apps and their instances wanted.
	 * @return Response
	 */
	@Transactional
	@Path("/update")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(JsonObject obj) {
		Objects.requireNonNull(obj);
		try {
			obj.keySet().forEach(key -> {
				String[] str = getFromJson(obj, key);
				data.addToAutoScale(key, Integer.parseInt(str[0]));
			});
		} catch(NumberFormatException e) {
			return Response.status(Status.NOT_ACCEPTABLE).entity("Error with the JSON").build();
		}

		this.data.startAutoScale();
		int status = updateStatus();

		if(status == Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("IO Error").build();
		} else if (status == Status.NOT_ACCEPTABLE.getStatusCode()) {
			return Response.status(Status.NOT_ACCEPTABLE).entity("Error with the JSON").build();
		} else {
			return Response.status(Status.OK).entity(data.getStatusMap()).build();
		}
	}

	private int updateStatus() {
		data.clearStatus();
		int status = 200;
		data.getAutoScale().forEach((key, autoScaleValue) -> {
			long instances = appList.getCountByNameAndPort(key);
			String statusValue =
					instances == autoScaleValue ?
					noAction : autoScaleActionString(instances - autoScaleValue);
			data.addToStatus(key, statusValue);
			try {
				autoScaleAction(key, instances - autoScaleValue);
			} catch (IOException e) {
				return;
			}
		});
		return status;
	}

	private String autoScaleActionString(long diff) {
		if(diff < 0) { 			// "need to start <-diff> instances"
			return String.format(actionTemplate, "start", -diff);
		} else {				// "need to stop <diff> instances"
			return String.format(actionTemplate, "stop", diff);
		}
	}

	private void autoScaleAction(String app, long diff) throws IOException {
		if(diff < 0) { 			// need to deploy <diff> instances
			app.replace('"', ' ').trim();
			String[] array = app.split(":");
			for(int i = 0; i < -diff; ++i) {
				appListRoute.deployApp(array);
			}
		} else {				// need to stop <diff> instances
			for(int i = 0; i < diff; ++i) {
				Optional<Application> tmpApp = appList.getAppByNameAndPort(app);
				if(tmpApp.isEmpty()) {
					return;
				}
				int id = tmpApp.get().getId();
				appListRoute.stopApp(String.valueOf(id));
			}
		}
	}
}
