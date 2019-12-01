package fr.umlv.square.database.repository;

import javax.enterprise.context.ApplicationScoped;

import fr.umlv.square.database.entities.Log;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

/**
 * A repository for Log Entity
 * @author FAU
 *
 */
@ApplicationScoped
public class LogRepository implements PanacheRepository<Log> {
	/**
	 * Play the role of bridge between the entity and the ressource
	 */
}
