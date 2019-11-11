package fr.umlv.square.models;

import java.util.ArrayList;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApplicationsList {
	private ArrayList<Application> list = new ArrayList<Application>();
	
	public void add(Application a) {
		this.list.add(a);
	}
	
	public ArrayList<Application> getList(){
		return new ArrayList<Application>(this.list);
	}
	
	public Application getOneAppRunning(String dockerInstance){
		Optional<Application> op = this.list.stream().
									filter(e -> dockerInstance.equals(e.getDockerInst())).
									findFirst();
		if (op.isEmpty())
			return null;
		return op.get();
	}
}
