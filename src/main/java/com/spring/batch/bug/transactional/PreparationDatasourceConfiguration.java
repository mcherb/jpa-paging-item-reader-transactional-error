package com.spring.batch.bug.transactional;

import com.spring.batch.bug.transactional.model.Space;
import com.spring.batch.bug.transactional.preparation.PreparationSpaceRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "preparationEntityManagerFactory",
        transactionManagerRef = "preparationTransactionManager",
        basePackageClasses = PreparationSpaceRepository.class
)
public class PreparationDatasourceConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "preparation.datasource")
    public DataSourceProperties preparationDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "preparation.datasource.hikari")
    public HikariDataSource preparationDataSource() {
        return preparationDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean preparationEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("preparationDataSource") DataSource preparationDataSource) {
        return builder
                .dataSource(preparationDataSource)
                .packages(Space.class)
                .persistenceUnit("preparation")
                .build();
    }

    @Bean
    public PlatformTransactionManager preparationTransactionManager(
            @Qualifier("preparationEntityManagerFactory") EntityManagerFactory preparationEntityManagerFactory) {
        return new JpaTransactionManager(preparationEntityManagerFactory);
    }
}
