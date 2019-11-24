package fr.umlv.square.database.entities;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import fr.umlv.square.serializer.ApplicationSerializer;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class Application extends PanacheEntity {

	@Column(name = "ID_APP", nullable = false)
	private int idApp;

	@Column(name = "NAME_APP", nullable = false)
	private String app;

	@Column(name = "PORT_APP", nullable = false)
	private int port;

	@Column(name = "SERVICE_PORT", nullable = false)
	private int service_port;

	@Column(name = "DOCKER_INSTANCE", nullable = false)
	private String docker_instance;

	@Column(name = "START_TIME", nullable = false)
	private long startTime;

	@Column(name = "CONTAINER_ID", nullable = false)
	private String idContainer;

	@Column(name = "IS_ACTIVE", nullable = false)
	private Boolean isActive = true;

	@OneToMany(mappedBy = "app", cascade = CascadeType.ALL)
	Set<Log> allLogs = new HashSet<Log>();

	@Transient
	private String elapsedTime;

	public Application() {
	}

	public Application(int id, String app, int port, int serv_port, String dock_instance) {
		this.startTime = 0;
		this.idApp = id;
		this.app = app;
		this.port = port;
		this.service_port = serv_port;
		this.docker_instance = dock_instance;
		this.startTime = System.currentTimeMillis();
	}

	public void addInBDD() {
		this.persist();
	}

	public int getId() {
		return this.idApp;
	}

	public String getAppname() {
		return this.app;
	}

	@JsonbTransient
	public String getIdCondtainer() {
		return this.idContainer;
	}

	public String getApp() {
		return this.app + ':' + this.port;
	}

	public int getPort() {
		return this.port;
	}

	@JsonbProperty("service-port")
	public int getServicePort() {
		return this.service_port;
	}

	@JsonbProperty("docker-instance")
	public String getDockerInst() {
		return this.docker_instance;
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
	
	public boolean isActive() {
		return this.isActive;
	}
	
	public void setActive(boolean res) {
		this.isActive = false;
	}


	/**
	 * This method create a Json from an Application
	 * @return String which correponds to the Json of the Application
	 * @param Application
	 */
	public static String serialize(Application app) {
		JsonbConfig config = new JsonbConfig().withSerializers(new ApplicationSerializer());
		Jsonb jsonb = JsonbBuilder.create(config);
		return jsonb.toJson(app);
	}

	public void setIDContainer(String string) {
		this.idContainer = string;
	}

	/**
	 * This method checks if an ID matches with the ID of the application (this)
	 * @return true if it matches
	 * @param String id we gonna check
	 */
	public boolean matchesWithID(String id) {
		Pattern pattern = Pattern.compile("^".concat(id).concat(".*"));
		return pattern.matcher(this.idContainer).matches();
	}

	public void update() {
		this.flush();	
	}
}