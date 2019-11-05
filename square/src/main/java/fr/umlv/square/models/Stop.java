package fr.umlv.square.models;

import java.util.Map;
import java.util.Objects;

import javax.json.bind.annotation.JsonbProperty;

public class Stop {
	private final Application appli;
	@JsonbProperty("elapsed-time")
	private final String e_t;
	
	public Stop(Application app, String e_t) {
		Objects.requireNonNull(app);
		Objects.requireNonNull(e_t);
		this.appli = app;
		this.e_t = e_t;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> js = appli.toMap();
		js.put("elapsed-time",e_t);
		return js;
	}
}
