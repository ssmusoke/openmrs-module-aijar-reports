-- $BEGIN
CREATE TABLE mamba_fact_patients_latest_nutrition_support
(
    id             INT AUTO_INCREMENT,
    client_id      INT NOT NULL,
    encounter_date DATE NULL,
    support         VARCHAR(100) NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8MB4;

CREATE INDEX
    mamba_fact_patients_latest_nutrition_support_client_id_index ON mamba_fact_patients_latest_nutrition_support (client_id);

-- $END

