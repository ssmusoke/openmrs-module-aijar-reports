-- $BEGIN
INSERT INTO mamba_fact_eid_patients (client_id,
                                     edd,
                                     eid_no,
                                     eid_dob,
                                     eid_age,
                                     eid_weight,
                                     eid_next_appt,
                                     eid_feeding,
                                     ctx_start,
                                     ctx_age,
                                     1st_pcr_date,
                                     1st_pcr_age,
                                     1st_pcr_result,
                                     1st_pcr_received,
                                     2nd_pcr_date,
                                     2nd_pcr_age,
                                     2nd_pcr_result,
                                     2nd_pcr_received,
                                     repeat_pcr_date,
                                     repeat_pcr_age,
                                     repeat_pcr_result,
                                     repeat_pcr_received,
                                     rapid_pcr_date,
                                     rapid_pcr_age,
                                     rapid_pcr_result,
                                     final_outcome,
                                     linkage_no,
                                     nvp_at_birth,
                                     breast_feeding_stopped,
                                     pmtct_status,
                                     pmtct_enrollment_date,
                                     baby)
SELECT patient,
       edd.edd_date,
       eidno.id                                                                                    AS eidno,
       eiddob.dob                                                                                  AS eid_dob,
       TIMESTAMPDIFF(MONTH, eiddob.dob, CURRENT_DATE())                                            AS eid_age,
       eid_w.value_numeric                                                                         AS eid_weight,
       eid_next_appt.value_datetime                                                                AS next_appointment_date,
       eid_feeding.name                                                                            AS feeding,
       ctx.mydate                                                                                  AS ctx_start,
       TIMESTAMPDIFF(MONTH, eiddob.dob, ctx.mydate)                                                AS agectx,
       1stpcr.mydate                                                                               AS 1stpcrdate,
       TIMESTAMPDIFF(MONTH, eiddob.dob, 1stpcr.mydate)                                             AS age1stpcr,
       1stpcrresult.name,
       1stpcrreceived.mydate                                                                       AS 1stpcrrecieved,
       2ndpcr.mydate                                                                               AS 2ndpcrdate,
       TIMESTAMPDIFF(MONTH, eiddob.dob, 2ndpcr.mydate)                                             AS age2ndpcr,
       2ndpcrresult.name,
       2ndpcrreceived.mydate                                                                       AS 2ndpcrrecieved,
       repeatpcr.mydate                                                                            AS repeatpcrdate,
       TIMESTAMPDIFF(MONTH, eiddob.dob, repeatpcr.mydate)                                          AS age3rdpcr,
       repeatpcrresult.name,
       repeatpcrreceived.mydate                                                                    AS repeatpcrrecieved,
       rapidtest.mydate                                                                            AS rapidtestdate,
       TIMESTAMPDIFF(MONTH, eiddob.dob, rapidtest.mydate)                                          AS ageatrapidtest,
       rapidtestresult.name,
       finaloutcome.name,
       linkageno.value_text,
       IF(nvp.mydate IS NULL, '', IF(TIMESTAMPDIFF(DAY, eiddob.dob, nvp.mydate) <= 2, 'Y', 'N'))   AS nvp,
       stopped_bf.latest_date                                                                      AS breast_feeding_stopped,
       IF(cohort.pmtct = 'Not Pregnant Not BreastFeeding', 'Stopped Breast Feeding', cohort.pmtct) AS pmtct,
       enrollment_date,
       babies                                                                                      AS baby

