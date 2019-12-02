package fr.umlv.square.docker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
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
	
	/**
	 * This method put in sleep a thread if another is deploying the same app. 
	 * If the thread doesn't sleep, it deploy app
	 * @param app 
	 * @param port
	 * @param host
	 * @param path 
	 * @return boolean
	 * @throws IOException
	 */
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
		}
		
		var res = tryDeploying(name, app, port, host, path);

		synchronized (this.deploySet) {
			this.deploySet.notifyAll();
			this.deploySet.remove(name);
			return res;
		
		}
	}	
	
	/**
	 * This method will try to deploy an container. It try to run the image who corresponds to the one it should be if the image exists
	 * If it doesn't work, 
	 * @param name
	 * @param app
	 * @param port
	 * @param host
	 * @param path path to saved docker image
	 * @return boolean whether we could deploy or not
	 * @throws IOException
	 */
	private static boolean tryDeploying(String name, Application app, String port, String host, String path) throws IOException {
		var docker = new Docker(app);
		if(!runImage(docker, path, app, Optional.empty())) {
			if(!loadAndCallImage(docker, path, app)) {
				var res = firstTimeBuild(docker, path, port, host, app);
				DockerDeploy.rmDockerFile(app, path);
				return res;
			}
		}
		return true;
	}

	/**
	 * This method is called if we need to write the dockerFile.
	 * @param docker
	 * @param path
	 * @param port
	 * @param host
	 * @param app
	 * @return boolean
	 * @throws IOException
	 */
	private static boolean firstTimeBuild(Docker docker, String path, String port, String host, Application app) throws IOException {
		DockerFileCompose dockerFile = new DockerFileCompose(app, port, host, path);
		dockerFile.composeDockerFile();
		if(!buildDockerImage(docker, path, app))
			return false;
		if(!runImage(docker, path, app, Optional.empty()))
			return false;
		return true;
	}

	/**
	 * This method is called when we build the docker image from the dockerFile
	 * Then it save the image
	 * @param docker
	 * @param path
	 * @param app
	 * @return boolean
	 * @throws IOException
	 */
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

	/**
	 * This method is called if the image doesn't exist in the repository of Docker.
	 * We load our existing image and run it.
	 * @param docker Docker object to be deployed
	 * @param path Path of the image
	 * @param app Application to be deployed
	 * @return boolean
	 * @throws IOException
	 */
	private static boolean loadAndCallImage(Docker docker, String path, Application app) throws IOException {
		Process p = loadImage(docker, path);
		if(waitFor(p)){
			String idImage = readLoadOutPut(p);
			if(runImage(docker, path, app, Optional.of(idImage)))
				return true;
		}
		return false;
	}

	private static Process loadImage(Docker docker, String path) throws IOException {
		return DockerDeploy.wrapperCreateStartPB(path, docker.getLoadCmd());
	}

	private static boolean runImage(Docker docker, String path, Application app, Optional<String> idImages) throws IOException {
		Process p;
		if(idImages.isEmpty()) {
			p = DockerDeploy.wrapperCreateStartPB(path, docker.getRunCmd());
		}
		else {
			p = DockerDeploy.wrapperCreateStartPB(path, docker.getAndSetRunCmdFromID(app, idImages.get()));
		}			
        String stdout = IOUtils.toString(p.getInputStream(), StandardCharsets.UTF_8);
        app.setIDContainer(stdout.split("\n")[0]);
		return waitFor(p);
	}
	
	private static String readLoadOutPut(Process proc) {
        String output;
		try {
			output = IOUtils.toString(proc.getInputStream(), StandardCharsets.UTF_8);
		} catch (IOException e) {
            throw new AssertionError(e);
		}
		return output.substring(output.lastIndexOf(":")+1, output.lastIndexOf("\n"));
	}
	
	private static String readOutPut(Process proc) {
        String output;
		try {
			output = IOUtils.toString(proc.getInputStream(), StandardCharsets.UTF_8);
		} catch (IOException e) {
            throw new AssertionError(e);
		}
		var id = output.substring(output.lastIndexOf(":")+1, output.lastIndexOf("\n"));
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
