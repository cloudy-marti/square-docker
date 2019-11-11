package fr.umlv.square.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class LogsApplication {
	private final Application app;
	private final String message;
	private final String timestamp;
	
	public LogsApplication(Application app, String message, String tS){
		Objects.requireNonNull(app);
		Objects.requireNonNull(message);
		Objects.requireNonNull(tS);
		
		this.app = app;
		this.message = message;
		this.timestamp = tS;
	}
	
	public static List<Map<String, Object>> getListMapped(List<LogsApplication>list){
		List<Map<String, Object>> list_parsed = new ArrayList<Map<String, Object>>();
		for(LogsApplication log : list) 
			list_parsed.add(log.toMap());
		return list_parsed;

	}
	public Map<String, Object> toMap(){
		Map<String, Object> m = this.app.toMap();
		m.put("message", message);
		m.put("timestamp", timestamp);
		return m;
	}
}
