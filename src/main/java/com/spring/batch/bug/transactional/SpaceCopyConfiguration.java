package com.spring.batch.bug.transactional;

import com.spring.batch.bug.transactional.diffusion.DiffusionSpaceRepository;
import com.spring.batch.bug.transactional.model.Space;
import com.spring.batch.bug.transactional.preparation.PreparationSpaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.database.orm.JpaNativeQueryProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import static org.springframework.batch.repeat.RepeatStatus.FINISHED;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class SpaceCopyConfiguration {

    @Value("${page.size:100}")
    private final int pageSize;

    private final StepBuilderFactory stepBuilderFactory;
    private final JobBuilderFactory jobBuilderFactory;

    @Bean
    Step cleanPreparation(@Qualifier("preparationDataSource") DataSource preparationDataSource,
                          PreparationSpaceRepository preparationSpaceRepository) {
        return stepBuilderFactory.get("cleanPreparation")
                .tasklet((contribution, chunkContext) -> {

                    Flyway flyway = Flyway.configure()
                            .dataSource(preparationDataSource)
                            .schemas("preparation")
                            .load();

                    flyway.clean();
                    flyway.migrate();

                    preparationSpaceRepository.deleteAll();

                    return FINISHED;
                })
                .build();
    }

    @Bean
    Step cleanDiffusionNames(DiffusionSpaceRepository diffusionSpaceRepository) {
        return stepBuilderFactory.get("cleanDiffusionNames")
                .tasklet((contribution, chunkContext) -> {
                    diffusionSpaceRepository.cleanNames();
                    return FINISHED;
                })
                .build();
    }

    @Bean
    ItemWriter<Space> spacePreparationItemWriter(PreparationSpaceRepository preparationSpaceRepository) {
        return preparationSpaceRepository::saveAll;
    }

    @Bean
    ItemReader<Space> spaceDiffusionItemReader(@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        JpaNativeQueryProvider<Space> queryProvider = new JpaNativeQueryProvider<>();
        queryProvider.setSqlQuery(
                """
                             select *
                             from space
                             where status = 'INACTIVE'
                             order by id
                        """
        );
        queryProvider.setEntityClass(Space.class);

        return new JpaPagingItemReaderBuilder<Space>()
                .name("spaceItemReader")
                .queryProvider(queryProvider)
                .entityManagerFactory(entityManagerFactory)
                .pageSize(pageSize)
                //.transacted(false)
                .build();
    }

    @Bean
    ItemProcessor<Space, Space> spaceItemProcessor() {
        return space -> {
            space.setName(space.getId().substring(0, 4));
            return space;
        };
    }

    @Bean
    Step spaceInactiveItemsStep(@Qualifier("preparationTransactionManager") PlatformTransactionManager preparationTransactionManager) {
        return stepBuilderFactory
                .get("spaceInactiveItemsStep")
                .<Space, Space>chunk(pageSize)
                .reader(spaceDiffusionItemReader(null))
                .processor(spaceItemProcessor())
                .writer(spacePreparationItemWriter(null))
                .transactionManager(preparationTransactionManager)
                .build();
    }

    @Bean
    Step loggingStep(DiffusionSpaceRepository diffusionSpaceRepository, PreparationSpaceRepository preparationSpaceRepository) {
        return stepBuilderFactory.get("cleanDiffusionNames")
                .tasklet((contribution, chunkContext) -> {

                    log.info("#################################################");

                    long all = diffusionSpaceRepository.count();
                    long changedNames = diffusionSpaceRepository.countAllByNameIsNotNull();
                    log.info("Total in preparation : [{}], page size is [{}]", preparationSpaceRepository.count(), pageSize);
                    log.info("Total in diffusion : [{}]", all);
                    log.info("Changed names in diffusion : [{}], not modified [{}]", changedNames, all - changedNames);

                    log.info("#################################################");


                    return FINISHED;
                })
                .build();
    }

    @Bean
    Job copyJob() {
        return jobBuilderFactory.get("copyJob")
                .incrementer(new RunIdIncrementer())
                .start(cleanPreparation(null, null))
                .next(cleanDiffusionNames(null))
                .next(spaceInactiveItemsStep(null))
                .next(loggingStep(null, null))
                .build();
    }
}
