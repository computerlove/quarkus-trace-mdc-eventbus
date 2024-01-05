package org.acme;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.opentelemetry.runtime.QuarkusContextStorage;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.MultiMap;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

@ApplicationScoped
public class Listener {
    private final Logger log;
    private final HelloClient helloClient;
    private final Otel tracer;

    public Listener(Logger log,
                    @RestClient HelloClient helloClient,
                    Otel tracer) {
        this.log = log;
        this.helloClient = helloClient;
        this.tracer = tracer;
    }

    @ConsumeEvent(value = HelloEvent.NAME, blocking = true)
    public void listen(MultiMap headers, HelloEvent helloEvent) {
        String traceparent = headers.get("traceparent");
        try(var context = tracer.fromTraceParent(traceparent)) {
            log.infof("fromTraceParent %s %s", headers, helloEvent);
            String hello = helloClient.hello(helloEvent.hello() + " fromTraceParent");
            log.info(hello);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @ConsumeEvent(value = HelloEvent.NAME, blocking = true)
    @WithSpan
    public void listen2(MultiMap headers, HelloEvent helloEvent) {
        log.infof("@WithSpan %s %s", headers, helloEvent);
        String hello = helloClient.hello(helloEvent.hello() + " @WithSpan");
        log.info(hello);
    }
}
