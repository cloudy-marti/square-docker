package fr.umlv.square.models;

import java.util.List;
import java.util.Objects;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.annotation.JsonbTransient;

import fr.umlv.square.database.Application;
import fr.umlv.square.serializer.LogsApplicationSerializer;


public class LogsApplication {
	private final Application app;
	private final String message;
	private final String timestamp;
	
	public LogsApplication(Application app, String message, String tS){
		Objects.requireNonNull(app);
		Objects.requireNonNull(message);
		Objects.requireNonNull(tS);
		
		this.app = app;
		this.message = message;
		this.timestamp = tS;
	}
	
	public static String listToJson(List<LogsApplication>list){
		StringBuilder str = new StringBuilder();
		for(var elem : list)
			str.append(LogsApplication.serialize(elem));
		return str.toString();

	}
	private static String serialize(LogsApplication app) {
		JsonbConfig config = new JsonbConfig()
		        .withSerializers(new LogsApplicationSerializer());
		Jsonb jsonb = JsonbBuilder.create(config);
		return jsonb.toJson(app);
	}

	@JsonbTransient
	public Application getApplication() {
		return this.app;
	}
	
	@JsonbTransient
	public String getMessage() {
		return this.message;
	}
	
	@JsonbTransient
	public String getTimestamp() {
		return this.timestamp;
	}
}
