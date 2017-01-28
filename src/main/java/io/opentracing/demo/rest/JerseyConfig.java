package io.opentracing.demo.rest;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.hawkular.apm.client.api.recorder.BatchTraceRecorder;
import org.hawkular.apm.client.api.sampler.Sampler;
import org.hawkular.apm.client.opentracing.APMTracer;
import org.hawkular.apm.client.opentracing.DeploymentMetaData;
import org.hawkular.apm.trace.publisher.rest.client.TracePublisherRESTClient;
import org.springframework.stereotype.Component;

import brave.features.opentracing.BraveTracer;
import io.opentracing.Tracer;
import zipkin.Endpoint;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.urlconnection.URLConnectionSender;

/**
 * @author Pavol Loffay
 */
@Component
@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(new RestHandler(openTracingHakular()));
    }

    Tracer openTracingHakular() {
        BatchTraceRecorder traceRecorder = new BatchTraceRecorder.BatchTraceRecorderBuilder()
                .withTracePublisher(
                        new TracePublisherRESTClient("jdoe", "password","http://localhost:8080"))
                .build();

        return new APMTracer(traceRecorder, Sampler.ALWAYS_SAMPLE,
                new DeploymentMetaData("opentracing-demo", "1"));
    }

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
}
