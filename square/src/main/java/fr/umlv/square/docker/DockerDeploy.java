package fr.umlv.square.docker;

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
    private static File dockerLog = new File("../logs/dockerLog");

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

        /*Application application = new Application(1, "appli_demo-runner:8080", 15202, 8080, "docker-2");

        DockerFileCompose dockerFile = new DockerFileCompose(application);
        dockerFile.composeDockerFile();

        Docker docker = new Docker(application);

        buildDockerImage(docker);
        runDockerImage(docker);

         */

        ArrayList<Application> apps = new ArrayList<>();

       /* apps.add(new Application(201,"appli_demo-runner",8082, 15201, "demo-12"));
        apps.add(new Application(202,"appli_demo-runner:8082", 8082, 15202, "demo-13"));

        */
        apps.add(new Application(203,"appli_demo-runner ", 8085,9000,"demo-16"));

        IntStream.range(0, apps.size()).forEach(index -> {

            Application tmp = apps.get(index);

            try {
                DockerFileCompose dockerFileCompose = new DockerFileCompose(tmp);
                dockerFileCompose.composeDockerFile();

                Docker docker2 = new Docker(tmp);

                buildDockerImage(docker2);
                runDockerImage(docker2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


}
