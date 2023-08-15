-- $BEGIN
CREATE TABLE mamba_fact_patients_latest_adherence
(
    id        INT AUTO_INCREMENT,
    client_id INT NOT NULL,
    adherence VARCHAR(250) NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8MB4;

-- $END

