package org.acme;

import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.quarkus.opentelemetry.runtime.QuarkusContextStorage;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;


@ApplicationScoped
public class Otel {
    private final boolean doManual;
    private final Tracer tracer;

    public Otel(@ConfigProperty(name = "context.manual") boolean doManual, Tracer tracer) {
        this.doManual = doManual;
        this.tracer = tracer;
    }

    public AutoCloseable getOtelContext(int message) {
        Context current = Context.current();
        Span span = tracer.spanBuilder("servicebus message")
                .setAttribute("message", message)
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();
        Context context = current.with(span);
        return makeContext(context, span);
    }

    private static AutoCloseable makeContext(Context with1, Span span) {
        Scope scope = QuarkusContextStorage.INSTANCE.attach(with1);
        return () -> {
            span.end();
            scope.close();
        };
    }

    public AutoCloseable fromTraceParent(String traceparent) {
        if(doManual) {
            Context current = QuarkusContextStorage.INSTANCE.current() == null ? Context.current() : QuarkusContextStorage.INSTANCE.current();
            String[] split = traceparent.split("-");
            SpanContext spanContext = SpanContext.createFromRemoteParent(split[1], split[2], TraceFlags.getDefault(), TraceState.getDefault());
            Span activeSpan = Span.wrap(spanContext);
            Context with = current.with(activeSpan);
            return makeContext(with, activeSpan);
        } else {
            return () -> {};
        }
    }
}
