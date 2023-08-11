-- $BEGIN
INSERT INTO mamba_fact_current_arv_regimen_start_date (client_id,
                                                       arv_regimen_start_date)
SELECT B.client_id, MIN(encounter_date)
from mamba_fact_encounter_hiv_art_card mfehac
         join
     (SELECT client_id, current_arv_regimen
      from mamba_fact_encounter_hiv_art_card
               join
           (SELECT client_id person, max(encounter_date) as latest_encounter_date
            from mamba_fact_encounter_hiv_art_card
            where current_arv_regimen is NOT NULL
            GROUP BY client_id) A on client_id = person and encounter_date = latest_encounter_date) B
     on B.client_id = mfehac.client_id
where mfehac.current_arv_regimen = B.current_arv_regimen
GROUP BY B.client_id;
-- $END