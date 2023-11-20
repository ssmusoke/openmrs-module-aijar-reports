DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_encounter_hiv_art_health_education_query;
CREATE PROCEDURE sp_fact_encounter_hiv_art_health_education_query(IN START_DATE
                                                     DATETIME, END_DATE DATETIME)
BEGIN
    SELECT *
    FROM mamba_fact_encounter_hiv_art_health_education hiv_health WHERE hiv_health.encounter_date >= START_DATE
      AND hiv_health.encounter_date<= END_DATE ;
END //

DELIMITER ;