FROM (
         # mothers with babies
         SELECT person_a AS patient, person_b AS babies, pmtct_enrollment.enrollment_date, preg_status.status AS pmtct
         FROM relationship r
                  INNER JOIN person p ON r.person_a = p.person_id
                  INNER JOIN person p1 ON r.person_b = p1.person_id
                  INNER JOIN relationship_type rt
                             ON r.relationship = rt.relationship_type_id AND
                                rt.uuid = '8d91a210-c2cc-11de-8d13-0010c6dffd0f'
                  LEFT JOIN (SELECT client_id, MIN(encounter_date) enrollment_date
                             FROM mamba_fact_encounter_hiv_art_card
                             WHERE pregnant = 'Breast feeding'
                                OR pregnant = 'YES' AND encounter_date <= CURRENT_DATE()
                                 AND encounter_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 24 MONTH)
                             GROUP BY client_id) pmtct_enrollment ON pmtct_enrollment.client_id = person_a
                  LEFT JOIN (SELECT client_id, status FROM mamba_fact_patients_latest_pregnancy_status) preg_status
                            ON preg_status.client_id = person_a
         WHERE p.gender = 'F'
           AND TIMESTAMPDIFF(MONTH, p1.birthdate, CURRENT_DATE()) <= 24
           AND r.person_b IN (SELECT DISTINCT e.patient_id
                              FROM encounter e
                                       INNER JOIN encounter_type et
                                                  ON e.encounter_type = et.encounter_type_id
                              WHERE e.voided = 0
                                AND et.uuid = '9fcfcc91-ad60-4d84-9710-11cc25258719'
                                AND encounter_datetime <= CURRENT_DATE()
                                AND encounter_datetime >= DATE_SUB(CURRENT_DATE(), INTERVAL 24 MONTH))
           AND r.person_a NOT IN (SELECT DISTINCT person_id FROM obs WHERE concept_id = 99165 AND voided = 0)
           AND r.person_b NOT IN (SELECT DISTINCT person_id FROM obs WHERE concept_id = 99165 AND voided = 0)

         UNION
# mothers without babies
         SELECT DISTINCT mfehac.client_id AS patient,
                         NULL             AS babies,
                         pmtct_enrollment_date,
                         'Pregnant'       AS pmtct
         FROM (SELECT client_id
               FROM mamba_fact_encounter_hiv_art_card
               WHERE pregnant = 'YES'
                 AND encounter_date <= CURRENT_DATE()
                 AND encounter_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR)
               GROUP BY client_id) mfehac
                  LEFT JOIN (SELECT person_a AS patient
                             FROM relationship r
                                      INNER JOIN person p ON r.person_a = p.person_id
                                      INNER JOIN person p1 ON r.person_b = p1.person_id
                                      INNER JOIN relationship_type rt
                                                 ON r.relationship = rt.relationship_type_id AND
                                                    rt.uuid = '8d91a210-c2cc-11de-8d13-0010c6dffd0f'
                                      LEFT JOIN (SELECT client_id, MIN(encounter_date) pmtct_enrollment_date
                                                 FROM mamba_fact_encounter_hiv_art_card
                                                 WHERE pregnant = 'Breast feeding'
                                                    OR pregnant = 'YES' AND encounter_date <= CURRENT_DATE()
                                                     AND encounter_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 24 MONTH)
                                                 GROUP BY client_id) pmtct_enrollment
                                                ON pmtct_enrollment.client_id = person_a
                                      LEFT JOIN (SELECT client_id, status
                                                 FROM mamba_fact_patients_latest_pregnancy_status) preg_status
                                                ON preg_status.client_id = person_a
                             WHERE p.gender = 'F'
                               AND TIMESTAMPDIFF(MONTH, p1.birthdate, CURRENT_DATE()) <= 24
                               AND r.person_b IN (SELECT DISTINCT e.patient_id
                                                  FROM encounter e
                                                           INNER JOIN encounter_type et
                                                                      ON e.encounter_type = et.encounter_type_id
                                                  WHERE e.voided = 0
                                                    AND et.uuid = '9fcfcc91-ad60-4d84-9710-11cc25258719'
                                                    AND encounter_datetime <= CURRENT_DATE()
                                                    AND encounter_datetime >= DATE_SUB(CURRENT_DATE(), INTERVAL 24 MONTH))) alreadymothers
                            ON mfehac.client_id = alreadymothers.patient
                  LEFT JOIN (SELECT client_id, MIN(encounter_date) pmtct_enrollment_date
                             FROM mamba_fact_encounter_hiv_art_card
                             WHERE pregnant = 'YES'
                               AND encounter_date <= CURRENT_DATE()
                               AND encounter_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 12 MONTH)
                             GROUP BY client_id) pmtct_enrollment ON mfehac.client_id = pmtct_enrollment.client_id
         WHERE alreadymothers.patient IS NULL

         UNION
