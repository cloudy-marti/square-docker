package fr.umlv.square.database.repository;

import javax.enterprise.context.ApplicationScoped;

import fr.umlv.square.database.entities.Application;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

/**
 * A repository for Application Entity
 * @author FAU
 *
 */
@ApplicationScoped
public class ApplicationRepository implements PanacheRepository<Application> {
	/**
	 * Play the role of bridge between the entity and the ressource
	 */
}
