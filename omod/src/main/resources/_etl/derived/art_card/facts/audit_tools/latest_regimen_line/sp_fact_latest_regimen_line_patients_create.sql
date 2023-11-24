-- $BEGIN
CREATE TABLE mamba_fact_patients_latest_regimen_line
(
    id                                      INT AUTO_INCREMENT,
    client_id                               INT NOT NULL,
    regimen                             VARCHAR(80) NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8;

CREATE INDEX
    mamba_fact_patients_latest_regimen_line_client_id_index ON mamba_fact_patients_latest_regimen_line (client_id);

-- $END

