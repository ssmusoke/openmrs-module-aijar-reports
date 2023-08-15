-- $BEGIN
CREATE TABLE mamba_fact_patients_latest_current_regimen
(
    id              INT AUTO_INCREMENT,
    client_id       INT NOT NULL,
    current_regimen VARCHAR(250) NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8MB4;

-- $END

