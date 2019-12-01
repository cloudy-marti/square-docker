package fr.umlv.square.database.repository;

import javax.enterprise.context.ApplicationScoped;

import fr.umlv.square.database.entities.Log;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class LogRepository implements PanacheRepository<Log> {}
