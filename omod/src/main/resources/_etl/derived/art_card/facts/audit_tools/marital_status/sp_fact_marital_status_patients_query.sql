DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_patient_marital_status_query;
CREATE PROCEDURE sp_fact_patient_marital_status_query()
BEGIN
    SELECT *
    FROM mamba_fact_patients_marital_status;
END //

DELIMITER ;




