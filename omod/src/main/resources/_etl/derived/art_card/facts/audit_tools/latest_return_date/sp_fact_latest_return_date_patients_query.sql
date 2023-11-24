DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_patient_latest_return_date_query;
CREATE PROCEDURE sp_fact_patient_latest_return_date_query()
BEGIN
    SELECT *
    FROM mamba_fact_patients_latest_return_date;
END //

DELIMITER ;




