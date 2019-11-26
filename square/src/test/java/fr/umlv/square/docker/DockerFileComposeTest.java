package fr.umlv.square.docker;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import fr.umlv.square.database.entities.Application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.StringJoiner;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

public class DockerFileComposeTest {
	
	
	

    @Test
    void dockerFileComposeWithNullAppShouldThrowNullPointerException () {
        assertThrows(NullPointerException.class, () -> new DockerFileCompose(null, "", "", "../"));
    }

    @Test
    void assertDockerFileComposeGetsTheRightPath() throws IOException {
        Application application = new Application(1, "hello", 8080, 8080, "docker");

        DockerFileCompose dockerFileCompose = new DockerFileCompose(application, "8080", "localhost", "../");
        String tmp = "../docker-images/" + application.getAppname() + ".jvm";

        assertEquals(tmp, dockerFileCompose.getDockerFilePath());
    }

    @Test
    void assertDockerFileComposeCreatesADockerFile () throws IOException {

        Application application = new Application(1, "hello",8080, 8080, "docker");

        DockerFileCompose dockerFileCompose = new DockerFileCompose(application,"8080", "localhost", "../");
        dockerFileCompose.composeDockerFile();

        var path2 = Paths.get(dockerFileCompose.getDockerFilePath());

        assertTrue(Files.exists(path2));
    }
}
