package fr.umlv.square.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import fr.umlv.square.database.entities.Application;
import fr.umlv.square.database.ressources.ApplicationRessources;
import fr.umlv.square.database.ressources.LogRessources;
import fr.umlv.square.docker.DockerDeploy;
import fr.umlv.square.utils.Counter;

@ApplicationScoped
public class ApplicationsList {
	enum IsUpToDate {
		FALSE, TRUE, IN_PROGRESS
	}

	private final ArrayList<Application> list = new ArrayList<>();

	private final ArrayList<String> appAvailable = new ArrayList<>();
	private final Counter idApps = new Counter(1);
	private HashMap<String, Counter> deployCount = new HashMap<>();
	private IsUpToDate isUpToDate;
	private final Object lock = new Object();

	public ApplicationsList(@ConfigProperty(name = "square.available.apps") String names) {
		this.appAvailable.addAll(Arrays.asList(names.split(",")));
	}

	public void add(Application a, String str) {
		synchronized (this.lock) {
			this.list.add(a);
			this.idApps.add(1);
			this.deployCount.get(a.getApp()).incCurrentNumber();
		}
	}

	public ArrayList<Application> getList() {
		synchronized (this.lock) {
			return new ArrayList<>(this.list);
		}
	}

	public Optional<Application> getOneAppRunning(String dockerInstance) {
		synchronized (this.lock) {
			Optional<Application> op = this.list.stream().filter(e -> dockerInstance.equals(e.getDockerInst()))
					.findFirst();
			return op;
		}
	}

	public Optional<Application> getOneAppRunningByID(String id) {
		synchronized (this.lock) {
			return this.list.stream().filter(e -> e.matchesWithID(id)).findFirst();
		}
	}

	public List<String> appAvailable() {
		return this.appAvailable;
	}

	public int getDeployID(String appName, int port) {
		synchronized (this.lock) {
			this.deployCount.compute(appName + ":" + port, (key, value) -> value == null ? new Counter(1) : value.inc());
			int res = 0;
			for(var key : this.deployCount.keySet()) {
				res += key.split(":")[0].equals(appName) ? this.deployCount.get(key).getCount() : 0; 
			}
			return res;
		}
	}

	public Optional<Application> getAppById(int id) {
		synchronized (this.lock) {
			return this.list.stream().filter(app -> app.getId() == id).findFirst();
		}
	}

	public Optional<Application> getAppByNameAndPort(String name) {
		synchronized (this.lock) {
			return this.list.stream().filter(app -> app.getApp().equals(name)).findFirst();
		}
	}

	public long getCountByNameAndPort(String name) {
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
		var list = ApplicationRessources.getApplications();
		var listBDD_ = list.collect(Collectors.toList());
		list.close();
		if (listBDD_.size() != 0)
			this.complexInit(listBDD_);
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
		LogRessources.disableApp(appToDisable);	

	}

	private void initHashMap(List<Application> list) {
		var copy = new ArrayList<>(list);
		List<Application> tmp = new ArrayList<>();
		while(!copy.isEmpty()) {
			var elem = copy.get(0);
			tmp = copy.stream().filter(e -> e.getApp().equals(elem.getApp())).collect(Collectors.toList());
			int size = tmp.size();
			this.deployCount.compute(elem.getApp(), (key, value) -> new Counter(size));
			copy.removeAll(tmp);
		}
	}

	@Transactional
	public void deleteApp(Application tmpApp) {
		synchronized (this.lock) {
			this.list.remove(tmpApp);
			tmpApp.setActive(false);
			ApplicationRessources.disableOneApp(tmpApp);
			this.deployCount.get(tmpApp.getApp()).decCurrentNumber();
		}
	}

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

	@Transactional
	public void test() {
		Application.streamAll().close();
		
	}
}
