package fr.umlv.square.docker;

import fr.umlv.square.models.Application;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.StringJoiner;

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
        String tmp = "docker-images/" + application.getapp() + ".jvm";

        assertEquals(tmp, dockerFileCompose.getDockerFilePath());
    }

    @Test
    void assertDockerFileComposeCreatesADockerFile () throws IOException {
        Application application = new Application(1, "hello", 8080, 8080, "docker");

        DockerFileCompose dockerFileCompose = new DockerFileCompose(application);
        dockerFileCompose.composeDockerFile();

        Path path = Paths.get(dockerFileCompose.getDockerFilePath());

        assertTrue(Files.exists(path));
    }

    @Test
    void assertDockerFileComposeWritesTheRightDockerFile () throws IOException {
        Application application = new Application(1, "hello", 8080, 8080, "docker");

        DockerFileCompose dockerFileCompose = new DockerFileCompose(application);
        dockerFileCompose.composeDockerFile();

        String tmp =    "FROM openjdk-11\n" +
                        "EXPOSE 8080\n" +
                        "WORKDIR /workspace/\n" +
                        "COPY apps/hello.jar hello.jar\n" +
                        "RUN [\"chmod\",\"+x\",\"hello.jar\"]\n" +
                        "CMD \"java\",\"-jar\",\"hello.jar > log.log 2>&1\"";

        Scanner scanner = new Scanner(new File(dockerFileCompose.getDockerFilePath()));
        StringJoiner str = new StringJoiner("\n");
        while(scanner.hasNext()) {
            str.add(scanner.nextLine());
        }

        assertEquals(tmp, str.toString());
    }
}
