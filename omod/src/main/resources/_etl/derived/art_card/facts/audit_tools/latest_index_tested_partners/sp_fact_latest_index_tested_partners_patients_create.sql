-- $BEGIN
CREATE TABLE mamba_fact_patients_latest_index_tested_partners
(
    id                                      INT AUTO_INCREMENT,
    client_id                               INT NOT NULL,
    no                            INT NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8MB4;

CREATE INDEX
    mamba_fact_patients_latest_partners_client_id_index ON mamba_fact_patients_latest_index_tested_partners (client_id);

-- $END

