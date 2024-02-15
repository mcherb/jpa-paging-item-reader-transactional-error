package com.spring.batch.bug.transactional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBatchTransactionalErrorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchTransactionalErrorApplication.class, args);
    }
}
