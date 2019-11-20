package fr.umlv.square.lifeBean;


import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;

import fr.umlv.square.database.Application;
import fr.umlv.square.models.ApplicationsList;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class AppLifecycleBean {
	
	private final ApplicationsList app;
	
	@Inject
	public AppLifecycleBean(ApplicationsList appL) {
		this.app = appL;
	}
	
	@Transactional
	void onStart(@Observes StartupEvent ev) {
		this.app.wrapperInit();
	}

}