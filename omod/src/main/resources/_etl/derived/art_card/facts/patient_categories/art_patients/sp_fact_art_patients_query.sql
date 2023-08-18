DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_art_patients_query;
CREATE PROCEDURE sp_fact_art_patients_query()
BEGIN
    SELECT *
    FROM mamba_fact_art_patients ;
END //

DELIMITER ;




