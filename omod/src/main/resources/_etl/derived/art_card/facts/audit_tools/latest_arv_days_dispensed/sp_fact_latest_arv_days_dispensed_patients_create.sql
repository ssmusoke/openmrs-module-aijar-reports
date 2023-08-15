-- $BEGIN
CREATE TABLE mamba_fact_patients_latest_arv_days_dispensed
(
    id             INT AUTO_INCREMENT,
    client_id      INT NOT NULL,
    encounter_date DATE NULL,
    days         INT NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8MB4;

-- $END

