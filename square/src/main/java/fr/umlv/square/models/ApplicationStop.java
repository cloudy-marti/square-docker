package fr.umlv.square.models;

import fr.umlv.square.database.entities.Application;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.bind.annotation.JsonbProperty;

public class ApplicationStop {
	private final Application app;
	@JsonbProperty("elapsed-time")
	private final String e_t;
	
	public ApplicationStop(Application app, String e_t) {
		Objects.requireNonNull(app);
		Objects.requireNonNull(e_t);
		this.app = app;
		this.e_t = e_t;
	}

	public Application getApp() {
		return this.app;
	}

	/**
	 * This method serializes a Stop object.
	 * @return String which is the Json of the Object.
	 * @param obj object we will serialize
	 */
	public static JsonObject serialize(ApplicationStop obj) {
		JsonObject value = 
				Json.createObjectBuilder().
				add("id", obj.app.getId()).
		        add("app", obj.app.getApp()).
		        add("port", obj.app.getPort()).
		        add("service-port", obj.app.getServicePort()).
		        add("docker-instance", obj.app.getDockerInst()).
		        add("elapsed_time", obj.e_t).
		        build();
		return value;
	}
}
