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

    /**
     * Make template once with the first instantiation / static method call
     */
    static {
        dockerFileTemplate =
                "FROM openjdk:11\n" +                                           // base image
                "EXPOSE %s\n" +                                                 // docker port exposed to host
                "WORKDIR /workspace/\n" +                                       // workspace directory
                "COPY apps/%s /workspace/%s\n" +                                // copy app into docker's workspace
                "RUN [\"chmod\",\"+x\",\"%s\"]\n" +                             // give exec permissions to .jar file
                "CMD [\"java\",\"-jar\",\"%s\",\">\",\"log.log\",\"2>&1\"]";    // command to be executed when docker starts running
    }

    public DockerFileCompose(Application application) throws IOException {
        Objects.requireNonNull(application);

        this.application = application;
        this.dockerFilePath = "docker-images/" + this.application.getAppName() + ".jvm";
        this.dockerFileBufferedWriter = new FileWriter(this.dockerFilePath);
    }

    public String getDockerFilePath() {
        return this.dockerFilePath;
    }

    private void composeDockerFileBuffer() {
        dockerFileBuffer = String.format(dockerFileTemplate,
                application.getport(),
                application.getAppName(),
                application.getAppName(),
                application.getAppName(),
                application.getAppName());
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
