DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_patient_latest_advanced_disease_query;
CREATE PROCEDURE sp_fact_patient_latest_advanced_disease_query()
BEGIN
    SELECT *
    FROM mamba_fact_patients_latest_advanced_disease;
END //

DELIMITER ;




