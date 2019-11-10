package fr.umlv.square.docker;

import fr.umlv.square.models.Application;
import org.hibernate.StaleStateException;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import java.lang.ProcessBuilder.Redirect;
import java.util.stream.IntStream;

public class DockerDeploy {
    /**
     * Static Methods with ProcessBuilder
     */

    private final static ArrayList<Docker> dockerInstances = new ArrayList<>();
    private final static File dockerLog = new File("../logs/dockerLog");

    private static void createAndStartProcessBuilder(String[] cmdLine) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(cmdLine);
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(Redirect.appendTo(dockerLog));
        processBuilder.start();
    }

    private static void buildDockerImage(Docker docker) throws IOException {
        createAndStartProcessBuilder(docker.getBuildCmd());
    }

    private static void runDockerImage(Docker docker) throws IOException {
        createAndStartProcessBuilder(docker.getRunCmd());

        docker.run();
        addToDockerListing(docker);
    }

    public static void deployDocker(Application application) throws IOException {
        DockerFileCompose dockerFile = new DockerFileCompose(application);
        dockerFile.composeDockerFile();

        Docker docker = new Docker(application);

        buildDockerImage(docker);
        runDockerImage(docker);
    }

    public static void stopDockerImage(Docker docker) throws IOException {
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

    public static void main (String[] args) {

        ArrayList<Application> apps = new ArrayList<>();

        IntStream.range(0, 5).forEach(index -> {

            apps.add(new Application(index, "appli_demo-runner", 8082, getUnboundedLocalPort(), "docker-" + index));

            System.out.println(apps.get(index).toMap().toString());

            try {
                deployDocker(apps.get(index));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
