package org.acme;

import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.quarkus.opentelemetry.runtime.QuarkusContextStorage;
import org.slf4j.MDC;


public class Otel {
    public static AutoCloseable getOtelContext(Tracer tracer, int message) {
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

    public static AutoCloseable fromTraceParent(String traceparent) {
        Context current = QuarkusContextStorage.INSTANCE.current() == null ?  Context.current() : QuarkusContextStorage.INSTANCE.current();
        String[] split = traceparent.split("-");
        SpanContext spanContext = SpanContext.createFromRemoteParent(split[1], split[2], TraceFlags.getDefault(), TraceState.getDefault());
        Span activeSpan = Span.wrap(spanContext);
        Context with = current.with(activeSpan);
        return makeContext(with, activeSpan);
    }
}
