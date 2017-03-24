package io.opentracing.demo.rest;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.hawkular.apm.client.api.recorder.BatchTraceRecorder;
import org.hawkular.apm.client.opentracing.APMTracer;
import org.hawkular.apm.client.opentracing.DeploymentMetaData;
import org.hawkular.apm.trace.publisher.rest.client.TracePublisherRESTClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.opentracing.Tracer;

import static org.hawkular.apm.client.api.sampler.Sampler.ALWAYS_SAMPLE;

/**
 * @author Pavol Loffay
 */
@Component
@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {
    private final Logger logger = LoggerFactory.getLogger(JerseyConfig.class);

    public static String HAWKULAR_APM_URI =  "http://localhost:8080";
    public static String HAKWULAR_APM_PASSWORD = "password";
    public static String HAWKULAR_APM_USERNAME = "jdoe";
    public static String DEMO_SERVICE_NAME = "opentracing-demo";

    public JerseyConfig() {
        HAWKULAR_APM_URI = getEnv("HAWKULAR_APM_URI", HAWKULAR_APM_URI);
        HAKWULAR_APM_PASSWORD = getEnv("HAKWULAR_APM_PASSWORD", HAKWULAR_APM_PASSWORD);
        HAWKULAR_APM_USERNAME = getEnv("HAWKULAR_APM_USERNAME", HAWKULAR_APM_USERNAME);

        Tracer tracer = openTracingHawkular();
        RestHandler restHandler = new RestHandler(tracer);
        register(restHandler);
        //register(new RestHandler(openTracingHawkular()));
    }

    public String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null) {
            logger.info("For " + name + " using default of " + defaultValue);
            return defaultValue;
        } else {
            logger.info("For " + name + "found: " + value);
            return value;
        }
    }

    Tracer openTracingHawkular() {
        TracePublisherRESTClient restClient = new TracePublisherRESTClient(HAWKULAR_APM_USERNAME, HAKWULAR_APM_PASSWORD, HAWKULAR_APM_URI);
        BatchTraceRecorder traceRecorder = new BatchTraceRecorder.BatchTraceRecorderBuilder()
                .withTracePublisher(restClient)
                .build();

        DeploymentMetaData deploymentMetaData = new DeploymentMetaData(DEMO_SERVICE_NAME, "1");
        APMTracer apmTracer = new APMTracer(traceRecorder, ALWAYS_SAMPLE, deploymentMetaData);
        return apmTracer;
    }

    /*
    Tracer openTracingZipkin() {
        Reporter<Span> reporter = AsyncReporter.builder(
                    URLConnectionSender.create("http://localhost:9411/api/v1/spans"))
                .build();

        brave.Tracer.Builder builder = new brave.Tracer.Builder()
                .localEndpoint(Endpoint.builder().serviceName("opentracing-demo").build())
                .sampler(brave.sampler.Sampler.ALWAYS_SAMPLE)
                .reporter(reporter);
        return BraveTracer.wrap(builder.build());
    }
    */
}
