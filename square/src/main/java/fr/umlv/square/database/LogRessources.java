package fr.umlv.square.database;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Optional;

import javax.transaction.Transactional;

import fr.umlv.square.models.Application;
import fr.umlv.square.models.ApplicationsList;
import fr.umlv.square.models.LogsApplication;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

class LogRessources {
	@Transactional
	public static ArrayList<LogsApplication> getByTime(OffsetDateTime time, ApplicationsList appli){
		String queryString = "timestamp > ?1";
		return getData(queryString, appli, time);
	}
	
	public static ArrayList<LogsApplication> getByTimeAndFilter(OffsetDateTime time, String filter, ApplicationsList appli){
		int value;
		String queryString;
		String queryString2 = "and timestamp > ?2";
		if((value = isNumeric(filter)) > -1) {
			queryString = "idapp = ?1 ".concat(queryString2);
			return getData(queryString, appli, value, time);
		}
		else if(isName(filter)) {
			queryString = "appname = ?1 ".concat(queryString2);
			return getData(queryString, appli, filter, time);
		}
		else if(isInstance(filter)) {	
			queryString = "dockerinstance = ?1 ".concat(queryString2);
			return getData(queryString, appli, filter, time);			
		}
		else
			throw new IllegalArgumentException();
	}
	
	private static ArrayList<LogsApplication> getData(String queryString, ApplicationsList appli, Object... params){
		PanacheQuery<Log> query = Log.find(queryString,params);
		ArrayList<LogsApplication> array= new ArrayList<LogsApplication>(Math.toIntExact(query.count()));
		query.stream().forEach(e -> {
			var app = appli.getOneAppRunning(e.dockerInstance);
			if(app.isPresent()) {
				array.add(new LogsApplication(app.get(), e.message,e.timestamp.toString()));

			}
		});
		return array;
	}
	
	
	private static boolean isName(String filter) {
		var array = filter.split(":");
		if(array.length != 2 || isNumeric(array[1])<=-1)
			return false;
		return true;
	}
	
	private static boolean isInstance(String filter) {
		var array = filter.split("-");
		if(array.length != 2 || isNumeric(array[1])<=-1)
			return false;
		return true;
	}
	
	private static int isNumeric(String filter) {
		try {
			var value = Integer.parseInt(filter);
			return value;
		}
		catch(NumberFormatException e) {
			return -1;
		}
	}
}