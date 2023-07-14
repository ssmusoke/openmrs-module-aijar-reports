-- $BEGIN
INSERT INTO mamba_fact_current_arv_regimen_start_date (client_id, current_arv_regimen,
                                                       arv_regimen_start_date)
SELECT mfehac.client_id, mfehac.current_arv_regimen, MIN(encounter_date) as arv_start_date
from mamba_fact_encounter_hiv_art_card mfehac
         INNER JOIN
     (SELECT client_id, max(encounter_date) latest_encounter_date, current_arv_regimen
      from mamba_fact_encounter_hiv_art_card
      GROUP BY client_id) current_regimen on current_regimen.client_id = mfehac.client_id
WHERE mfehac.current_arv_regimen = current_regimen.current_arv_regimen
GROUP BY mfehac.client_id;
-- $END