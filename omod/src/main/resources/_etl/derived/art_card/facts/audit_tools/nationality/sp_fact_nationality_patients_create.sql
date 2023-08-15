-- $BEGIN
CREATE TABLE mamba_fact_patients_nationality
(
    id                                      INT AUTO_INCREMENT,
    client_id                               INT NOT NULL,
    nationality                             VARCHAR(80) NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8MB4;

-- $END

