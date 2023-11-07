-- $BEGIN
CREATE TABLE mamba_fact_patients_latest_current_regimen
(
    id              INT AUTO_INCREMENT,
    client_id       INT NOT NULL,
    current_regimen VARCHAR(250) NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8;

CREATE INDEX
    mamba_fact_patients_latest_current_regimen_client_id_index ON mamba_fact_patients_latest_current_regimen (client_id);

-- $END

