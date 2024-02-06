-- $BEGIN
INSERT INTO mamba_fact_transfer_out (
                                  client_id,
                                  encounter_date,
                                  transfer_out_date
                                 )
SELECT person_id, obs_datetime, value_datetime from obs where concept_id=99165 and voided =0 ;

-- $END