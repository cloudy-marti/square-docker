package fr.umlv.square.models;

import java.util.HashMap;
import java.util.Map;

public class AutoScale {
	private final Map<String, Integer> autoScale;
	private final Map<String, String> statusMap;

	private boolean running;
	
	public AutoScale() {
		this.autoScale = new HashMap<>();
		this.statusMap = new HashMap<>();
		this.running = false;
	}
	
	public Map<String, Integer> getAutoScale() {
		return this.autoScale;
	}

	public void addToAutoScale(String key, int value) {
		this.autoScale.put(key, value);
	}
	
	public Map<String, Integer> getMap(){
		return this.autoScale;
	}

	public void clearStatus() {
		this.statusMap.clear();
	}

	public void addToStatus(String key, String value) {
		this.statusMap.put(key, value);
	}

	public Map<String, String> getStatusMap() {
		return this.statusMap;
	}

	public boolean isAutoScaleRunning() {
		return this.running;
	}

	public void startAutoScale() {
		this.running = true;
	}

	public void stopAutoScale() {
		this.running = false;
	}
}
