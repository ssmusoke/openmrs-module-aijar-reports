DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_patient_latest_arv_days_dispensed_query;
CREATE PROCEDURE sp_fact_patient_latest_arv_days_dispensed_query()
BEGIN
    SELECT *
    FROM mamba_fact_patients_latest_arv_days_dispensed;
END //

DELIMITER ;




