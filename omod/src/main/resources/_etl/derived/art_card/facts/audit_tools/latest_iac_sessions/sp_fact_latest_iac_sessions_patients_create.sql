-- $BEGIN
CREATE TABLE mamba_fact_patients_latest_iac_sessions
(
    id             INT AUTO_INCREMENT,
    client_id      INT NOT NULL,
    encounter_date DATE NULL,
    sessions         INT NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8MB4;

CREATE INDEX
    mamba_fact_patients_latest_iac_sessions_client_id_index ON mamba_fact_patients_latest_iac_sessions (client_id);

-- $END

