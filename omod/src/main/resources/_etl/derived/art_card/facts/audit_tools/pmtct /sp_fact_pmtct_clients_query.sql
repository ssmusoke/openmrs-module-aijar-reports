DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_encounter_hiv_art_card_query;
CREATE PROCEDURE sp_fact_encounter_hiv_art_card_query(IN START_DATE
                                                     DATETIME, END_DATE DATETIME)
BEGIN
    SELECT *
    FROM mamba_fact_encounter_hiv_art_card hiv_card WHERE hiv_card.encounter_date >= START_DATE
      AND hiv_card.encounter_date <= END_DATE ;
END //

DELIMITER ;




