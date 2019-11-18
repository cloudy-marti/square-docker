package fr.umlv.square.database;

import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.ForeignKey;

import fr.umlv.square.models.ApplicationsList;
import fr.umlv.square.models.LogsApplication;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.JsonObject;

@Entity
public class Log extends PanacheEntity {
    @NotBlank
    public String message;
    @NotBlank
    public OffsetDateTime timestamp;
    @NotBlank
	public String dockerInstance;
	@NotBlank
	public int idApp;
	@NotBlank
	public String appName;
	
	private Log(String dockerI, String message, OffsetDateTime timestamp, int idApp, String appName) {
		Objects.requireNonNull(dockerI);
		Objects.requireNonNull(message);
		Objects.requireNonNull(timestamp);
		Objects.requireNonNull(idApp);
		Objects.requireNonNull(appName);
		this.message = message;
		this.timestamp = timestamp;
		this.dockerInstance = dockerI;
		this.idApp = idApp;
		this.appName = appName;
	}
	
	public Log() {}
	
	


	public static boolean addLogs(List<JsonObject> obj, Application app) {
		ArrayList <Log> l = new ArrayList<Log>();
		for(JsonObject elem : obj) {
			String message = String.valueOf(elem.get("message"));
			String defaultDate = elem.get("date").toString().replace('"',' ').trim();
			message = message.substring(1, message.lastIndexOf("\""));
			
			OffsetDateTime date = getDateOrDefault(message, defaultDate);
			l.add(new Log(
					app.getDockerInst(), 
					message,
					date,
					app.getId(),
					app.getAppname()+":"+app.getPort()					
				));
		}
		Log.persist(l.stream());
		return true;
	}
	
	private static OffsetDateTime getDateOrDefault(String message, String defaultDate) {
		Pattern pattern = Pattern.compile("[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1]) (2[0-3]|[01][0-9]):[0-5][0-9]:[0-5][0-9]");
		Matcher matcher = pattern.matcher(message);
		String find;
		OffsetDateTime o1, o2 = null;
		if (matcher.find())
		{
		    find = matcher.group(0);
		    find = find.replace(" ", "T").concat("Z");
		    o1 = OffsetDateTime.parse(find);
		    o2 = OffsetDateTime.parse(defaultDate);
		    var timer = ChronoUnit.SECONDS.between(o1, o2);
		    if(timer <= 60 && timer >= -60)
		    	return o1;
		}
		return o2;
	}
	
	private static OffsetDateTime getTimed(int timer) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);
        Instant instant = Instant.parse(df.format(new Date()));
        OffsetDateTime time = OffsetDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));	
        return time.minusMinutes(timer);	
	}

	public static ArrayList<LogsApplication> getByTime(int timer, ApplicationsList appli) {
		return LogRessources.getByTime(getTimed(timer), appli);		
	}

	public static List<LogsApplication> getByTimeAndFilter(int time, String filter, ApplicationsList listApp) {
		return LogRessources.getByTimeAndFilter(getTimed(time), filter, listApp);	
	}

}
