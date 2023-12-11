-- $BEGIN
CREATE TABLE mamba_fact_patients_no_of_interruptions
(
    id                                      INT AUTO_INCREMENT,
    client_id                               INT NOT NULL,
    encounter_date                          DATE NULL,
    return_date                             DATE NULL,
    days_interrupted                        INT NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8;

CREATE INDEX
    mamba_fact_patients_no_of_interruptions_client_id_index ON mamba_fact_patients_no_of_interruptions (client_id);
CREATE INDEX
    mamba_fact_patients_no_of_interruptions_encounter_date_index ON mamba_fact_patients_no_of_interruptions (encounter_date);

-- $END

