DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_patient_latest_viral_load_query;
CREATE PROCEDURE sp_fact_patient_latest_viral_load_query()
BEGIN
    SELECT *
    FROM mamba_fact_patients_latest_viral_load;
END //

DELIMITER ;




