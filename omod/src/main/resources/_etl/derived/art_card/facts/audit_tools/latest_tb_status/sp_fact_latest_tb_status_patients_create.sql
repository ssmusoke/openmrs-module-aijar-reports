-- $BEGIN
CREATE TABLE mamba_fact_patients_latest_tb_status
(
    id             INT AUTO_INCREMENT,
    client_id      INT NOT NULL,
    encounter_date DATE NULL,
    status         VARCHAR(100) NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8MB4;

-- $END