# babies without parents in emr
         SELECT NULL AS patient, e.patient_id AS babies, NULL AS pmtct_enrollment_date, 'HEI with caregiver' AS pmtct
         FROM encounter e
                  INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id
                  INNER JOIN person p ON e.patient_id = p.person_id
         WHERE e.voided = 0
           AND et.uuid = '9fcfcc91-ad60-4d84-9710-11cc25258719'
           AND encounter_datetime <= CURRENT_DATE()
           AND encounter_datetime >= DATE_SUB(CURRENT_DATE(), INTERVAL 24 MONTH)
           AND patient_id NOT IN (SELECT person_b AS parent
                                  FROM relationship r
                                           INNER JOIN relationship_type rt
                                                      ON r.relationship = rt.relationship_type_id AND
                                                         rt.uuid = '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
           AND TIMESTAMPDIFF(MONTH, p.birthdate, CURRENT_DATE()) <= 24) cohort
         LEFT JOIN (SELECT person_id, MAX(DATE(value_datetime)) AS edd_date
                    FROM obs
                    WHERE concept_id = 5596
                      AND voided = 0
                      AND obs_datetime >= DATE_SUB(CURRENT_DATE(), INTERVAL 16 MONTH)
                      AND obs_datetime <= CURRENT_DATE()
                    GROUP BY person_id) edd ON patient = edd.person_id
         LEFT JOIN (SELECT o.person_id, DATE(value_datetime) mydate
    FROM obs o
                             INNER JOIN (SELECT person_id, MAX(obs_datetime) latest_date
                                         FROM obs
                                         WHERE concept_id = 99771
                                           AND obs.voided = 0
                                         GROUP BY person_id) a
ON o.person_id = a.person_id
WHERE o.concept_id = 99771
  AND obs_datetime = a.latest_date
  AND o.voided = 0
GROUP BY o.person_id) nvp
ON babies = nvp.person_id
    LEFT JOIN (SELECT patient_id, pi.identifier AS id
    FROM patient_identifier pi
    INNER JOIN patient_identifier_type pit
    ON pi.identifier_type = pit.patient_identifier_type_id AND
    pit.uuid = '2c5b695d-4bf3-452f-8a7c-fe3ee3432ffe') eidno
    ON babies = eidno.patient_id
    LEFT JOIN (SELECT person_id, p.birthdate AS dob FROM person p) eiddob ON babies = eiddob.person_id
    LEFT JOIN (SELECT o.person_id, value_numeric
    FROM obs o
    INNER JOIN (SELECT person_id, MAX(obs_datetime) latest_date
    FROM obs
    WHERE concept_id = 5089
    AND obs.voided = 0
    GROUP BY person_id) a
    ON o.person_id = a.person_id
    LEFT JOIN concept_name cn
    ON value_coded = cn.concept_id AND cn.concept_name_type = 'FULLY_SPECIFIED' AND
    cn.locale = 'en'
    WHERE o.concept_id = 5089
    AND obs_datetime = a.latest_date
    AND o.voided = 0
    GROUP BY o.person_id) eid_w ON babies = eid_w.person_id
    LEFT JOIN (SELECT o.person_id, value_datetime
    FROM obs o
    INNER JOIN (SELECT person_id, MAX(obs_datetime) latest_date
    FROM obs
    WHERE concept_id = 5096
    AND obs.voided = 0
    GROUP BY person_id) a ON o.person_id = a.person_id
    LEFT JOIN concept_name cn
    ON value_coded = cn.concept_id AND cn.concept_name_type = 'FULLY_SPECIFIED' AND
    cn.locale = 'en'
    WHERE o.concept_id = 5096
    AND obs_datetime = a.latest_date
    AND o.voided = 0
    GROUP BY person_id) eid_next_appt ON babies = eid_next_appt.person_id
    LEFT JOIN (SELECT o.person_id, cn.name
    FROM obs o
    INNER JOIN (SELECT person_id, MAX(obs_datetime) latest_date
    FROM obs
    WHERE concept_id = 99451
    AND obs.voided = 0
    GROUP BY person_id) a ON o.person_id = a.person_id
    LEFT JOIN concept_name cn
    ON value_coded = cn.concept_id AND cn.concept_name_type = 'FULLY_SPECIFIED' AND
    cn.locale = 'en'
    WHERE o.concept_id = 99451
    AND obs_datetime = a.latest_date
    AND o.voided = 0
    GROUP BY o.person_id) eid_feeding ON babies = eid_feeding.person_id
    LEFT JOIN (SELECT o.person_id, DATE (value_datetime) mydate
    FROM obs o
    INNER JOIN (SELECT person_id, MAX(obs_datetime) latest_date
    FROM obs
    WHERE concept_id = 99773
    AND obs.voided = 0
    GROUP BY person_id) a ON o.person_id = a.person_id
    LEFT JOIN concept_name cn
    ON value_coded = cn.concept_id AND cn.concept_name_type = 'FULLY_SPECIFIED' AND
    cn.locale = 'en'
    WHERE o.concept_id = 99773
    AND obs_datetime = a.latest_date
    AND o.voided = 0
    GROUP BY o.person_id) ctx ON babies = ctx.person_id
    LEFT JOIN (SELECT o.person_id, DATE (value_datetime) mydate
    FROM obs o
    INNER JOIN (SELECT person_id, MAX(obs_datetime) latest_date
    FROM obs
    WHERE concept_id = 99606
    AND obs.voided = 0
    GROUP BY person_id) a ON o.person_id = a.person_id
    WHERE o.concept_id = 99606
    AND obs_datetime = a.latest_date
    AND o.voided = 0
    GROUP BY o.person_id) 1stpcr ON babies = 1stpcr.person_id
    LEFT JOIN (SELECT o.person_id, cn.name
    FROM obs o
    INNER JOIN (SELECT person_id, MAX(obs_datetime) latest_date
    FROM obs
    WHERE concept_id = 99435
    AND obs.voided = 0
    GROUP BY person_id) a ON o.person_id = a.person_id
    LEFT JOIN concept_name cn
    ON value_coded = cn.concept_id AND cn.concept_name_type = 'FULLY_SPECIFIED' AND
    cn.locale = 'en'
    WHERE o.concept_id = 99435
    AND obs_datetime = a.latest_date
    AND o.voided = 0
    GROUP BY o.person_id) 1stpcrresult ON babies = 1stpcrresult.person_id
    LEFT JOIN (SELECT o.person_id, DATE (value_datetime) mydate
    FROM obs o
    INNER JOIN (SELECT person_id, MAX(obs_datetime) latest_date
    FROM obs
    WHERE concept_id = 99438
    AND obs.voided = 0
    GROUP BY person_id) a
    ON o.person_id = a.person_id
    WHERE o.concept_id = 99438
    AND obs_datetime = a.latest_date
    AND o.voided = 0
    GROUP BY o.person_id) 1stpcrreceived ON babies = 1stpcrreceived.person_id
    LEFT JOIN (SELECT o.person_id, DATE (value_datetime) mydate
    FROM obs o
    INNER JOIN (SELECT person_id, MAX(obs_datetime) latest_date
    FROM obs
    WHERE concept_id = 99436
    AND obs.voided = 0
    GROUP BY person_id) a
    ON o.person_id = a.person_id
    WHERE o.concept_id = 99436
    AND obs_datetime = a.latest_date
    AND o.voided = 0
    GROUP BY person_id) 2ndpcr ON babies = 2ndpcr.person_id
    LEFT JOIN (SELECT o.person_id, cn.name
    FROM obs o
    INNER JOIN (SELECT person_id, MAX(obs_datetime) latest_date
    FROM obs
    WHERE concept_id = 99440
    AND obs.voided = 0
    GROUP BY person_id) a ON o.person_id = a.person_id
    LEFT JOIN concept_name cn
    ON value_coded = cn.concept_id AND cn.concept_name_type = 'FULLY_SPECIFIED' AND
    cn.locale = 'en'
    WHERE o.concept_id = 99440
    AND obs_datetime = a.latest_date
    AND o.voided = 0
    GROUP BY person_id) 2ndpcrresult ON babies = 2ndpcrresult.person_id
    LEFT JOIN (SELECT o.person_id, DATE (value_datetime) mydate
    FROM obs o
    INNER JOIN (SELECT person_id, MAX(obs_datetime) latest_date
    FROM obs
    WHERE concept_id = 99442
    AND obs.voided = 0
    GROUP BY person_id) a
    ON o.person_id = a.person_id
    WHERE o.concept_id = 99442
    AND obs_datetime = a.latest_date
    AND o.voided = 0
    GROUP BY person_id) 2ndpcrreceived ON babies = 2ndpcrreceived.person_id
    LEFT JOIN (SELECT o.person_id, DATE (value_datetime) mydate
    FROM obs o
    INNER JOIN (SELECT person_id, MAX(obs_datetime) latest_date
    FROM obs
    WHERE concept_id = 165405
    AND obs.voided = 0
    GROUP BY person_id) a
    ON o.person_id = a.person_id
    WHERE o.concept_id = 165405
    AND obs_datetime = a.latest_date
    AND o.voided = 0
    GROUP BY person_id) repeatpcr ON babies = repeatpcr.person_id
    LEFT JOIN (SELECT o.person_id, cn.name
    FROM obs o
    INNER JOIN (SELECT person_id, MAX(obs_datetime) latest_date
    FROM obs
    WHERE concept_id = 165406
    AND obs.voided = 0
    GROUP BY person_id) a
    ON o.person_id = a.person_id
    LEFT JOIN concept_name cn
    ON value_coded = cn.concept_id AND cn.concept_name_type = 'FULLY_SPECIFIED' AND
    cn.locale = 'en'
    WHERE o.concept_id = 165406
    AND obs_datetime = a.latest_date
    AND o.voided = 0
    GROUP BY person_id) repeatpcrresult ON babies = repeatpcrresult.person_id
    LEFT JOIN (SELECT o.person_id, DATE (value_datetime) mydate
    FROM obs o
    INNER JOIN (SELECT person_id, MAX(obs_datetime) latest_date
    FROM obs
    WHERE concept_id = 165408
    AND obs.voided = 0
    GROUP BY person_id) a
    ON o.person_id = a.person_id
    WHERE o.concept_id = 165408
    AND obs_datetime = a.latest_date
    AND o.voided = 0
    GROUP BY a.person_id) repeatpcrreceived ON babies = repeatpcrreceived.person_id
    LEFT JOIN (SELECT o.person_id, DATE (value_datetime) mydate
    FROM obs o
    INNER JOIN (SELECT person_id, MAX(obs_datetime) latest_date
    FROM obs
    WHERE concept_id = 162879
    AND obs.voided = 0
    GROUP BY person_id) a
    ON o.person_id = a.person_id
    WHERE o.concept_id = 162879
    AND obs_datetime = a.latest_date
    AND o.voided = 0
    GROUP BY a.person_id) rapidtest ON babies = rapidtest.person_id
    LEFT JOIN (SELECT o.person_id, cn.name
    FROM obs o
    INNER JOIN (SELECT person_id, MAX(obs_datetime) latest_date
    FROM obs
    WHERE concept_id = 162880
    AND obs.voided = 0
    GROUP BY person_id) a
    ON o.person_id = a.person_id
    LEFT JOIN concept_name cn
    ON value_coded = cn.concept_id AND cn.concept_name_type = 'FULLY_SPECIFIED' AND
    cn.locale = 'en'
    WHERE o.concept_id = 162880
    AND obs_datetime = a.latest_date
    AND o.voided = 0
    GROUP BY o.person_id) rapidtestresult ON babies = rapidtestresult.person_id
    LEFT JOIN (SELECT o.person_id, cn.name
    FROM obs o
    INNER JOIN (SELECT person_id, MAX(obs_datetime) latest_date
    FROM obs
    WHERE concept_id = 99797
    AND obs.voided = 0
    GROUP BY person_id) a ON o.person_id = a.person_id
    LEFT JOIN concept_name cn
    ON value_coded = cn.concept_id AND cn.concept_name_type = 'FULLY_SPECIFIED' AND
    cn.locale = 'en'
    WHERE o.concept_id = 99797
    AND obs_datetime = a.latest_date
    AND o.voided = 0
    GROUP BY o.person_id) finaloutcome ON babies = finaloutcome.person_id
    LEFT JOIN (SELECT o.person_id, value_text
    FROM obs o
    INNER JOIN (SELECT person_id, MAX(obs_datetime) latest_date
    FROM obs
    WHERE concept_id = 99751
    AND obs.voided = 0
    GROUP BY person_id) a
    ON o.person_id = a.person_id
    WHERE o.concept_id = 99751
    AND obs_datetime = a.latest_date
    AND o.voided = 0
    GROUP BY o.person_id) linkageno ON babies = linkageno.person_id
    LEFT JOIN (SELECT person_id, MIN(obs_datetime) latest_date
    FROM obs
    WHERE concept_id = 99451
    AND value_coded = 99793
    AND obs.voided = 0
    GROUP BY person_id) stopped_bf ON babies = stopped_bf.person_id;
-- $END