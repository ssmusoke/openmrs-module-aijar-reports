-- $BEGIN
INSERT INTO mamba_fact_patients_latest_iac_sessions(client_id,
                                                    encounter_date,
                                                    sessions)
SELECT obs.person_id,obs_datetime, COUNT(value_datetime) sessions
FROM obs
         INNER JOIN (SELECT person_id, MAX(DATE (value_datetime)) AS vldate
                     FROM obs
                     WHERE concept_id = 163023
                       AND voided = 0
                       AND value_datetime <= CURRENT_DATE()
                       AND obs_datetime <= CURRENT_DATE()
                     GROUP BY person_id) vl_date ON vl_date.person_id = obs.person_id
WHERE concept_id = 163154
  AND value_datetime >= vldate
  AND obs_datetime BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) AND CURRENT_DATE()
GROUP BY obs.person_id;
-- $END