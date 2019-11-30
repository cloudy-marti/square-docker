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

	public static void rmDockerFile(Application app, String path){
		var str = new StringBuilder();
		var condition = System.getProperty("os.name").toLowerCase().startsWith("win"); 
		if(condition) {
			str.append("del");
		}
		else {
			str.append("rm ");
		}
		str.append(app.getAppname()).append(app.getPort());
		try {
			createAndStartProcessBuilder(path,str.toString().split(" "));
		} catch (IOException e) {
			return;
		}
	}
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
