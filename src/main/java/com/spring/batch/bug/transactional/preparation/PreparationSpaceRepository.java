package com.spring.batch.bug.transactional.preparation;

import com.spring.batch.bug.transactional.model.Space;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreparationSpaceRepository extends JpaRepository<Space, String> {
}
