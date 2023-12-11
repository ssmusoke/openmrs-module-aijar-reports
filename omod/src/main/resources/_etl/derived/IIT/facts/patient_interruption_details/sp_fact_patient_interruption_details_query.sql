DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_patients_interruptions_details_query;
CREATE PROCEDURE sp_fact_patients_interruptions_details_query()
BEGIN
    SELECT *
    FROM mamba_fact_patients_interruptions_details;
END //

DELIMITER ;




