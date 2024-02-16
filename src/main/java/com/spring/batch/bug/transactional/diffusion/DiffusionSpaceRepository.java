package com.spring.batch.bug.transactional.diffusion;

import com.spring.batch.bug.transactional.model.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface DiffusionSpaceRepository extends JpaRepository<Space, String> {


    @Modifying
    @Query("update Space s set s.name = null ")
    void cleanNames();

    long countAllByNameIsNotNull();
}
