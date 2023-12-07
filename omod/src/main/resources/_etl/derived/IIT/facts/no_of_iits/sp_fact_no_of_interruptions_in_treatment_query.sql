DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_no_of_interruptions_in_treatment_query;
CREATE PROCEDURE sp_fact_no_of_interruptions_in_treatment_query()
BEGIN
    SELECT *
    FROM mamba_fact_patients_no_of_interruptions;
END //

DELIMITER ;




