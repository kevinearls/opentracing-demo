package io.opentracing.demo.rest;

import javax.ws.rs.ApplicationPath;

import brave.opentracing.BraveTracer;
import org.glassfish.jersey.server.ResourceConfig;
import org.hawkular.apm.client.api.recorder.BatchTraceRecorder;
import org.hawkular.apm.client.opentracing.APMTracer;
import org.hawkular.apm.client.opentracing.DeploymentMetaData;
import org.hawkular.apm.trace.publisher.rest.client.TracePublisherRESTClient;
import org.springframework.stereotype.Component;

import io.opentracing.Tracer;
import zipkin.Endpoint;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.urlconnection.URLConnectionSender;

import static org.hawkular.apm.client.api.sampler.Sampler.ALWAYS_SAMPLE;

/**
 * @author Pavol Loffay
 */
@Component
@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {

    public static final String HAWKULAR_APM_URL =  "http://localhost:8080";
    public static final String HAKWULAR_APM_PASSWORD = "password";
    public static final String HAWKULAR_APM_USER = "jdoe";
    public static final String DEMO_SERVICE_NAME = "opentracing-demo";

    public JerseyConfig() {
        register(new RestHandler(openTracingHakular()));
    }

    Tracer openTracingHakular() {
        TracePublisherRESTClient restClient = new TracePublisherRESTClient(HAWKULAR_APM_USER, HAKWULAR_APM_PASSWORD, HAWKULAR_APM_URL);
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
