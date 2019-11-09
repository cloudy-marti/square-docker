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
		return app+':'+port;
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
	

	
	public Map<String, Object> toMap(){
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("app", getapp());
		map.put("port", port);
		map.put("service-port", service_port);
		map.put("docker-instance", docker_instance);
		
		return map;
	}
}
