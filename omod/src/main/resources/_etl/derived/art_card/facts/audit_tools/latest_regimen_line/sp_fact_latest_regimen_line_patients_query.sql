DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_patient_latest_regimen_line_query;
CREATE PROCEDURE sp_fact_patient_latest_regimen_line_query()
BEGIN
    SELECT *
    FROM mamba_fact_patients_latest_regimen_line;
END //

DELIMITER ;




