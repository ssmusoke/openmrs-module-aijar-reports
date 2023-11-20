DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_active_in_care_query;
CREATE PROCEDURE sp_fact_active_in_care_query(IN DAYS_LOST INT)
BEGIN
    SELECT *
    FROM mamba_fact_active_in_care WHERE days_left_to_be_lost >= DAYS_LOST;
END //

DELIMITER ;




