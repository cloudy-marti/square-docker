package fr.umlv.square.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.annotation.JsonbTransient;

import org.hibernate.mapping.Array;

import fr.umlv.square.database.entities.Application;


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
	
	/**
	 * This method serialize a list of LogsApplication.
	 * @return String which is the Json of the List.
	 * @param List<LogsApplication> 
	 */
	public static ArrayList<JsonObject> listToJson(List<LogsApplication>list){
		ArrayList<JsonObject> array = new ArrayList<>(list.size());
		for(var elem : list)
			array.add(serialize(elem));
		return array;

	}
	
	private static JsonObject serialize(LogsApplication obj) {
		JsonObject value = 
				Json.createObjectBuilder().
				add("id", obj.app.getId()).
		        add("app", obj.app.getApp()).
		        add("port", obj.app.getPort()).
		        add("service-port", obj.app.getServicePort()).
		        add("message", obj.message).
		        add("timestamp", obj.timestamp).
		        build();
		return value;
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
