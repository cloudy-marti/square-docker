package fr.umlv.square.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.swing.text.html.Option;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ApplicationsList {
	private ArrayList<Application> list = new ArrayList<>();
	@ConfigProperty(name = "square.available.apps")
	private String appAvailable;
	private HashMap<String, Integer> deployCount = new HashMap<>();
	
	public void add(Application a) {
		this.list.add(a);
	}
	
	public ArrayList<Application> getList(){
		return new ArrayList<>(this.list);
	}
	
	public Application getOneAppRunning(String dockerInstance){
		Optional<Application> op = this.list.stream().
									filter(e -> dockerInstance.equals(e.getDockerInst())).
									findFirst();
		if (op.isEmpty())
			return null;
		return op.get();
	}
	
	public void increment_app(String app_name) {
		this.deployCount.compute(app_name, (key,value) -> value == null ? 1 : value+1);
	}

	public List<String> appAvailable() {
		return Arrays.asList(this.appAvailable.split(",")); //$NON-NLS-1$
	}

	public int getDeployID(String appName) {
		return this.deployCount.getOrDefault(appName, 0);
	}

	public Optional<Application> getAppById(int id) {
		return this.list.stream()
				.filter(app -> app.getId() == id)
				.findFirst();
	}
}
