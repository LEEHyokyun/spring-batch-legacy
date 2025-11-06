package com.system.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.atomic.AtomicInteger;

@Import(BatchConfig.class)
public class SystemTerminationConfig {
    /*
    * web run != batch run
    * batch 동작을 위한 CommandJobRunner 별도 실행 및 이를 실행하기 위한 SystemConfig FCQN / Job name 전달 필요
    * */
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private AtomicInteger processesKilled = new AtomicInteger(0);
    private final int TERMINATION_TARGET = 5;

    public SystemTerminationConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Job systemTerminationSimulationJob(){
        return new JobBuilder("systemTerminationSimulationJob", jobRepository)
                .start(enterWorldStep())
                .next(meetNPCStep())
                .next(defeatProcessStep())
                .next(completeQuestStep())
                .build();
    }

    @Bean
    public Step enterWorldStep(){
        return new StepBuilder("enterWorldStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Entered to System Termination World.");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step meetNPCStep(){
        return new StepBuilder("meetNPCStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("meet NPC.");
                    System.out.println("First mission : Zombie process : " + TERMINATION_TARGET + " has been Killed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step defeatProcessStep(){
        return new StepBuilder("defeatProcessStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                  int terminated = processesKilled.incrementAndGet();
                  System.out.println("defeatProcessStep is Running : target has been killed by "+ terminated);

                  if(terminated < TERMINATION_TARGET){
                      return RepeatStatus.CONTINUABLE;
                  }else{
                      return RepeatStatus.FINISHED;
                  }
                }, transactionManager)
                .build();
    }

    @Bean
    public Step completeQuestStep(){
        return new StepBuilder("completeQuestStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("complete quest.");
                    System.out.println("Congratulations! Your Batch Step has just reached the basic one!");
                    return  RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
