-- $BEGIN
INSERT INTO mamba_fact_patients_marital_status (client_id,
                                                marital_status)
SELECT person_id, mdcn.name
FROM person_attribute pa
         INNER JOIN person_attribute_type pat
                    ON pa.person_attribute_type_id = pat.person_attribute_type_id
         INNER JOIN mamba_dim_concept_name mdcn ON pa.value = mdcn.concept_id
WHERE pat.uuid = '8d871f2a-c2cc-11de-8d13-0010c6dffd0f'
  AND pa.voided = 0
  AND mdcn.locale = 'en'
  AND mdcn.concept_name_type = 'FULLY_SPECIFIED';
-- $END