-- $BEGIN
CREATE TABLE mamba_fact_patients_nationality
(
    id                                      INT AUTO_INCREMENT,
    client_id                               INT NOT NULL,
    nationality                             VARCHAR(80) NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8MB4;

CREATE INDEX
    mamba_fact_patients_nationality_client_id_index ON mamba_fact_patients_nationality (client_id);

-- $END

