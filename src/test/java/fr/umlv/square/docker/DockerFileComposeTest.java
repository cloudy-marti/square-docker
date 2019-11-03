package fr.umlv.square.docker;

import fr.umlv.square.models.Application;
import org.apache.groovy.json.internal.IO;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class DockerFileComposeTest {

    @Test
    void dockerFileComposeWithNullAppShouldThrowNullPointerException () {
        assertThrows(NullPointerException.class, () -> new DockerFileCompose(null));
    }

    @Test
    void assertDockerFileComposeGetsTheRightPath() throws IOException {
        Application application = new Application(1, "hello", 8080, 8080, "docker");

        DockerFileCompose dockerFileCompose = new DockerFileCompose(application);
        String tmp = "docker-images/" + application.getapp() + "Docker";

        assertEquals(tmp, dockerFileCompose.getDockerFilePath());
    }

    @Test
    void assertDockerFileComposeCreatesADockerFile () throws IOException {
        Application application = new Application(1, "hello", 8080, 8080, "docker");

        DockerFileCompose dockerFileCompose = new DockerFileCompose(application);
        dockerFileCompose.composeDockerFile();

        Path path = Paths.get(dockerFileCompose.getDockerFilePath());

        assertTrue(Files.exists(path));
        //File tmpFile = new File(dockerFileCompose.getDockerFilePath());
    }

    @Test
    void assertDockerFileComposeWritesADockerFile () throws IOException {
        Application application = new Application(1, "hello", 8080, 8080, "docker");

        DockerFileCompose dockerFileCompose = new DockerFileCompose(application);
        dockerFileCompose.composeDockerFile();

        String tmp = "FROM openjdk-11\n" +
                "EXPOSE 8080\n" +
                "WORKDIR /workspace/\n" +
                "RUN [\"chmod\",\"+x\",\"/bin/hello.jar\"]\n" +
                "CMD [\"java\",\"-jar\",\"/bin/hello.jar\"]";

        File tmpFile = new File(dockerFileCompose.getDockerFilePath());
        assertTrue(tmpFile.exists());
    }
}
