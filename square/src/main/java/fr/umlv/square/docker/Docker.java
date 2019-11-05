package java.fr.umlv.square.docker;

import fr.umlv.square.models.Application;

import java.util.Objects;

public class Docker {

    /**
     * Object to be created when a docker process is deployed, to be saved
     */
    private static final String buildCmdTemplate;
    private static final String runCmdTemplate;

    static {
        buildCmdTemplate = "build -f docker-images/%s.jvm -t quarkus/%s-jvm";
        runCmdTemplate = "docker run -i --rm -p %s:%s quarkus/%s-jvm";
    }

    private final Application application;
    private final String dockerId;

    private final String osName;

    private final String[] buildCmd;
    private final String[] runCmd;

    private boolean running;

    public Docker(Application application) {
        Objects.requireNonNull(application);

        this.application = application;
        this.dockerId = application.getDockerInst();

        this.osName = System.getProperty("os.name");

        this.buildCmd = String.format(buildCmdTemplate, this.application.getapp(), this.application.getapp()).split(" ");
        this.runCmd = String.format(runCmdTemplate, this.application.getport(), this.application.getserviceport(), this.application.getapp()).split(" ");

        this.running = false;
    }

    public String getOsName() {
        return this.osName;
    }

    public String[] getBuildCmd() {
        return this.buildCmd;
    }

    public String[] getRunCmd() {
        return this.runCmd;
    }
}
