DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_encounter_hiv_art_query;
CREATE PROCEDURE sp_fact_encounter_hiv_art_query(IN START_DATE
                                                     DATETIME, END_DATE DATETIME)
BEGIN
    SELECT pn.given_name,
           TIMESTAMPDIFF(YEAR,p.birthdate,CURRENT_DATE),
           p.gender,
            pi.identifier,
           hivart.return_date,
           hivart.current_regimen,
           hivart.who_stage,
           hivart.no_of_days,
           hivart.tb_status,
           hivart.dsdm,
           hivart.pregnant,
           hivart.emtct
    FROM mamba_fact_encounter_hiv_art hivart INNER JOIN mamba_dim_person_name pn on client_id=pn.external_person_id
     INNER JOIN mamba_dim_person p on client_id= p.external_person_id inner join patient_identifier pi on client_id =pi.patient_id
    WHERE hivart.return_date >= START_DATE
      AND hivart.return_date <= END_DATE AND pi.identifier_type=4;
END //

DELIMITER ;




