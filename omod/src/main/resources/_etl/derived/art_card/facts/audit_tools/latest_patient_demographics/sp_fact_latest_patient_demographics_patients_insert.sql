-- $BEGIN
INSERT INTO mamba_fact_patients_latest_patient_demographics(patient_id,
                                                       birthdate,
                                                       age,
                                                       gender,
                                                       dead)
SELECT person_id,
       birthdate,
       TIMESTAMPDIFF(YEAR, birthdate, NOW()) AS age,
       gender,
       dead
from mamba_dim_person where voided=0;
-- $END