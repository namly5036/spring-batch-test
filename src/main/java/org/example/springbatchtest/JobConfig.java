package org.example.springbatchtest;

import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class JobConfig {

    @Bean
    public Tasklet exampleTasklet(Tracer tracer) {
        return (contribution, chunkContext) -> {
            log.info("Running the example tasklet...");
            log.info("Current traceId: {}", tracer.currentSpan().context().traceId());
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Job exampleJob(JobRepository jobRepository, Step exampleStep, JobExecutionListener jobTracingListener) {
        return new JobBuilder("exampleJob", jobRepository)
                .listener(jobTracingListener)
                .incrementer(new RunIdIncrementer())
                .start(exampleStep)
                .build();
    }

    @Bean
    public Step exampleStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, Tasklet tasklet) {
        return new StepBuilder("exampleStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}
