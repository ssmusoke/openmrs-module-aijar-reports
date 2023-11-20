DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_patient_latest_patient_demographics_query;
CREATE PROCEDURE sp_fact_patient_latest_patient_demographics_query()
BEGIN
    SELECT *
    FROM mamba_fact_patients_latest_patient_demographics;
END //

DELIMITER ;




