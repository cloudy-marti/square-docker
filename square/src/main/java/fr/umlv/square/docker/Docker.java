package fr.umlv.square.docker;

import java.util.Objects;
import fr.umlv.square.database.entities.Application;

public class Docker {

    private static final String buildCmdTemplate;
    private static final String runCmdTemplateFromName;	
    private static final String runCmdTemplateFromID;	
    private static final String saveCmdTemplate;
    private static final String loadCmdTemplate;

    /**
     * Create all String we need to process on the deployment of a container
     */
    static {
        StringBuilder tmp = new StringBuilder(System.getProperty("os.name").toLowerCase().startsWith("win") ?
                "powershell.exe -c " : "env -- ");
        buildCmdTemplate = tmp.append("docker build -q -f docker-images/%s.jvm -t quarkus/%s .").toString();
        runCmdTemplateFromName = "docker run -d -it --rm --name %s -p %s:%s quarkus/%s";
        runCmdTemplateFromID = "docker run -d -it --rm --name %s -p %s:%s %s";
        saveCmdTemplate = "docker save %s -o docker-images/%s.tar.gz";
        loadCmdTemplate = "docker load -q -i docker-images/%s.tar.gz";
    }


    private final String[] buildCmd;
    private final String[] runCmd;
    private String[] saveCmd;
    private final String[] loadCmd;
    private final String nameDock;

    public Docker(Application application) {
        Objects.requireNonNull(application);
        this.nameDock = application.getAppname()+application.getPort();
        this.buildCmd = String.format(buildCmdTemplate,
                this.nameDock,
                this.nameDock).split(" ");

        this.runCmd = String.format(runCmdTemplateFromName,
                application.getDockerInst(),
                application.getServicePort(),
                application.getPort(),
                this.nameDock).split(" ");
        
        this.loadCmd = String.format(loadCmdTemplate,this.nameDock).split(" ");
    }
    
    public String[] getLoadCmd() {
    	return this.loadCmd;
    }

    public String[] getBuildCmd() {
        return this.buildCmd;
    }

    public String[] getSaveCmd() {
    	return this.saveCmd;  	
    }
    
    public String[] getRunCmd() {
        return this.runCmd;
    }
    
    public void setSave(String ID) {
        this.saveCmd = String.format(saveCmdTemplate, ID, this.nameDock).split(" ");
    }

    /**
     * This method will create the command we need to run to start a container with the image's ID
     * @param app application to set port and name.
     * @param id the image's ID
     * @return String[]
     */
	public String[] getAndSetRunCmdFromID(Application app, String id) {
		return String.format(runCmdTemplateFromID,
				app.getDockerInst(),
				app.getServicePort(),
				app.getPort(),
				id).split(" ");
	}
}
