package fr.umlv.square.docker;

import fr.umlv.square.controllers.ApplicationsListRoute;
import fr.umlv.square.models.Application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import java.lang.ProcessBuilder.Redirect;
import java.util.stream.IntStream;

public class DockerDeploy {
    /**
     * Static Methods with ProcessBuilder
     */
    /**
     * private static non final not allowad, to change
     */
    private static ArrayList<Docker> dockerInstances = new ArrayList<>();
    private static File dockerLog = new File("logs/dockerLog");

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

    public static void stopDockerImage(Docker docker) throws IOException {
        docker.stop();
        removeDockerFromListing(docker);
    }

    private static void addToDockerListing(Docker docker) {
        dockerInstances.add(docker);
    }

    private static void removeDockerFromListing(Docker docker) {
        dockerInstances.remove(docker);
    }


    public static void main (String[] args) {

        /*ApplicationsListRoute apps = new ApplicationsListRoute();

        IntStream.range(0, apps.list().size()).forEach(index -> {

            Application tmp = apps.list().get(index);

            try {
                DockerFileCompose dockerFileCompose = new DockerFileCompose(tmp);
                dockerFileCompose.composeDockerFile();

                Docker docker = new Docker(tmp);

                buildDockerImage(docker);
                runDockerImage(docker);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

         */
    }


}
