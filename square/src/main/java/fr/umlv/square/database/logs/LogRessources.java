package fr.umlv.square.database.logs;

import java.time.OffsetDateTime;
import java.util.ArrayList;

import javax.transaction.Transactional;

import fr.umlv.square.models.Application;
import fr.umlv.square.models.ApplicationsList;
import fr.umlv.square.models.LogsApplication;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

class LogRessources {
	@Transactional
	public static ArrayList<LogsApplication> getByTime(OffsetDateTime time, ApplicationsList appli){
		String queryString = "timestamp > ?1";
		PanacheQuery<Log> query = Log.find(queryString,time);
		ArrayList<LogsApplication> array= new ArrayList<LogsApplication>(Math.toIntExact(query.count()));
		query.stream().forEach(e -> {
			Application a = appli.getOneAppRunning(e.dockerInstance);
			if(a != null)
				array.add(new LogsApplication(a, e.message,e.timestamp.toString()));
		});
		return array;
	}
	
	public static ArrayList<LogsApplication> getByTimeAndFilter(OffsetDateTime time, String filter, ApplicationsList appli){
		
		return null;
	}
}
