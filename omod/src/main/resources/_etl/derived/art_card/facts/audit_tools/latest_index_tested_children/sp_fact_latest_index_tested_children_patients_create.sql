-- $BEGIN
CREATE TABLE mamba_fact_patients_latest_index_tested_children
(
    id                                      INT AUTO_INCREMENT,
    client_id                               INT NOT NULL,
    no                            INT NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8;

CREATE INDEX
    mamba_fact_patients_tested_children_client_id_index ON mamba_fact_patients_latest_index_tested_children (client_id);

-- $END

