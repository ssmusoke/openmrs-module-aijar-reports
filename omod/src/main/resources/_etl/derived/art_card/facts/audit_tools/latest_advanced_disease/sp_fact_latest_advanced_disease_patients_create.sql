-- $BEGIN
CREATE TABLE mamba_fact_patients_latest_advanced_disease
(
    id                                      INT AUTO_INCREMENT,
    client_id                               INT NOT NULL,
    encounter_date                          DATE NULL,
    advanced_disease                        VARCHAR(100) NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8;

CREATE INDEX
    mamba_fact_patients_latest_advanced_disease_client_id_index ON mamba_fact_patients_latest_advanced_disease (client_id);

-- $END

