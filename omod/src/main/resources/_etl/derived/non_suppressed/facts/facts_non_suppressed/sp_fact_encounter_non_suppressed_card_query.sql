DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_encounter_non_suppressed_card_query;
CREATE PROCEDURE sp_fact_encounter_non_suppressed_card_query(IN START_DATE
                                                     DATETIME, END_DATE DATETIME)
BEGIN
    SELECT *
    FROM mamba_fact_encounter_non_suppressed_card non_suppressed WHERE non_suppressed.encounter_date >= START_DATE
      AND non_suppressed.encounter_date <= END_DATE ;
END //

DELIMITER ;




