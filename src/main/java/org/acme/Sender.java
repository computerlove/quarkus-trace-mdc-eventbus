package org.acme;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.jboss.logging.Logger;

import java.time.Duration;

@ApplicationScoped
public class Sender {
    private final EventBus eventBus;
    private final Logger log;

    private final Otel tracer;

    public Sender(EventBus eventBus, Logger log, Otel tracer) {
        this.eventBus = eventBus;
        this.log = log;
        this.tracer = tracer;
    }

    void start(@Observes StartupEvent event) {
        Thread.ofVirtual().start(() -> {
            int i = 1;
            while(true) {
                try(var context = tracer.getOtelContext(i++)) {
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
