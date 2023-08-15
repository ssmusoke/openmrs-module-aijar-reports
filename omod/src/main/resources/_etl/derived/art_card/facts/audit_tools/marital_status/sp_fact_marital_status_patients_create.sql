-- $BEGIN
CREATE TABLE mamba_fact_patients_marital_status
(
    id             INT AUTO_INCREMENT,
    client_id      INT NOT NULL,
    marital_status VARCHAR(80) NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8MB4;

-- $END

