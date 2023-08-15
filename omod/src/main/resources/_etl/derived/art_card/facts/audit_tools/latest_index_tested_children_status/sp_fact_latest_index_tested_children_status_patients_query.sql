DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_patient_latest_index_tested_children_status_query;
CREATE PROCEDURE sp_fact_patient_latest_index_tested_children_status_query()
BEGIN
    SELECT *
    FROM mamba_fact_patients_latest_index_tested_children_status;
END //

DELIMITER ;




