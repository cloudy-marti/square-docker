package fr.umlv.square.database.logs;

import java.time.OffsetDateTime;
import java.util.ArrayList;

import javax.transaction.Transactional;

import fr.umlv.square.controllers.ApplicationsListRoute;
import fr.umlv.square.controllers.LogsListRoute;
import fr.umlv.square.models.LogsApplication;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

class LogRessources {
	@Transactional
	public static ArrayList<LogsApplication> getByTime(OffsetDateTime time){
		String queryString = "timestamp > ?1";
		System.out.println(time);
		PanacheQuery<Log> query = Log.find(queryString,time);
		ArrayList<LogsApplication> array= new ArrayList<LogsApplication>(Math.toIntExact(query.count()));
		query.stream().forEach(e -> {
			Application a = ApplicationsListRoute.getOneAppRunning(e.dockerInstance);
			array.add(new LogsApplication(null, e.message,e.timestamp.toString()));
		});
		return array;
	}
}
