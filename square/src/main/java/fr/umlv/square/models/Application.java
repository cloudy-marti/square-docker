package fr.umlv.square.models;

import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.bind.annotation.JsonbProperty;

public class Application {
	private final int id;
	private final String app;
	private final int port;
	
	private final int service_port;
	private	 final String docker_instance;
	
	public Application(int id, String app, int port, int serv_port, String dock_instance) {
		this.id = id;
		this.app = app;
		this.port = port;
		this.service_port = serv_port;
		this.docker_instance = dock_instance;
	}
	
	public int getid() {
		return id;
	}
	
	public String getapp() {
		return app;
	}
	
	public int getport() {
		return port;
	}
	
	@JsonbProperty("service-port")
	public int getserviceport() {
		return service_port;
	}
	
	@JsonbProperty("docker-instance")
	public String getDockerInst() {
		return docker_instance;
	}
	
	public JsonObject toJson() {
		JsonBuilderFactory factory = Json.createBuilderFactory(new HashMap<String, Object>());
		return factory.createObjectBuilder().
		add("id", id).
		add("app", app).
		add("port", port).
		add("service-port", service_port).
		add("docker-instance", docker_instance).
		build();		
	}
	
	public Map<String, Object> toMap(){
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("app", app);
		map.put("port", port);
		map.put("service-port", service_port);
		map.put("docker-instance", docker_instance);
		
		return map;
	}
}
