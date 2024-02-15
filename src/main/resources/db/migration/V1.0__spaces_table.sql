CREATE TABLE IF NOT EXISTS SPACE
(
    id            VARCHAR(100) NOT NULL,
    name          VARCHAR(100),
    status        VARCHAR(8)   NOT NULL,
    created       TIMESTAMP    NOT NULL,
    changed       TIMESTAMP    NOT NULL,
    valid_from    TIMESTAMP    NOT NULL,
    valid_to      TIMESTAMP,
    deletion_date TIMESTAMP,
    PRIMARY KEY (id)
);
