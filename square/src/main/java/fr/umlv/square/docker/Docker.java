package fr.umlv.square.docker;

import fr.umlv.square.models.Application;

import java.util.Objects;
import java.util.StringJoiner;

public class Docker {

    private static final String buildCmdTemplate;
    private static final String runCmdTemplate;

    static {
        StringBuilder tmp = new StringBuilder(System.getProperty("os.name").toLowerCase().startsWith("win") ?
                "powershell.exe -c " : "env -- ");
        buildCmdTemplate = tmp.append("docker build -f docker-images/%s.jvm -t quarkus/%s-jvm .").toString();
        runCmdTemplate = "docker run -d -it --rm --name %s -p %s:%s quarkus/%s-jvm";
    }

    private final Application application;

    private final String[] buildCmd;
    private final String[] runCmd;

    public Docker(Application application) {
        Objects.requireNonNull(application);

        this.application = application;

        this.buildCmd = String.format(buildCmdTemplate,
                this.application.getAppname(),
                this.application.getAppname()).split(" ");

        this.runCmd = String.format(runCmdTemplate,
                this.application.getDockerInst(),
                this.application.getServicePort(),
                this.application.getPort(),
                this.application.getAppname()).split(" ");
    }

    public String[] getBuildCmd() {
        return this.buildCmd;
    }

    public String[] getRunCmd() {
        return this.runCmd;
    }


    public String getBuildCmdToString() {
        StringJoiner strJoiner = new StringJoiner(" ");
        for(String str : buildCmd) {
            strJoiner.add(str);
        }
        return strJoiner.toString();
    }

    public String getRunCmdToString() {
        StringJoiner strJoiner = new StringJoiner(" ");
        for(String str : runCmd) {
            strJoiner.add(str);
        }
        return strJoiner.toString();
    }
}
