package fr.umlv.square.controllers;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import fr.umlv.square.docker.Docker;
import fr.umlv.square.docker.DockerFileCompose;
import fr.umlv.square.models.Application;
import fr.umlv.square.models.Stop;

import static fr.umlv.square.docker.DockerDeploy.*;


@Path("/app")
public class ApplicationsListRoute {
	private static ArrayList<Application> list = new ArrayList<Application>();
	
	public ApplicationsListRoute() {
		list.add(new Application(201,"todomvc",8082,15201,"todomvc-12"));
		list.add(new Application(202,"todomvc",8082,15202,"todomvc-13"));
		list.add(new Application(203,"todomvc",8082,15203,"todomvc-14"));

	}
	
	@Path("/list")
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<Application>list() {
        return list;
    }
	
	@Path("/deploy")
	@POST	
    @Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
    public Response deploy(JsonObject obj) {
		Objects.requireNonNull(obj);

		String str;
		String array[];

		try {
			str = obj.get("app").toString();
			str = str.replace('"',' ').trim();
			array = str.split(":");

			Application app = new Application(204,array[0],Integer.parseInt(array[1]), getUnboundedLocalPort(),"docker-204");

			System.out.println("hello");
			System.out.println(app.toMap());
			list.add(app);

			deployDocker(app);

		} catch(NullPointerException  e) {
			return Response.status(Status.NOT_ACCEPTABLE).entity("Error with the JSON").build();
		} catch (IndexOutOfBoundsException e) {
			return Response.status(Status.NOT_ACCEPTABLE).entity("index out of bound").build();
		} catch (NumberFormatException e) {
			return Response.status(Status.NOT_ACCEPTABLE).entity("number format exception").build();
		} catch (IllegalStateException e) {
			return Response.status(Status.NOT_ACCEPTABLE).entity("Unbounded port not found").build();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Status.NOT_ACCEPTABLE).entity("IO Error").build();
		}

		return Response.status(Status.CREATED).build();
    }
	
	@Path("/stop")
	@POST	
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
    public Response stop(JsonObject obj) {
		 System.out.println("\nFermeture du Docker de l'app avec l'id " + obj.get("id"));
		 Stop val = new Stop(list.get(1),"4m37s");
		 
         return Response.status(Status.OK).entity(val.toMap()).build();
    }

	public static Application getOneAppRunning(String dockerInstance) {
		Optional<Application> op = list.stream().
				filter(e -> dockerInstance.equals(e.getDockerInst())).
				findFirst();
		if(op.isEmpty())
			return null;
		return op.get();
	}
}