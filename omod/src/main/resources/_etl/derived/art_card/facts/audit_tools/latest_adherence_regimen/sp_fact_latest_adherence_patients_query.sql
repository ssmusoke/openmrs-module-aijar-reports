DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_patient_latest_adherence_query;
CREATE PROCEDURE sp_fact_patient_latest_adherence_query()
BEGIN
    SELECT *
    FROM mamba_fact_patients_latest_adherence;
END //

DELIMITER ;




