package fr.umlv.square.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import fr.umlv.square.database.entities.Application;
import fr.umlv.square.database.ressources.ApplicationRessources;
import fr.umlv.square.database.ressources.LogRessources;
import fr.umlv.square.docker.DockerDeploy;
import fr.umlv.square.utils.SynchronizedCounter;

@ApplicationScoped
public class ApplicationsList {
	enum IsUpToDate {
		FALSE, TRUE, IN_PROGRESS
	}

	private final ArrayList<Application> list = new ArrayList<>();

	private final ArrayList<String> appAvailable = new ArrayList<>();
	private final SynchronizedCounter idApps = new SynchronizedCounter(1);
	private HashMap<String, SynchronizedCounter> deployCount = new HashMap<>();
	private IsUpToDate isUpToDate;
	private final Object lock = new Object();
	
	private final ApplicationRessources appRessource;
	private final LogRessources logRessource;

	/**
	 * Constructor
	 * @param names get value from application.properties which is a string of the available apps
	 * @param app injected ApplicationRessources 
	 * @param logR injected LogRessources
	 */
	@Inject
	public ApplicationsList(@ConfigProperty(name = "square.available.apps") String names, ApplicationRessources app, LogRessources logR) {
		this.appRessource = app;
		this.logRessource = logR;
		this.appAvailable.addAll(Arrays.asList(names.split(",")));
	}
	
	/**
	 * this method add the app in the list and also increment the Counter.
	 * @param app to be added
	 */
	public void add(Application app) {
		synchronized (this.lock) {
			this.list.add(app);
			this.deployCount.get(app.getApp()).incCurrentNumber();
		}
	}

	public ArrayList<Application> getList() {
		synchronized (this.lock) {
			return new ArrayList<>(this.list);
		}
	}


	/**
	 * 
	 * @param dockerInstance, the docker instance name of the app
	 * @return an Optional<Applicaton>
	 */
	public Optional<Application> getOneAppRunning(String dockerInstance) {
		synchronized (this.lock) {
			Optional<Application> op = this.list.stream().filter(e -> dockerInstance.equals(e.getDockerInst()))
					.findFirst();
			return op;
		}
	}

	/**
	 * 
	 * @param id of the app
	 * @return an Optional<Applicaton>
	 */
	public Optional<Application> getOneAppRunningByID(String id) {
		synchronized (this.lock) {
			return this.list.stream().filter(e -> e.matchesWithID(id)).findFirst();
		}
	}

	public List<String> appAvailable() {
		return this.appAvailable;
	}

	/**
	 * This methods returns the ID of the container depending on the name of the app and her port
	 * @param appName
	 * @param port
	 * @return the id that the app will have
	 */
	public int getDeployID(String appName, int port) {
		synchronized (this.lock) {
			this.deployCount.compute(appName + ":" + port, (key, value) -> value == null ? new SynchronizedCounter(1) : value.inc());
			int res = 0;
			for(var key : this.deployCount.keySet()) {
				res += key.split(":")[0].equals(appName) ? this.deployCount.get(key).getCount() : 0; 
			}
			return res;
		}
	}

	/**
	 * 
	 * @param id of the app
	 * @return an Optional<Application> to describe that we don't know if the app is present
	 */
	public Optional<Application> getAppById(int id) {
		synchronized (this.lock) {
			return this.list.stream().filter(app -> app.getId() == id).findFirst();
		}
	}

	/**
	 * 
	 * @param name of the app
	 * @return an Optional<Application> to describe that we don't know if the app is present
	 */
	public Optional<Application> getAppByNameAndPort(String name) {
		synchronized (this.lock) {
			return this.list.stream().filter(app -> app.getApp().equals(name)).findFirst();
		}
	}

	/**
	 * 
	 * @param name of the app
	 * @return the total of application deployed with this name
	 */
	public int getCountByNameAndPort(String name) {
		synchronized (this.lock) {
			var c = this.deployCount.get(name);
			return c != null ? c.getCurrentNumber() : 0;
		}
	}

	public int getCount() {
		synchronized (this.lock) {
			return this.idApps.getCount();
		}
	}

	/**
	 * A wrapper to init all fields from the dataBase
	 */
	public void wrapperInit() {
		synchronized (this.lock) {
			while (IsUpToDate.IN_PROGRESS == this.isUpToDate)
				try {
					this.lock.wait();
				} catch (InterruptedException e) {
					throw new AssertionError(e);
				}
			if (this.isUpToDate == IsUpToDate.TRUE)
				return;
			this.isUpToDate = IsUpToDate.IN_PROGRESS;
			this.initApplicationsList();
		}
	}

	private void initApplicationsList() {
		try(var list = this.appRessource.getApplications();){
			var listBDD_ = list.collect(Collectors.toList());
			if (listBDD_.size() != 0)
				this.complexInit(listBDD_);
		}
		this.isUpToDate = IsUpToDate.TRUE;
		this.lock.notifyAll();

	}

	private void complexInit(List<Application> listBDD_) {
		this.idApps.add(listBDD_.stream().map(e -> e.getId()).max(Comparator.naturalOrder()).get());
		var listStreamed = listBDD_.stream().filter(e -> e.isActive()).collect(Collectors.toList());
		this.initHashMap(listBDD_);
		this.initListApp(listStreamed);
		this.initCurrentNumberApps();

	}
	
	private void initCurrentNumberApps() {
		for(var elem : this.list) {
			this.deployCount.get(elem.getApp()).incCurrentNumber();	
		}
		
	}

	private void initListApp(List<Application> list) {
		var listPS = DockerDeploy.getRunningInstancesNames();
		var appToDisable = list.stream().filter(e -> {
			if (e.isActive()) {
				if (listPS.contains(e.getDockerInst()))
					this.list.add(e);
				else {
					e.setActive(false);
					return true;
				}
			}
			return false;
		}).collect(Collectors.toList());
		this.logRessource.disableApp(appToDisable);	

	}

	private void initHashMap(List<Application> list) {
		var copy = new ArrayList<>(list);
		List<Application> tmp = new ArrayList<>();
		while(!copy.isEmpty()) {
			var elem = copy.get(0);
			tmp = copy.stream().filter(e -> e.getApp().equals(elem.getApp())).collect(Collectors.toList());
			int size = tmp.size();
			this.deployCount.compute(elem.getApp(), (key, value) -> new SynchronizedCounter(size));
			copy.removeAll(tmp);
		}
	}

	/**
	 * Delete an application
	 * @param tmpApp the application to be deleted
	 */
	@Transactional
	public void deleteApp(Application tmpApp) {
		synchronized (this.lock) {
			this.list.remove(tmpApp);
			tmpApp.setActive(false);
			this.appRessource.disableOneApp(tmpApp);
			this.deployCount.get(tmpApp.getApp()).decCurrentNumber();
		}
	}

	/**
	 * put to sleep the thread while the initialisation of all fields are done
	 */
	public void waitStillInit() {
		synchronized (this.lock) {
			while(this.isUpToDate != IsUpToDate.TRUE) {
				try {
					this.lock.wait();
				} catch (InterruptedException e) {
					throw new AssertionError();
				}
			}
		}
		
	}

	public int getCountAndInc() {
		synchronized (this.lock) {
			var value = this.idApps.getCount();
			this.idApps.inc();
			return value;
		}
	}
}
