-- $BEGIN
INSERT INTO mamba_fact_patients_nationality (client_id,
                                                nationality)
SELECT person_id, mdcn.name
FROM person_attribute pa
         INNER JOIN person_attribute_type pat
                    ON pa.person_attribute_type_id = pat.person_attribute_type_id
         INNER JOIN mamba_dim_concept_name mdcn ON pa.value = mdcn.concept_id
WHERE pat.uuid = 'dec484be-1c43-416a-9ad0-18bd9ef28929'
  AND pa.voided = 0
  AND mdcn.locale = 'en'
  AND mdcn.concept_name_type = 'FULLY_SPECIFIED';
-- $END