package fr.umlv.square.docker;

import fr.umlv.square.models.Application;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

/**
 * DockerFileCompose used to write an external DockerFile
 *
 * Using BufferedWritter ?
 * https://howtodoinjava.com/java/io/java-write-to-file/
 * https://www.baeldung.com/java-write-to-file
 *
 * To determine : should we use docker-compose ?
 * Using yaml : https://www.baeldung.com/java-snake-yaml question mark
 *
 * This class is linked to an Application :
 *  - One dockerfile will be used for all instances of docker-container with same app
 *
 */
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
                "FROM openjdk:11\n" +                                       // base image
                "EXPOSE %s\n" +                                             // docker port exposed to host
                "WORKDIR /workspace/\n" +                                   // workspace directory
                "COPY apps/%s.jar %s.jar\n" +                               // copy app into docker's workspace
                "RUN [\"chmod\",\"+x\",\"%s.jar\"]\n" +                     // give exec permissions to .jar file
                "CMD \"java\",\"-jar\",\"%s.jar > log.log 2>&1\"";          // command to be executed when docker starts running
    }

    public DockerFileCompose(Application application) throws IOException {
        Objects.requireNonNull(application);

        this.application = application;
        this.dockerFilePath = "docker-images/" + this.application.getapp() + ".jvm";
        this.dockerFileBufferedWriter = new FileWriter(this.dockerFilePath);
    }

    public String getDockerFilePath() {
        return this.dockerFilePath;
    }

    /**
     * Write DockerFile in a buffer
     */
    private void composeDockerFileBuffer() {
        dockerFileBuffer = String.format(dockerFileTemplate, application.getserviceport(), application.getapp(), application.getapp(), application.getapp(), application.getapp());
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

    /*
    public static void main(String[] args) throws IOException {
        Application demo = new Application(1, "hello", 8080, 8080, "docker");

        DockerFileCompose dockerFileCompose = new DockerFileCompose(demo);
        dockerFileCompose.composeDockerFileBuffer();

        System.out.println(dockerFileCompose.dockerFileBuffer);

        dockerFileCompose.composeDockerFile();
    }

     */
}
