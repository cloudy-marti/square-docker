package fr.umlv.square.database.logs;

import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;

import fr.umlv.square.models.LogsApplication;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import javax.json.JsonObject;

@Entity
public class Log extends PanacheEntity {
    @NotBlank
    public String message;
    @NotBlank
    public OffsetDateTime timestamp;
    @NotBlank
	public String dockerInstance;
	
	private Log(String dockerI, String message, OffsetDateTime timestamp) {
		Objects.requireNonNull(dockerI);
		Objects.requireNonNull(message);
		Objects.requireNonNull(timestamp);
		this.message = message;
		this.timestamp = timestamp;
		this.dockerInstance = dockerI;
	}
	
	public Log() {}
	
	

	public static boolean addLogs(List<JsonObject> obj, String appName) {
		ArrayList <Log> l = new ArrayList<Log>();
		for(JsonObject elem : obj) {
			System.out.println(elem.get("date").toString());
			l.add(new Log(
					appName, 
					String.valueOf(elem.get("message")),
					OffsetDateTime.parse(elem.get("date").toString().replace('"',' ').trim())));
		}
		Log.persist(l.stream());
		return true;
	}

	public static ArrayList<LogsApplication> getByTime(int timer) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        Instant instant = Instant.parse(df.format(new Date()));
        OffsetDateTime time = OffsetDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));
        time = time.minusMinutes(timer);
		return LogRessources.getByTime(time);		
	}

}
