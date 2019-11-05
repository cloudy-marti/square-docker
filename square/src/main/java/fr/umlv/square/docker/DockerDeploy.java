package java.fr.umlv.square.docker;

import fr.umlv.square.models.Application;

import java.io.IOException;
import java.util.ArrayList;

public class DockerDeploy {

    /**
     * Static Methods with ProcessBuilder
     */
    private ArrayList<Docker> dockerInstance;

    public static void buildDockerImage(Docker docker) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(docker.getBuildCmd());
       /* processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(new File("logs/log"));

        */
        processBuilder.start();
    }

    public static Docker runDockerImage(Docker docker) {
        String[] cmdLine = docker.getRunCmd();
        return new Docker(new Application(1, "a", 1, 1, "b"));
    }

    public static void main (String[] args) throws IOException {

        Application application = new Application(1, "hello", 8080, 8080, "docker");
        /*
        DockerFileCompose dockerFileCompose = new DockerFileCompose(application);
        dockerFileCompose.composeDockerFile();

         */

        Docker docker = new Docker(application);
        /*int index = 0;
        while(index < docker.getBuildCmd().length) {
            System.out.print(docker.getBuildCmd()[index] + " ");
            index++;
        }

         */

        buildDockerImage(docker);

    }

}
