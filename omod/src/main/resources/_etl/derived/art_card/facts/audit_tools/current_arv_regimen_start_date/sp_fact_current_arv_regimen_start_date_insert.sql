-- $BEGIN
INSERT INTO mamba_fact_current_arv_regimen_start_date (client_id,
                                                       arv_regimen_start_date)
SELECT B.client_id, MIN(encounter_date)
from mamba_fact_encounter_hiv_art_card mfehac
         join mamba_fact_patients_latest_current_regimen
      B
     on B.client_id = mfehac.client_id
where mfehac.current_arv_regimen = B.current_regimen
GROUP BY B.client_id;
-- $END