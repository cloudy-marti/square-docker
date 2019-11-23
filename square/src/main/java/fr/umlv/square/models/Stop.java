package fr.umlv.square.models;

import fr.umlv.square.database.entities.Application;
import fr.umlv.square.serializer.StopSerializer;

import java.util.Objects;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.annotation.JsonbProperty;

public class Stop {
	private final Application app;
	@JsonbProperty("elapsed-time")
	private final String e_t;
	
	public Stop(Application app, String e_t) {
		Objects.requireNonNull(app);
		Objects.requireNonNull(e_t);
		this.app = app;
		this.e_t = e_t;
	}

	public Application getApp() {
		return this.app;
	}

	public static String serialize(Stop stop) {
		JsonbConfig config = new JsonbConfig()
				.withSerializers(new StopSerializer());
		Jsonb jsonb = JsonbBuilder.create(config);
		return jsonb.toJson(stop);
	}
}
