package fr.umlv.square.docker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import fr.umlv.square.database.entities.Application;

public class DockerFileCompose {

    private static final String dockerFileTemplate;

    private final Application application;
    private final String dockerFilePath;
    private final String dockerFileName;
    private String dockerFileBuffer;
    private final String port;
    private final String host;

    static {
        dockerFileTemplate =
                "FROM openjdk:11.0.5-jre-slim\r\n" +                                 // Base image giving openjdk 11 environment
                "EXPOSE %s\n" +                                                     // Docker port exposed to host
                "WORKDIR /workspace/\n" +                                           // Workspace directory

                "ENV SQUARE_PORT=%s\n" +
                "ENV SQUARE_HOST=%s\n" +

                "COPY lib-client/lib_cliente-runner.jar /workspace/lib.jar\n" +     // Copy files into docker's workspace
                "COPY apps/%s.jar /workspace/%s.jar\n" +
                "COPY docker-images/script.sh /workspace/script.sh\n" +

                "RUN [\"chmod\",\"+x\",\"lib.jar\"]\n" +                            // Give permission to execute
                "RUN [\"chmod\",\"+x\",\"%s.jar\"]\n" +
                "RUN [\"chmod\",\"+x\",\"script.sh\"]\n" +

                "RUN [\"sed\", \"-i\", \"s/\\r$//g\", \"script.sh\"]\n" +           // # Remove annoying '\r' Windows characters
                "CMD bash script.sh %s %s \n" +                         // Run script with bash - Name of demo-app given as a parameter

                "HEALTHCHECK --interval=5s --timeout=3s --retries=3 \\" +           // Check app's health
                "\nCMD curl -f http://localhost:%s || exit 1";
    }

    public DockerFileCompose(Application application, String port, String host, String path) {
        Objects.requireNonNull(application);
        this.application = application;
        this.port = port;
        this.host = host;
        this.dockerFilePath = path + "docker-images/";
        this.dockerFileName = (this.application.getAppname()+this.application.getPort()) + ".jvm";
    }

    public String getDockerFilePath() {
        return this.dockerFilePath;
    }

    private void composeDockerFileBuffer() {
        this.dockerFileBuffer = String.format(dockerFileTemplate,
        		this.application.getPort(),
                this.port,
                this.host,
                this.application.getAppname(),
                this.application.getAppname(),
                this.application.getAppname(),
                this.application.getPort(),
                this.application.getAppname(),
                this.application.getPort()
                );
    }

    /**
     * This method create a file corresponding to the DockerFile created
     * @throws IOException
     */
    public void composeDockerFile() throws IOException {
        composeDockerFileBuffer();
        Path fileToCreatePath = Paths.get(this.dockerFilePath + this.dockerFileName);
        Path newFilePath = Files.createFile(fileToCreatePath);
        Files.write(newFilePath, this.dockerFileBuffer.getBytes());
    }
}
