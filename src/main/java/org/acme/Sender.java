package org.acme;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.quarkus.opentelemetry.runtime.QuarkusContextStorage;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.jboss.logging.Logger;
import org.slf4j.MDC;

import java.time.Duration;

import static org.acme.Otel.getOtelContext;

@ApplicationScoped
public class Sender {
    private final EventBus eventBus;
    private final Logger log;

    private final Tracer tracer;

    public Sender(EventBus eventBus, Logger log, Tracer tracer) {
        this.eventBus = eventBus;
        this.log = log;
        this.tracer = tracer;
    }

    void start(@Observes StartupEvent event) {
        Thread.ofVirtual().start(() -> {
            int i = 1;
            while(true) {
                try(var context = getOtelContext(tracer, i++)) {
                    doStuff(i);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                try {
                    Thread.sleep(Duration.ofSeconds(10));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void doStuff(int n) {
        log.info("doing stuff " + n);
        eventBus.publish(HelloEvent.NAME, new HelloEvent("Hello " + n));
        log.info("Did stuff " + n);
    }


}
