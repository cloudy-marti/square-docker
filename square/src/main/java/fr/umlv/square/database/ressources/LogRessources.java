package fr.umlv.square.database.ressources;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import fr.umlv.square.database.entities.Application;
import fr.umlv.square.database.entities.Log;
import fr.umlv.square.models.LogsApplication;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

public class LogRessources {
	/**
	 * This method get all the logs in the Databases after a specific date.
	 * @return ArrayList<LogsApplication>
	 * @param OffsetDateTime from which logs are retrieved
	 */
	@Transactional
	public static ArrayList<LogsApplication> getByTime(OffsetDateTime time){
		String queryString = "TIMESTAMP_LOG > ?1"; //$NON-NLS-1$
		return getData(queryString, time);
	}
	
	
	/**
	 * This method get all the logs in the Databases after a specific date and filtered by a filed (id, docker instance name
	 * or 'appName:port').
	 * @return List<LogsApplication>
	 * @param OffsetDateTime from which logs are retrieved and a String which is a filter
	 * @throws IllegalArgumentException
	 */
	public static List<LogsApplication> getByTimeAndFilter(OffsetDateTime time, String filter){
		int value;
		String queryString = "TIMESTAMP_LOG > ?1"; //$NON-NLS-1$
		if((value = isNumeric(filter)) > -1) 
			return getData(queryString, time).stream().filter(e -> value == e.getApplication().getId()).collect(Collectors.toList());
		
		else if(isName(filter))
			return getData(queryString, time).stream().filter(e -> e.getApplication().getApp().equals(filter)).collect(Collectors.toList());
		
		else if(isInstance(filter))
				return getData(queryString, time).stream().filter(e -> e.getApplication().getDockerInst().equals(filter)).collect(Collectors.toList());
		else
			throw new IllegalArgumentException();
	}


	private static ArrayList<LogsApplication> getData(String queryString, Object... params){
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

	/**
	 * This method update a Applications passed in the list.
	 * @param List<Application>
	 */
	@Transactional
	public static void disableApp(List<Application> appToDisable) {
		appToDisable.forEach(e -> e.flush());			
	}
}