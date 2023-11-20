DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_patient_latest_nutrition_support_query;
CREATE PROCEDURE sp_fact_patient_latest_nutrition_support_query()
BEGIN
    SELECT *
    FROM mamba_fact_patients_latest_nutrition_support;
END //

DELIMITER ;




