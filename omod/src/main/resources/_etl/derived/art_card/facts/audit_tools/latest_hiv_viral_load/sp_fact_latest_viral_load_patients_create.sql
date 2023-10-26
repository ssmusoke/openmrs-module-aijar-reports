-- $BEGIN
CREATE TABLE mamba_fact_patients_latest_viral_load
(
    id        INT AUTO_INCREMENT,
    client_id INT NOT NULL,
    encounter_date DATE NULL,
    hiv_viral_load_copies INT NULL,
    hiv_viral_collection_date DATE NULL,
    specimen_type VARCHAR(100) NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8MB4;

CREATE INDEX
    mamba_fact_patients_latest_viral_load_client_id_index ON mamba_fact_patients_latest_viral_load (client_id);

-- $END

