-- $BEGIN
CREATE TABLE mamba_fact_patients_latest_regimen_line
(
    id                                      INT AUTO_INCREMENT,
    client_id                               INT NOT NULL,
    regimen                             VARCHAR(80) NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8MB4;

-- $END

