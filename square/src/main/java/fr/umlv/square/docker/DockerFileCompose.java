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
 */
public class DockerFileCompose {

    private final Application application;

    private final String dockerFilePath;
    private final FileWriter dockerFileBufferedWriter;
    private String dockerFileBuffer;

    public DockerFileCompose(Application application) throws IOException {
        Objects.requireNonNull(application);

        this.application = application;
        this.dockerFilePath = "docker-images/" + this.application.getapp() + "Docker";
        this.dockerFileBufferedWriter = new FileWriter(this.dockerFilePath);
    }

    public String getDockerFilePath() {
        return this.dockerFilePath;
    }

    /**
     * Write DockerFile in a buffer
     */
    private void composeDockerFileBuffer() {
        StringBuilder str = new StringBuilder();
        str.append("FROM openjdk-11\n")
                .append("EXPOSE ").append(application.getserviceport()).append("\n")
                .append("WORKDIR /workspace/\n")
                .append("RUN [\"chmod\",\"+x\",\"/bin/").append(application.getapp()).append(".jar\"]\n")
                .append("CMD [\"java\",\"-jar\",\"/bin/").append(application.getapp()).append(".jar\"]");
        dockerFileBuffer = str.toString();
    }

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
