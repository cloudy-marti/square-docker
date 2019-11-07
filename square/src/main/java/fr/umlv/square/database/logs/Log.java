package fr.umlv.square.database.logs;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import static javax.persistence.GenerationType.SEQUENCE;

import java.util.List;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObject;
import javax.json.JsonValue;

@Entity
public class Log extends PanacheEntity {
    @NotBlank
	public final String appName;
    @NotBlank
    public final String message;
    @NotBlank
    public final String timestamp;
	
	private Log(String appName, String message, String timestamp) {
		this.appName = appName;
		this.message = message;
		this.timestamp = timestamp;
	}

	public static void addLogs(List<JsonObject> obj, String appName) {
		Log l = new Log(appName,"test", "timestamp");
		l.persist();
//		obj.forEach(e -> e.entrySet().forEach(f -> createOne(f, appName)));
	}
//	
//	@Transactional
//	private static void createOne(Entry<String, JsonValue> obj, String appName) {
//		Log log = new Log(appName,"ok","dd");
//		LogRessources.create(log);
//	}
}
