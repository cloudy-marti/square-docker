package fr.umlv.square.database.ressources;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import fr.umlv.square.database.entities.Application;
import fr.umlv.square.database.entities.Log;
import fr.umlv.square.database.repository.LogRepository;
import fr.umlv.square.models.LogsApplication;

@ApplicationScoped
public class LogRessources {
	
	private final LogRepository repo;
	
	@Inject
	public LogRessources(LogRepository rep) {
		this.repo = rep;
	}
	
	/**
	 * This method get all the logs in the Databases after a specific date.
	 * @return ArrayList<LogsApplication>
	 * @param time from which logs are retrieved
	 */
	@Transactional
	public ArrayList<LogsApplication> getByTime(OffsetDateTime time){
		String queryString = "TIMESTAMP_LOG > ?1";
		return getData(queryString, time);
	}
	
	
	/**
	 * This method get all the logs in the Databases after a specific date and filtered by a filed (id, docker instance name
	 * or 'appName:port').
	 * @return List<LogsApplication>
	 * @param time : OffsetDateTime from which logs are retrieved 
	 * @param filter : String which is a filter
	 */
	public List<LogsApplication> getByTimeAndFilter(OffsetDateTime time, String filter) {
		int value;
		String queryString = "TIMESTAMP_LOG > ?1";
		if((value = isNumeric(filter)) > -1) 
			return getData(queryString, time).stream().filter(e -> value == e.getApplication().getId()).collect(Collectors.toList());
		
		else if(isName(filter))
			return getData(queryString, time).stream().filter(e -> e.getApplication().getApp().equals(filter)).collect(Collectors.toList());
		
		else if(isInstance(filter))
				return getData(queryString, time).stream().filter(e -> e.getApplication().getDockerInst().equals(filter)).collect(Collectors.toList());
		else
			throw new IllegalArgumentException();
	}


	private ArrayList<LogsApplication> getData(String queryString, Object... params) {
		List<Log> query = this.repo.list(queryString,params);
		ArrayList<LogsApplication> array= new ArrayList<>();
		query.forEach(e -> array.add(new LogsApplication(e.getApp(), e.getMessage(),e.getTimeStamp().toString())));
		return array;
	}
	
	private boolean isName(String filter) {
		var array = filter.split(":");
		if(array.length != 2 || isNumeric(array[1])<=-1)
			return false;
		return true;
	}
	
	private boolean isInstance(String filter) {
		var array = filter.split("-");
		if(array.length != 2 || isNumeric(array[1])<=-1)
			return false;
		return true;
	}
	
	private int isNumeric(String filter) {
		try {
			return Integer.parseInt(filter);
		}
		catch(NumberFormatException e) {
			return -1;
		}
	}

	/**
	 * This method update a Applications passed in the list.
	 * @param appToDisable
	 */
	@Transactional
	public void disableApp(List<Application> appToDisable) {
		appToDisable.forEach(e -> e.flush());			
	}
}