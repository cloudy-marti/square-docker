package fr.umlv.square.docker;

import org.apache.commons.io.IOUtils;


import fr.umlv.square.database.entities.Application;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class DockerDeploy {

	private static Process createAndStartProcessBuilder(String path, String...cmdLine) throws IOException {
		Objects.requireNonNull(cmdLine);
		ProcessBuilder processBuilder = new ProcessBuilder(cmdLine);
		processBuilder.directory(new File(path));
		return processBuilder.start();
	}
	
	/**
	 * 
	 * @param path the path we should be to start the ProcessBuider
	 * @param cmdLine the command to execute
	 * @return Process corresponding to the ProcessBuilder started
	 * @throws IOException
	 */
	public static Process wrapperCreateStartPB(String path, String...cmdLine) throws IOException {
		var p = createAndStartProcessBuilder(path, cmdLine);
		return p;
	}
	
	/**
	 * This method stop a container
	 * @param dockerInstance the name of the container
	 * @param path path from where we will start the processBuilder
	 * @return
	 * @throws IOException
	 */
	public static boolean stopDockerInstance(String dockerInstance, String path) throws IOException {
		Objects.requireNonNull(dockerInstance);
		Process stopProcess = createAndStartProcessBuilder(
				path, String.format("docker container stop -t1 %s", dockerInstance).split(" "));

		try {
			stopProcess.waitFor();
		} catch (InterruptedException e) {
			throw new AssertionError(e);
		}

		return stopProcess.exitValue() == 0;
	}

	/**
	 * This method returns a list containing all docker-instance name running
	 * @return
	 */
	public static List<String> getRunningInstancesNames() {
		ProcessBuilder dockerPs = new ProcessBuilder(("docker ps --format '{{.Names}}'").split(" "));
		String output;
		try {
			output = IOUtils.toString(dockerPs.start().getInputStream(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new AssertionError(e);
		}

		return Arrays.asList(output.replace("'", "").split("\n"));
	}

	/**
	 * This method delete a dockerFile. Try it first with linux command, then with windows.
	 * @param app the application so we can retrieve the name of the dockerFile.
	 * @param path, the path from where we are to go to docker-images directory
	 */
	public static void rmDockerFile(Application app, String path){
		String str = "rm docker-images/" + app.getAppname() + app.getPort() + ".jvm";
		String str2 = "del docker-images/" + app.getAppname() + app.getPort() + ".jvm";
		try {
			var proc = createAndStartProcessBuilder(path,str.split(" "));
			try {
				proc.waitFor();
			} catch (InterruptedException e) {
				throw new AssertionError();
			}
			if(proc.exitValue() != 0) {
				createAndStartProcessBuilder(path,str2.split(" "));
			}
		} catch (IOException e) {
			return;
		}
	}
		
	/**
	 * This method get a free port for the application we want to deploy
	 * @return the port	
	 */
	public static int getUnboundedLocalPort() {
		try (ServerSocket socket = new ServerSocket()) {
			socket.bind(new InetSocketAddress(0));
			var id = socket.getLocalPort();
			socket.close();
			return id;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
