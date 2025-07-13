package org.example.springbatchtest;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class TracingConfig {

    @Bean
    public DefaultTracingObservationHandler tracingHandler(Tracer tracer) {
        return new DefaultTracingObservationHandler(tracer);
    }

    @Bean
    public InitializingBean registerHandlerWithRegistry(ObservationRegistry registry, DefaultTracingObservationHandler handler) {
        return () -> registry.observationConfig().observationHandler(handler);
    }

}
