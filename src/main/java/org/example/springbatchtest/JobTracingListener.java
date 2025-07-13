package org.example.springbatchtest;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Tracer.SpanInScope;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobTracingListener implements JobExecutionListener {

    private final Tracer tracer;

    private static final ThreadLocal<SpanInScope> spanScopeThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Span> spanThreadLocal = new ThreadLocal<>();

    @Override
    public void beforeJob(JobExecution jobExecution) {
        Span jobSpan = tracer.nextSpan().name("Job:" + jobExecution.getJobInstance().getJobName()).start();
        SpanInScope scope = tracer.withSpan(jobSpan);

        // Store in ThreadLocal for isolation
        spanScopeThreadLocal.set(scope);
        spanThreadLocal.set(jobSpan);

        // Set MDC
        MDC.put("traceId", jobSpan.context().traceId());
        MDC.put("spanId", jobSpan.context().spanId());
    }

    /*@Override
    public void afterJob(JobExecution jobExecution) {
        // Safely retrieve span and scope from ThreadLocal
        SpanInScope scope = spanScopeThreadLocal.get();
        Span span = spanThreadLocal.get();

        if (scope != null) {
            scope.close();
            spanScopeThreadLocal.remove();
        }

        if (span != null) {
            span.end();
            spanThreadLocal.remove();
        }

        // Clean up MDC for this thread
        MDC.remove("traceId");
        MDC.remove("spanId");
    }*/

    @Override
    public void afterJob(JobExecution jobExecution) {
        SpanInScope scope = spanScopeThreadLocal.get();
        Span span = spanThreadLocal.get();

        if (scope != null) {
            scope.close();
            spanScopeThreadLocal.remove(); // Prevent leakage
        }

        if (span != null) {
            // Add optional tags for final job status, duration, etc.
            span.tag("job.name", jobExecution.getJobInstance().getJobName());
            span.tag("job.status", jobExecution.getStatus().toString());
            span.end(); // End span cleanly
            spanThreadLocal.remove();
        }

        // ❌ Do NOT remove MDC here
        // It is now managed outside (e.g., in CommandLineRunner)
    }
}
