package fr.umlv.square.database.ressources;

import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import fr.umlv.square.database.entities.Application;
import fr.umlv.square.database.repository.ApplicationRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

@ApplicationScoped
public class ApplicationRessources {
	
	private final ApplicationRepository repo;
	
	@Inject
	public ApplicationRessources(ApplicationRepository rep) {
		this.repo = rep;
	}
	/**
	 * This method get all the Applications in the Databases
	 * @return a Stream<Application>
	 */
	@Transactional
	public Stream<Application> getApplications(){
		return this.repo.streamAll();
	}
	
	/**
	 * This method set as false a field in an Application and save it in the Databases
	 * @param the Application we want to midifie
	 */
	@Transactional
	public void disableOneApp(Application tmpApp) {
		PanacheQuery<Application> app = this.repo.find("id = ?1", tmpApp.id);
		Stream<Application> stream = app.stream();
		var val = stream.findFirst().orElseThrow();
		stream.close();
		val.setActive(false);
		val.flush();
	}

}
