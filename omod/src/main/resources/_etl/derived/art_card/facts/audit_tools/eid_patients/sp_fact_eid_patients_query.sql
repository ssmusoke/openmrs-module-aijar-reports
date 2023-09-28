DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_eid_patients_query;
CREATE PROCEDURE sp_fact_eid_patients_query()
BEGIN
    SELECT *
    FROM mamba_fact_eid_patients;
END //

DELIMITER ;




