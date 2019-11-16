package fr.umlv.square.docker;

import fr.umlv.square.models.Application;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class DockerDeploy {
    /**
     * Static Methods with ProcessBuilder
     */

    private final static ArrayList<Docker> dockerInstances = new ArrayList<>();

    private static Process createAndStartProcessBuilder(String[] cmdLine) throws IOException {
        Objects.requireNonNull(cmdLine);

        ProcessBuilder processBuilder = new ProcessBuilder(cmdLine);
        processBuilder.directory(new File("../.."));
        processBuilder.inheritIO();

        return processBuilder.start();
    }

    private static Process buildDockerImage(Docker docker) throws IOException {
        return createAndStartProcessBuilder(docker.getBuildCmd());
    }

    private static Process runDockerImage(Docker docker) throws IOException {
        Process process = createAndStartProcessBuilder(docker.getRunCmd());

        docker.run();
        addToDockerListing(docker);

        return process;
    }

    public static void deployDocker(Application application, String port, String host) throws IOException {
        DockerFileCompose dockerFile = new DockerFileCompose(application, port, host);
        dockerFile.composeDockerFile();

        Docker docker = new Docker(application);

        Process buildProcess = buildDockerImage(docker);
        try {
            buildProcess.waitFor();
            runDockerImage(docker).waitFor();
        } catch (InterruptedException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    public static void stopDockerInstance(Docker docker) throws IOException {
        createAndStartProcessBuilder(docker.getStopCmd());

        docker.stop();
        removeDockerFromListing(docker);
    }

    private static void addToDockerListing(Docker docker) {
        dockerInstances.add(docker);
    }

    private static void removeDockerFromListing(Docker docker) {
        dockerInstances.remove(docker);
    }

    public static int getUnboundedLocalPort() {
        try(ServerSocket socket = new ServerSocket()) {
            socket.bind(new InetSocketAddress(0));
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
