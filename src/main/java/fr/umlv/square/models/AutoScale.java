package fr.umlv.square.models;

import java.util.HashMap;
import java.util.Map;

public class AutoScale {
	private Map<String, Object> autoScale;
	
	public AutoScale() {
		autoScale = new HashMap<String, Object>();
	}
	
	public Map<String, Object> getAutoScale() {
		return autoScale;
	}
	public void addToMap(String key, Object value) {
		autoScale.put(key, value);
	}
	
	public Map<String, Object> getMap(){
		return autoScale;
	}
}
