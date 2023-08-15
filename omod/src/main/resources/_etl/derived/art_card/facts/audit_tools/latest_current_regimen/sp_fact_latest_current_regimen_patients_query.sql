DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_patient_latest_current_regimen_query;
CREATE PROCEDURE sp_fact_patient_latest_current_regimen_query()
BEGIN
    SELECT *
    FROM mamba_fact_patients_latest_current_regimen;
END //

DELIMITER ;




