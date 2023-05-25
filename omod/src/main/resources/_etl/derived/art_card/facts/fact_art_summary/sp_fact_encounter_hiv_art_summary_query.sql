DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_encounter_hiv_art_summary_query;
CREATE PROCEDURE sp_fact_encounter_hiv_art_summary_query(IN START_DATE
                                                     DATETIME, END_DATE DATETIME)
BEGIN
    SELECT *
    FROM mamba_fact_encounter_hiv_art_summary hiv_sum WHERE hiv_sum.encounter_date >= START_DATE
      AND hiv_sum.encounter_date <= END_DATE ;
END //

DELIMITER ;




