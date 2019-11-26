package fr.umlv.square.docker;

import org.apache.commons.io.IOUtils;

import fr.umlv.square.database.entities.Application;

import javax.inject.Inject;
import javax.swing.text.html.Option;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.*;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class DockerDeploy {

    private static Process createAndStartProcessBuilder(String[] cmdLine, String path) throws IOException {
        Objects.requireNonNull(cmdLine);
        ProcessBuilder processBuilder = new ProcessBuilder(cmdLine);
        processBuilder.directory(new File(path));
        return processBuilder.start();
    }

    private static Process buildDockerImage(Docker docker, String path) throws IOException {
        Objects.requireNonNull(docker);
        return createAndStartProcessBuilder(docker.getBuildCmd(), path);
    }

    private static Process runDockerImage(Docker docker, String path) throws IOException {
        Objects.requireNonNull(docker);
        Process process = createAndStartProcessBuilder(docker.getRunCmd(), path);

        return process;
    }

    public static boolean deployDocker(Application application, String port, String host, String path) throws IOException {
        Objects.requireNonNull(application);
        Objects.requireNonNull(port);
        Objects.requireNonNull(host);

        DockerFileCompose dockerFile = new DockerFileCompose(application, port, host, path);
        dockerFile.composeDockerFile();

        Docker docker = new Docker(application);

        Process buildProcess = buildDockerImage(docker, path);
        Process runProcess;
        try {
            buildProcess.waitFor();
            runProcess = runDockerImage(docker, path);
            runProcess.waitFor();
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
        
        String stdout = IOUtils.toString(runProcess.getInputStream(), "UTF-8");
        application.setIDContainer(stdout.split("\n")[0]);
        return buildProcess.exitValue() == 0 && runProcess.exitValue() == 0;
    }

    public static boolean stopDockerInstance(String dockerInstance, String path) throws IOException {
        Objects.requireNonNull(dockerInstance);
        Process stopProcess = createAndStartProcessBuilder(String.format("docker container stop -t1 %s", dockerInstance).split(" "), path);

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

    public static int getUnboundedLocalPort() {
        try(ServerSocket socket = new ServerSocket()) {
            socket.bind(new InetSocketAddress(0));
            var id = socket.getLocalPort();
            socket.close();
            return id;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
