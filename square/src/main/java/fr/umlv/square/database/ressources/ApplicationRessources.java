package fr.umlv.square.database.ressources;

import java.util.stream.Stream;

import javax.transaction.Transactional;

import fr.umlv.square.database.entities.Application;
import io.quarkus.hibernate.orm.panache.PanacheQuery;


public class ApplicationRessources {
	@Transactional
	public static Stream<Application> getApplications(){
		PanacheQuery<Application> apps = Application.findAll();
		return apps.stream();
	}
	
	@Transactional
	public static void disableOneApp(Application tmpApp) {
		PanacheQuery<Application> app = Application.find("id = ?1", tmpApp.id);
		Application val = app.stream().findFirst().get();
		val.setActive(false);
		val.flush();
	}

}
