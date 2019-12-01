package fr.umlv.square.docker;

import org.junit.jupiter.api.Test;

import fr.umlv.square.database.entities.Application;
import java.io.IOException;
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
        String tmp = "../docker-images/";

        assertEquals(tmp, dockerFileCompose.getDockerFilePath());
    }
}
