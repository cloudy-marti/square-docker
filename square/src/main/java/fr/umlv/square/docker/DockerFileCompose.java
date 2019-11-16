package fr.umlv.square.docker;

import fr.umlv.square.models.Application;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class DockerFileCompose {

    private static final String dockerFileTemplate;

    private final Application application;

    private final String dockerFilePath;
    private final FileWriter dockerFileBufferedWriter;
    private String dockerFileBuffer;
    private String port;
    private String host;
	
    /**
     * Make template once with the first instantiation / static method call
     */
    static {
        dockerFileTemplate =
                "FROM openjdk:11\n" +                                               // Base image giving openjdk 11 environment
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
                "CMD [\"bash\", \"script.sh\", \"%s\"]\n" +                         // Run script with bash - Name of demo-app given as a parameter

                "HEALTHCHECK --interval=5s --timeout=3s --retries=3 \\" +           // Check app's health
                "\nCMD curl -f http://localhost:8080 || exit 1";
    }

    public DockerFileCompose(Application application, String port, String host) throws IOException {
        Objects.requireNonNull(application);

        this.application = application;
        this.port = port;
        this.host = host;
        this.dockerFilePath = "../../docker-images/" + this.application.getappname() + ".jvm";

        this.dockerFileBufferedWriter = new FileWriter(this.dockerFilePath);
    }

    public String getDockerFilePath() {
        return this.dockerFilePath;
    }

    private void composeDockerFileBuffer() {
        this.dockerFileBuffer = String.format(dockerFileTemplate,
                application.getport(),
                this.port,
                this.host,
                application.getappname(),
                application.getappname(),
                application.getappname(),
                application.getappname(),
                application.getappname());
    }

    /**
     * Write dockerfile buffer in a file
     * @throws IOException
     */
    public void composeDockerFile() throws IOException {
        composeDockerFileBuffer();
        dockerFileBufferedWriter.write(dockerFileBuffer);
        dockerFileBufferedWriter.flush();
        dockerFileBufferedWriter.close();
    }
}
