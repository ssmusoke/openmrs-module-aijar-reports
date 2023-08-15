DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_patient_latest_family_planning_query;
CREATE PROCEDURE sp_fact_patient_latest_family_planning_query()
BEGIN
    SELECT *
    FROM mamba_fact_patients_latest_family_planning;
END //

DELIMITER ;




