package fr.umlv.square.models;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.enterprise.context.ApplicationScoped;
import javax.sql.rowset.spi.SyncFactory;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.Hibernate;

import fr.umlv.square.database.Application;
import fr.umlv.square.docker.DockerDeploy;
import fr.umlv.square.utils.Counter;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@ApplicationScoped
public class ApplicationsList {
	enum IsUpToDate {
		FALSE, TRUE, IN_PROGRESS
	}

	private ArrayList<Application> list = new ArrayList<>();

	@ConfigProperty(name = "square.available.apps")
	private String appAvailable;
	private final Counter idApps = new Counter();
	private HashMap<String, Counter> deployCount = new HashMap<>();
	private IsUpToDate isUpToDate;
	private final Object lock = new Object();

	public void add(Application a, String str) {
		synchronized (this.lock) {
			this.list.add(a);
			this.idApps.add(1);
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
			Optional<Application> op = this.list.stream().filter(e -> e.matchesWithID(id)).findFirst();
			return op;
		}
	}

	public List<String> appAvailable() {
		return Arrays.asList(this.appAvailable.split(",")); //$NON-NLS-1$
	}

	public int getDeployID(String appName) {
		synchronized (this.lock) {
			int res = this.deployCount.compute(appName, (key, value) -> value == null ? new Counter(0) : value.inc())
					.getCount();
			return res;
		}
	}

	public Optional<Application> getAppById(int id) {
		synchronized (this.lock) {
			return this.list.stream().filter(app -> app.getId() == id).findFirst();
		}
	}

	public int getCount() {
		synchronized (this.lock) {
			return this.idApps.getCount();
		}
	}

	public void initApplicationsList() {
		synchronized (this.lock) {
			while (IsUpToDate.IN_PROGRESS == this.isUpToDate)
				try {
					this.lock.wait();
				} catch (InterruptedException e) {
					throw new UndeclaredThrowableException(e);
				}
			if (this.isUpToDate == IsUpToDate.TRUE)
				return;
			this.isUpToDate = IsUpToDate.IN_PROGRESS;
			var listBDD_ = Application.getAllApps().collect(Collectors.toList());
			this.idApps.add(listBDD_.size());
			listBDD_ = listBDD_.stream().filter(e -> e.isActive()).collect(Collectors.toList());
			this.initHashMap(listBDD_);
			this.initListApp(listBDD_);
			this.isUpToDate = IsUpToDate.TRUE;
			this.lock.notifyAll();
		}
	}

	private void initListApp(List<Application> list) {
		synchronized (this.	lock) {
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
			Application.disableApp(appToDisable);
		}
	}

	private void initHashMap(List<Application> list) {
		synchronized (this.lock) {
			Arrays.asList(this.appAvailable.split(",")).forEach(name -> {
				var max = list.stream().filter(e -> e.getAppname().equals(name))
						.flatMapToInt(e -> IntStream.of(Integer.parseInt(e.getDockerInst().split("-")[1]))).max();
				this.deployCount.compute(name, (key, value) -> new Counter(max.isPresent() ? max.getAsInt() : 0));
			});
		}
	}

	public void deleteApp(Application tmpApp) {
		synchronized (this.lock) {
			this.list.remove(tmpApp);
			tmpApp.setActive(false);
			//tmpApp.update();
		}		
	}
}
