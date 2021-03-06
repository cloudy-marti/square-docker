package fr.umlv.square.models;

import static fr.umlv.square.controllers.ApplicationEndPoint.getFromJson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;


@ApplicationScoped
public class AutoScale {

	private static final String actionTemplate;
	private static final String noAction;

	static {
		actionTemplate = "need to %s %s instance(s)";
		noAction = "no action";
	}

	private final ApplicationsList appList;

	private final Map<String, Integer> autoScale;
	private final Map<String, String> statusMap;

	private final Object lock = new Object();
	private boolean running;
	private boolean isFree;

	/**
	 * 
	 * @param appList injected ApplicationsList
	 */
	@Inject
	public AutoScale(ApplicationsList appList) {
		synchronized (this.lock) {
			this.appList = appList;
			this.isFree = true;
			this.autoScale = new HashMap<>();
			this.statusMap = new HashMap<>();
		}
	}

	private void clearStatus() {
		this.statusMap.clear();
	}

	private void addToStatus(String key, String value) {
		this.statusMap.put(key, value);
	}

	/**
	 * Return what the auto scale need to do for each applications as a String
	 * @return StatusMap
	 */
	private Map<String, String> getStatusMap() {
		synchronized (this.lock) {
			return this.statusMap;
		}
	}

	public boolean isAutoScaleRunning() {
		synchronized (this.lock) {
			return this.running;
		}
	}

	private void stopAutoScale() {
		if (!this.running)
			throw new IllegalStateException();
		this.running = false;
	}

	private boolean parseJsonToAddInMap(JsonObject obj, List<String> list) {
		for (var key : obj.keySet()) {
			String[] name = key.split(":");
			if (name.length != 2)
				return false;
			if (!list.contains(name[0]) || Integer.parseInt(name[1]) < 0)
				return false;
			String[] str = getFromJson(obj, key);
			int number = Integer.parseInt(str[0]);
			if (number < 0)
				return false;
			this.autoScale.put(key, number);
		}
		return true;

	}

	/**
	 * Set the auto-scale to up. Only one thread can update it at a time
	 * @param obj, the json who contains what the auto scale need to do for each apps
	 * @param appList List of all applications
	 * @return Map needed by the autoscale
	 */
	public Map<String, String> updateAutoScale(JsonObject obj, ApplicationsList appList) {
		synchronized (this.lock) {
			this.autoScale.clear();
			if (!this.parseJsonToAddInMap(obj, appList.appAvailable()))
				throw new IllegalArgumentException();
			this.running = true;
			this.clearStatus();
			this.updateStatus(appList);
			return this.getStatusMap();
		}
	}

	/**
	 * Wrapper to update the status
	 * @param appList List of all Applications
	 * @return Map of the autoscale
	 */
	public Map<String, String> wrapperUpdateStatus(ApplicationsList appList) {
		synchronized (this.lock) {
			this.updateStatus(appList);
			return this.getStatusMap();
		}
	}

	private int updateStatus(ApplicationsList appList) {
		this.autoScale.forEach((key, autoScaleValue) -> {
			int instances = appList.getCountByNameAndPort(key);
			String statusValue = instances == autoScaleValue ? noAction
					: autoScaleActionString(instances - autoScaleValue);
			this.addToStatus(key, statusValue);
		});
		return 200;
	}

	private static String autoScaleActionString(int diff) {
		if (diff < 0) { // "need to start <-diff> instances"
			return String.format(actionTemplate, "start", -diff);
		} else { // "need to stop <diff> instances"
			return String.format(actionTemplate, "stop", diff);
		}
	}

	/**
	 * Wrapper to stop autoScale
	 * @return a map containing what the auto scale is setup for
	 */
	public Map<String, Integer> WrapperStopAutoScale() {
		synchronized (this.lock) {
			this.stopAutoScale();
			return this.autoScale;
		}
	}

	/**
	 * This method try to update the autoScale. Only one thread can do it at a time
	 * Calling checkIsFree() to see if he can update
	 * @return Map of the instances needed by the autoscale
	 */
	public Map<String, Integer> tryUpdating() {
		synchronized (this.lock) {
			var map = new HashMap<String, Integer>();
			if(this.running) {
				this.checkIsFree();
				this.autoScale.forEach((key, autoScaleValue) -> {
					int instances = this.appList.getCountByNameAndPort(key);
					map.put(key, instances - autoScaleValue);
				});
			}
			return map;

		}
	}

	/**
	 * This method put in sleep threads while one is updating the auto-scale.
	 */
	private void checkIsFree() {
		synchronized (this.lock) {
			if(this.isFree) {
				this.isFree = false;
			}
			else{
				while(!this.isFree) {
					try {
						this.lock.wait();
					} catch (InterruptedException e) {
						throw new AssertionError();
					}
				}
			}
		}
	}
	
	public void isFree() {
		synchronized (this.lock) {
			this.isFree  = true;
			this.lock.notifyAll();
		}		
	}	
}
