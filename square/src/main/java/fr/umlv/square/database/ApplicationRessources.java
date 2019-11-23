package fr.umlv.square.database;

import java.util.stream.Stream;

import javax.transaction.Transactional;

import io.quarkus.hibernate.orm.panache.PanacheQuery;


class ApplicationRessources {
	@Transactional
	public static Stream<Application> getApplications(){
		PanacheQuery<Application> apps = Application.findAll();
		return apps.stream();
	}

}
