package fr.umlv.square.database.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.JsonObject;

@Entity
public class Log extends PanacheEntity {

	@Column(name = "MESSAGE_LOG", nullable = false,length = 555)
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
	

	/**
	 * This method saves logs into the database
	 * @return boolean : true if all was ok
	 * @param app : Application for which we will save the logs
	 * @param obj : List<JsonObject>, from this list we will Log and push it to the databases
	 */
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

	public Application getApp() {
		return this.app;
	}

}
