DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_patient_latest_pregnancy_status_query;
CREATE PROCEDURE sp_fact_patient_latest_pregnancy_status_query()
BEGIN
    SELECT *
    FROM mamba_fact_patients_latest_pregnancy_status;
END //

DELIMITER ;




