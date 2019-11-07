package fr.umlv.square.database.logs;

import javax.transaction.Transactional;
import javax.validation.Valid;

class LogRessources {
	@Transactional
	public static void create(@Valid Log log) {
		log.persist();
	}
}
