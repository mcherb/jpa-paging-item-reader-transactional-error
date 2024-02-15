package com.spring.batch.bug.transactional.model;

import com.spring.batch.bug.transactional.Status;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Space {

    @Id
    private String id;

    @Column
    private OffsetDateTime created;

    private OffsetDateTime changed;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "valid_from")
    private OffsetDateTime validFrom;

    @Column(name = "valid_to")
    private OffsetDateTime validTo;

    @Column(name = "deletion_date")
    private OffsetDateTime deletionDate;

    private String name;
}


