-- $BEGIN
CREATE TABLE mamba_fact_patients_latest_index_tested_children_status
(
    id                                      INT AUTO_INCREMENT,
    client_id                               INT NOT NULL,
    no                            INT NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8;

CREATE INDEX
    mamba_patients_latest_children_status_client_id_index ON mamba_fact_patients_latest_index_tested_children_status (client_id);

-- $END

