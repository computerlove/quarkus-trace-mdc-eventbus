package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

@Path("/hello")
public class GreetingResource {

    private final Logger log;

    public GreetingResource(Logger log) {
        this.log = log;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@QueryParam("hello") String hello) {
        log.infof("%s!", hello);
        return hello + " from RESTEasy Reactive";
    }
}
