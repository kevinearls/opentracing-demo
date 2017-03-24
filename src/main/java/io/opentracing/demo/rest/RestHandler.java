package io.opentracing.demo.rest;

import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pavol Loffay
 */
@Path("/")
public class RestHandler {

    private Random random = new Random();
    private final Tracer tracer;
    private final Logger logger = LoggerFactory.getLogger(RestHandler.class);

    public RestHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    @GET
    @Path("/user/{userName}")
    public Response getEmail(@PathParam("userName") String userName, @Context UriInfo uriInfo) {
        logger.info("getMail invoked with user [" + userName + "]");
        Span span = tracer.buildSpan("get_user_email")
                .withTag("tag", "hello")
                .start();

        String user = getEmailFromDB(span.context(), userName);

        span.finish();

        return Response.ok().entity(user).build();
    }

    private String getEmailFromDB(SpanContext parent, String userName) {

        if (parent != null) {
            Span dbProcessingSpan = tracer.buildSpan("get_user_from_db")
                    .asChildOf(parent)
                    .start();

            /**
             * Business logic
             *
             * ....
             */

            long waitTime = waitRandom();

            //Adding wait time as a tag
            dbProcessingSpan.setTag("waitTime", waitTime);
            dbProcessingSpan.setTag("user", userName);

            dbProcessingSpan.finish();
        }

        return userName.replaceAll(" ", "_") + "@redhat.com";
    }

    private long waitRandom() {
        long waitTime = (long) (random.nextDouble() * 1000);
        try {

            logger.info("Sleeping for " + waitTime + " ms");
            Thread.sleep((int) waitTime);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return waitTime;
    }
}
