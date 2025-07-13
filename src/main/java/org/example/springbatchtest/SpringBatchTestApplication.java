package org.example.springbatchtest;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.MDC;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableBatchProcessing
public class SpringBatchTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchTestApplication.class, args);
    }

    @Bean
    public CommandLineRunner runJobAtStartup(JobLauncher jobLauncher, Job exampleJob, Tracer tracer) {
        return args -> {
            Span rootSpan = tracer.nextSpan().name("StartupJob:" + exampleJob.getName()).start();
            try (Tracer.SpanInScope scope = tracer.withSpan(rootSpan)) {
                // Set MDC to propagate to logging
                MDC.put("traceId", rootSpan.context().traceId());
                MDC.put("spanId", rootSpan.context().spanId());

                JobParameters jobParameters = new JobParametersBuilder()
                        .addLong("run.id", System.currentTimeMillis())
                        .toJobParameters();

                jobLauncher.run(exampleJob, jobParameters);
            } finally {
                rootSpan.end();
                MDC.clear(); // Clean up after done
            }
        };
    }

}
