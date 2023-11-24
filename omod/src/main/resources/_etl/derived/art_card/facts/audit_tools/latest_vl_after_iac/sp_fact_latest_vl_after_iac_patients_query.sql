DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_patient_latest_vl_after_iac_query;
CREATE PROCEDURE sp_fact_patient_latest_vl_after_iac_query()
BEGIN
    SELECT *
    FROM mamba_fact_patients_latest_vl_after_iac;
END //

DELIMITER ;




