package fr.umlv.square.database;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import fr.umlv.square.models.ApplicationsList;
import fr.umlv.square.models.LogsApplication;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

class LogRessources {
	@Transactional
	public static ArrayList<LogsApplication> getByTime(OffsetDateTime time, ApplicationsList appli){
		String queryString = "TIMESTAMP_LOG > ?1";
		return getData(queryString, appli, time);
	}
	
	public static List<LogsApplication> getByTimeAndFilter(OffsetDateTime time, String filter, ApplicationsList appli){
		int value;
		String queryString = "TIMESTAMP_LOG > ?1";
		if((value = isNumeric(filter)) > -1) 
			return getData(queryString, appli, time).stream().filter(e -> value == e.getApplication().getId()).collect(Collectors.toList());
		
		else if(isName(filter))
			return getData(queryString, appli, time).stream().filter(e -> e.getApplication().getApp().equals(filter)).collect(Collectors.toList());
		
		else if(isInstance(filter))
				return getData(queryString, appli, time).stream().filter(e -> e.getApplication().getDockerInst().equals(filter)).collect(Collectors.toList());
		else
			throw new IllegalArgumentException();
	}
	
	private static ArrayList<LogsApplication> getData(String queryString, ApplicationsList appli, Object... params){
		PanacheQuery<Log> query = Log.find(queryString,params);
		ArrayList<LogsApplication> array= new ArrayList<LogsApplication>(Math.toIntExact(query.count()));
		query.stream().forEach(e -> {
				array.add(new LogsApplication(e.getApp(), e.message,e.timestamp.toString()));
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