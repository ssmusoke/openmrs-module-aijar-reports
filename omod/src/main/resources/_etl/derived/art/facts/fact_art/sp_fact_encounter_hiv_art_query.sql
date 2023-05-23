DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_encounter_hiv_art_query;
CREATE PROCEDURE sp_fact_encounter_hiv_art_query(IN START_DATE
                                                     DATETIME, END_DATE DATETIME)
BEGIN
    SELECT hivart.return_date,
           hivart.current_regimen,
           hivart.who_stage,
           hivart.no_of_days,
           hivart.tb_status,
           hivart.dsdm,
           hivart.pregnant,
           hivart.emtct
    FROM mamba_fact_encounter_hiv_art hivart

    WHERE hivart.return_date >= START_DATE
      AND hivart.return_date <= END_DATE;
END //

DELIMITER ;




