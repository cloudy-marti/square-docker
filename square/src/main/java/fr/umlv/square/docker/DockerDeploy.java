package fr.umlv.square.docker;

import org.apache.commons.io.IOUtils;

import fr.umlv.square.database.Application;

import javax.inject.Inject;
import javax.swing.text.html.Option;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.*;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class DockerDeploy {

    @Inject
    private HashMap<String, String> appImage = new HashMap<>();

    private static Process createAndStartProcessBuilder(String[] cmdLine) throws IOException {
        Objects.requireNonNull(cmdLine);

        ProcessBuilder processBuilder = new ProcessBuilder(cmdLine);
        //System.out.println(new File("../..").getCanonicalPath());
        processBuilder.directory(new File("../.."));

        return processBuilder.start();
    }

    private static Process buildDockerImage(Docker docker) throws IOException {
        Objects.requireNonNull(docker);
        return createAndStartProcessBuilder(docker.getBuildCmd());
    }

    private static Process runDockerImage(Docker docker) throws IOException {
        Objects.requireNonNull(docker);
        Process process = createAndStartProcessBuilder(docker.getRunCmd());

        return process;
    }

    public static boolean deployDocker(Application application, String port, String host) throws IOException {
        Objects.requireNonNull(application);
        Objects.requireNonNull(port);
        Objects.requireNonNull(host);

        DockerFileCompose dockerFile = new DockerFileCompose(application, port, host);
        dockerFile.composeDockerFile();

        Docker docker = new Docker(application);

        Process buildProcess = buildDockerImage(docker);
        Process runProcess;
        try {
            buildProcess.waitFor();
            runProcess = runDockerImage(docker);
            runProcess.waitFor();
        } catch (InterruptedException e) {
            throw new UndeclaredThrowableException(e);
        }
        
        String stdout = IOUtils.toString(runProcess.getInputStream(), "UTF-8");
        application.setIDContainer(stdout.split("\n")[0]);
        return buildProcess.exitValue() == 0 && runProcess.exitValue() == 0;
    }

    public static boolean stopDockerInstance(String dockerInstance) throws IOException {
        Objects.requireNonNull(dockerInstance);
        Process stopProcess = createAndStartProcessBuilder(String.format("docker container stop %s", dockerInstance).split(" "));

        try {
            stopProcess.waitFor();
        } catch (InterruptedException e) {
            throw new UndeclaredThrowableException(e);
        }

        return stopProcess.exitValue() == 0;
    }

    public static List<String> getRunningInstancesNames() throws IOException {

        ProcessBuilder dockerPs = new ProcessBuilder(("docker ps --format '{{.Names}}'").split(" "));
        String output = IOUtils.toString(dockerPs.start().getInputStream(), StandardCharsets.UTF_8);

        return Arrays.asList(output.replace("'", "").split("\n"));
    }

    public static int getUnboundedLocalPort() {
        try(ServerSocket socket = new ServerSocket()) {
            socket.bind(new InetSocketAddress(0));
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
