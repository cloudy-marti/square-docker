package fr.umlv.square.docker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;


import org.apache.commons.io.IOUtils;

import fr.umlv.square.database.entities.Application;

public class SynchronizedDeploy {
	private final Set<String> deploySet = new HashSet<>();
	
	/**
	 * This methods try to add in the hashmap 
	 * @param name the name of the application concatenated with the port ex : demo8080 
	 * @return true if added, else false.
	 */
	private boolean addInMapIfPossible(String name) {
			return this.deploySet.add(name);
	}
	
	public boolean deployApp(Application app, String port, String host, String path) throws IOException {
		String name = app.getAppname()+app.getPort();
		synchronized(this.deploySet){
			while(!this.addInMapIfPossible(name)) {
				try {
					this.deploySet.wait();
				} catch (InterruptedException e) {
					return false;
				}
			}
			var res = tryDeploying(name, app, port, host, path);
			this.deploySet.notifyAll();
			this.deploySet.remove(name);
			return res;
		}
	}	
	
	private static boolean tryDeploying(String name, Application app, String port, String host, String path) throws IOException {
		var docker = new Docker(app);
		if(!runImage(docker, path, app)) {
			if(!loadAndCallImage(docker, path, app)) {
				return firstTimeBuild(docker, path, port, host, app);
			}
		}
		return true;
	}

	private static boolean firstTimeBuild(Docker docker, String path, String port, String host, Application app) throws IOException {
		DockerFileCompose dockerFile = new DockerFileCompose(app, port, host, path);
		dockerFile.composeDockerFile();
		if(!buildDockerImage(docker, path, app))
			return false;
		if(!runImage(docker, path, app))
			return false;
		return true;
	}

	private static boolean buildDockerImage(Docker docker, String path, Application app) throws IOException {
		Process p = DockerDeploy.wrapperCreateStartPB(path, docker.getBuildCmd());
		if(waitFor(p)) {
			if(!saveImage(p, docker, path)) 
				return false;
			
			String stdout = IOUtils.toString(p.getInputStream(), "UTF-8");
			app.setIDContainer(stdout.split("\n")[0]);
			return true;
		}
		return false;
	}

	private static boolean loadAndCallImage(Docker docker, String path, Application app) throws IOException {
		if(loadImage(docker, path)) {
			if(runImage(docker, path, app))
				return true;
		}
		return false;
	}

	private static boolean loadImage(Docker docker, String path) throws IOException {
		Process p = DockerDeploy.wrapperCreateStartPB(path, docker.getLoadCmd());
		return waitFor(p);
	}

	private static boolean runImage(Docker docker, String path, Application app) throws IOException {
		Process p = DockerDeploy.wrapperCreateStartPB(path, docker.getRunCmd());
        String stdout = IOUtils.toString(p.getInputStream(), "UTF-8");
        app.setIDContainer(stdout.split("\n")[0]);
		return waitFor(p);
	}
	
	private static String readOutPut(Process proc) {
        String output;
		try {
			output = IOUtils.toString(proc.getInputStream(), StandardCharsets.UTF_8);
		} catch (IOException e) {
            throw new AssertionError(e);
		}
		var id = output.substring(output.indexOf(":")+1, output.lastIndexOf("\n"));
		return id.substring(0,12);
	}
	
	private static boolean saveImage(Process Buildproc, Docker docker, String path) throws IOException {		
		var ID = readOutPut(Buildproc);
		if(ID.isEmpty())
			return false;
		docker.setSave(ID);
		return waitFor(DockerDeploy.wrapperCreateStartPB(path, docker.getSaveCmd()));
	}
	
	private static boolean waitFor(Process p) {
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			return false;
		}
		return p.exitValue() == 0;
	}
}
