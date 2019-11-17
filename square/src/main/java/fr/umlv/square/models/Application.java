package fr.umlv.square.models;

import java.util.HashMap;
import java.util.Map;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;

import fr.umlv.square.serializer.ApplicationSerializer;

public class Application {
	private final int id;
	private final String app;
	private final int port;
	
	private final int service_port;
	private final String docker_instance;

	private final long startTime;
	private String elapsedTime;
	
	private String idContainer;
	
	public Application(int id, String app, int port, int serv_port, String dock_instance) {
		this.id = id;
		this.app = app;
		this.port = port;
		this.service_port = serv_port;
		this.docker_instance = dock_instance;
		this.startTime = System.currentTimeMillis();
		this.elapsedTime = "";
	}
	
	public int getId() {
		return id;
	}

	public String getAppname() {
		return this.app;
	}
	
	@JsonbTransient
	public String getIdCondtainer() {
		return this.idContainer;
	}
	
	public String getApp() {
		return app+':'+port;
	}

	public int getPort() {
		return port;
	}
	
	@JsonbProperty("service-port")
	public int getServicePort() {
		return service_port;
	}
	
	@JsonbProperty("docker-instance")
	public String getDockerInst() {
		return docker_instance;
	}

	public long getStartTime() {
		return this.startTime;
	}

	public String getElapsedTime() {
		return this.elapsedTime;
	}

	public void setElapsedTime(String value) {
		this.elapsedTime = value;
	}
	
	public Map<String, Object> toMap(){
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("app", getApp());
		map.put("port", port);
		map.put("service-port", service_port);
		map.put("docker-instance", docker_instance);
		
		return map;
	}
	
	public static String serialize(Application app) {
		JsonbConfig config = new JsonbConfig()
		        .withSerializers(new ApplicationSerializer());
		Jsonb jsonb = JsonbBuilder.create(config);
		return jsonb.toJson(app);
	}

	public void setIDContainer(String string) {
		this.idContainer = string;
		
	}
}	
