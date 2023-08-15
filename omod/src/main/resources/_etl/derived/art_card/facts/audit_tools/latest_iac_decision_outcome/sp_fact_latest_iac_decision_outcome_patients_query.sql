DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_patient_latest_iac_decision_outcome_query;
CREATE PROCEDURE sp_fact_patient_latest_iac_decision_outcome_query()
BEGIN
    SELECT *
    FROM mamba_fact_patients_latest_iac_decision_outcome;
END //

DELIMITER ;




