package fr.umlv.square.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

	@Column(name = "MESSAGE_LOG", nullable = false)
	public String message;
	
	@Column(name = "TIMESTAMP_LOG", nullable = false)
	public OffsetDateTime timestamp;
	
    @ManyToOne
    @JoinColumn(name ="FK_APPLICATION")
    private Application app;
   	
	private Log(String message, OffsetDateTime timestamp, Application app) {
		Objects.requireNonNull(message);
		Objects.requireNonNull(app);
		Objects.requireNonNull(timestamp);
		this.message = message;
		this.timestamp = timestamp;
		this.app = app;
	}
	
	public Log() {}
	


	@SuppressWarnings("static-access")
	public static boolean addLogs(List<JsonObject> obj, Application app) {
		ArrayList <Log> l = new ArrayList<Log>();
		for(JsonObject elem : obj) {
			String message = String.valueOf(elem.get("message")); //$NON-NLS-1$
			String defaultDate = elem.get("date").toString().replace('"',' ').trim(); //$NON-NLS-1$
			message = message.substring(1, message.lastIndexOf("\"")); //$NON-NLS-1$
			
			OffsetDateTime date = getDateOrDefault(message, defaultDate);
			l.add(new Log(
					message,
					date, 
					app
				));
		}
		Log.persist(l.stream());
		return true;
	}
	
	private static OffsetDateTime getDateOrDefault(String message, String defaultDate) {
		Pattern pattern = Pattern.compile("[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1]) (2[0-3]|[01][0-9]):[0-5][0-9]:[0-5][0-9]"); //$NON-NLS-1$
		Matcher matcher = pattern.matcher(message);
		String find;
		OffsetDateTime o1, o2 = OffsetDateTime.parse(defaultDate);;
		if (matcher.find())
		{
		    find = matcher.group(0);
		    find = find.replace(" ", "T").concat("Z");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		    o1 = OffsetDateTime.parse(find);
		    var timer = ChronoUnit.SECONDS.between(o1, o2);
		    if(timer <= 60 && timer >= -60)
		    	return o1;
		}
		return o2;
	}
	
	private static OffsetDateTime getTimed(int timer) {
        TimeZone tz = TimeZone.getTimeZone("UTC"); //$NON-NLS-1$
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); //$NON-NLS-1$
        df.setTimeZone(tz);
        Instant instant = Instant.parse(df.format(new Date()));
        OffsetDateTime time = OffsetDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));	
        return time.minusMinutes(timer);	
	}

	public static ArrayList<LogsApplication> getByTime(int timer) {
		return LogRessources.getByTime(getTimed(timer));		
	}

	public static List<LogsApplication> getByTimeAndFilter(int time, String filter) {
		return LogRessources.getByTimeAndFilter(getTimed(time), filter);	
	}

	public Application getApp() {
		return this.app;
	}

}
