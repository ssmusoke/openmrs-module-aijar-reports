DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_patient_latest_viral_load_ordered_query;
CREATE PROCEDURE sp_fact_patient_latest_viral_load_ordered_query()
BEGIN
    SELECT *
    FROM mamba_fact_patients_latest_viral_load_ordered;
END //

DELIMITER ;




