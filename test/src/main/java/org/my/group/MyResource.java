package org.my.group;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/test")
public class MyResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Object hello() {
        return new Object();
    }
}