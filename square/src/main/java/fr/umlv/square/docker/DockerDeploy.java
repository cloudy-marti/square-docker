package fr.umlv.square.docker;

import fr.umlv.square.models.Application;

public class DockerDeploy {

    /**
     * Static Methods with ProcessBuilder
     */

    public static void buildDockerImage() {
        // TO DO
    }

    public static Docker runDockerImage() {
        // TO DO
        return new Docker(new Application(1, "a", 1, 1, "b"));
    }
}
