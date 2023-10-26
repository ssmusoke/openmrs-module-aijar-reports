-- $BEGIN
CREATE TABLE mamba_fact_patients_latest_arv_days_dispensed
(
    id             INT AUTO_INCREMENT,
    client_id      INT NOT NULL,
    encounter_date DATE NULL,
    days         INT NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8MB4;

CREATE INDEX
    mamba_fact_patients_latest_arv_days_dispensed_client_id_index ON mamba_fact_patients_latest_arv_days_dispensed (client_id);

-- $END

