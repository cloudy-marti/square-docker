package fr.umlv.square.docker;

import fr.umlv.square.models.Application;

import java.util.Objects;
import java.util.StringJoiner;

public class Docker {

    /**
     * Object to be created when a docker process is deployed, to be saved
     */
    private static final String buildCmdTemplate;
    private static final String runCmdTemplate;
    private static final String stopCmdTemplate;

    static {
            buildCmdTemplate = "docker build -f ../docker-images/%s.jvm -t quarkus/%s-jvm .";
            runCmdTemplate = "docker run -i --rm --name %s -p %s:%s quarkus/%s-jvm";
            stopCmdTemplate = "docker container stop %s";
    }

    private final Application application;

    private final String[] buildCmd;
    private final String[] runCmd;
    private final String[] stopCmd;

    private boolean running;

    public Docker(Application application) {
        Objects.requireNonNull(application);

        this.application = application;

        this.buildCmd = String.format(buildCmdTemplate, this.application.getapp(), this.application.getapp()).split(" ");
        this.runCmd = String.format(runCmdTemplate, this.application.getDockerInst(), this.application.getserviceport(), this.application.getport(), this.application.getapp()).split(" ");
        this.stopCmd = String.format(stopCmdTemplate, this.application.getDockerInst()).split(" "); // TO DO

        this.running = false;
    }

    public String[] getBuildCmd() {
        return this.buildCmd;
    }

    public String[] getRunCmd() {
        return this.runCmd;
    }

    public String[] getStopCmd() {
        return this.stopCmd;
    }

    public boolean isDockerRunning() {
        return this.running;
    }

    public void run() {
        this.running = true;
    }

    public void stop() {
        this.running = false;
    }
}
