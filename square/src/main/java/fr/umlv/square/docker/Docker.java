package fr.umlv.square.docker;

import fr.umlv.square.models.Application;

import java.util.Objects;

public class Docker {

    /**
     * Object to be created when a docker process is deployed, to be saved
     */

    private final Application application;
    private final String osname;

    public Docker(Application application) {
        Objects.requireNonNull(application);

        this.application = application;
        this.osname = System.getProperty("os.name");
    }
}
