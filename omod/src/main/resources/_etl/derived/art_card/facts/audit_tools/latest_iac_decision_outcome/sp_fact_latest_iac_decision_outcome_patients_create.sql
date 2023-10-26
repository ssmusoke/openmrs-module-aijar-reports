-- $BEGIN
CREATE TABLE mamba_fact_patients_latest_iac_decision_outcome
(
    id             INT AUTO_INCREMENT,
    client_id      INT NOT NULL,
    encounter_date DATE NULL,
    decision         TEXT NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8MB4;

CREATE INDEX
    mamba_fact_patients_latest_iac_decision_outcome_client_id_index ON mamba_fact_patients_latest_iac_decision_outcome (client_id);
-- $END

