package fr.umlv.square.database.ressources;

import java.util.stream.Stream;

import javax.transaction.Transactional;

import fr.umlv.square.database.entities.Application;
import io.quarkus.hibernate.orm.panache.PanacheQuery;


public class ApplicationRessources {
	/**
	 * This method get all the Applications in the Databases
	 * @return a Stream<Application>
	 */
	@Transactional
	public static Stream<Application> getApplications(){
		return Application.streamAll();
	}
	
	/**
	 * This method set as false a field in an Application and save it in the Databases
	 * @param the Application we want to midifie
	 */
	@Transactional
	public static void disableOneApp(Application tmpApp) {
		PanacheQuery<Application> app = Application.find("id = ?1", tmpApp.id);
		Stream<Application> stream = app.stream();
		var val = stream.findFirst().orElseThrow();
		stream.close();
		val.setActive(false);
		val.flush();
	}

}
