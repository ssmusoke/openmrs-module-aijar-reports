-- $BEGIN
INSERT INTO mamba_fact_transfer_in (
                                  client_id,
                                  encounter_date,
                                  transfer_in_date
                                 )
SELECT person_id, obs_datetime, value_datetime from obs where concept_id=99160 and voided =0 ;

-- $END