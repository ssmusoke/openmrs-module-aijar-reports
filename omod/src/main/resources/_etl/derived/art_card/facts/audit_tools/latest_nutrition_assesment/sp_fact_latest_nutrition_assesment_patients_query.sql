DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_patient_latest_nutrition_assesment_query;
CREATE PROCEDURE sp_fact_patient_latest_nutrition_assesment_query()
BEGIN
    SELECT *
    FROM mamba_fact_patients_latest_nutrition_assesment;
END //

DELIMITER ;




