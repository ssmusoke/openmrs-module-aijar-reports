-- $BEGIN
CREATE TABLE mamba_fact_patients_latest_vl_after_iac
(
    id             INT AUTO_INCREMENT,
    client_id      INT NOT NULL,
    encounter_date DATE NULL,
    results        VARCHAR(100) NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8MB4;

-- $END

