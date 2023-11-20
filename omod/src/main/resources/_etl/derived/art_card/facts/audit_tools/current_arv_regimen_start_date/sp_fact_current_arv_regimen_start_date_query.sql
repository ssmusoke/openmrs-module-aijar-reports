DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_current_arv_start_date_query;
CREATE PROCEDURE sp_fact_current_arv_start_date_query()
BEGIN
    SELECT *
    FROM mamba_fact_current_arv_regimen_start_date;
END //

DELIMITER ;




