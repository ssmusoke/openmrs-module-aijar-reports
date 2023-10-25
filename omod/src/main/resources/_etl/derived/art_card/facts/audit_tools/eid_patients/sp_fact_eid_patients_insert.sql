-- $BEGIN
INSERT INTO mamba_fact_eid_patients (
    client_id,
    EDD ,
    EID_NO ,
    EID_DOB,
    EID_AGE ,
    EID_WEIGHT ,
    EID_NEXT_APPT ,
    EID_FEEDING ,
    CTX_START ,
    CTX_AGE ,
    1ST_PCR_DATE ,
    1ST_PCR_AGE ,
    1ST_PCR_RESULT ,
    1ST_PCR_RECEIVED ,
    2ND_PCR_DATE ,
    2ND_PCR_AGE ,
    2ND_PCR_RESULT ,
    2ND_PCR_RECEIVED ,
    REPEAT_PCR_DATE ,
    REPEAT_PCR_AGE ,
    REPEAT_PCR_RESULT ,
    REPEAT_PCR_RECEIVED ,
    RAPID_PCR_DATE ,
    RAPID_PCR_AGE ,
    RAPID_PCR_RESULT ,
    FINAL_OUTCOME ,
    LINKAGE_NO ,
    NVP_AT_BIRTH,
    BREAST_FEEDING_STOPPED,
    PMTCT_STATUS,
    PMTCT_ENROLLMENT_DATE,
    BABY
)
SELECT patient,
       edd.edd_date,
       eidno.id                                                                                  AS eidno,
       eiddob.dob                                                                                AS eid_dob,
       TIMESTAMPDIFF(MONTH, eiddob.dob, CURRENT_DATE())                                          AS eid_age,
       eid_w.value_numeric                                                                       AS eid_weight,
       eid_next_appt.value_datetime                                                              AS next_appointment_date,
       eid_feeding.name                                                                          AS feeding,
       ctx.mydate                                                                                AS ctx_start,
       TIMESTAMPDIFF(MONTH, eiddob.dob, ctx.mydate)                                              AS agectx,
       1stpcr.mydate                                                                             AS 1stpcrdate,
       TIMESTAMPDIFF(MONTH, eiddob.dob, 1stpcr.mydate)                                           AS age1stpcr,
       1stpcrresult.name,
       1stpcrreceived.mydate                                                                     AS 1stpcrrecieved,
       2ndpcr.mydate                                                                             AS 2ndpcrdate,
       TIMESTAMPDIFF(MONTH, eiddob.dob, 2ndpcr.mydate)                                           AS age2ndpcr,
       2ndpcrresult.name,
       2ndpcrreceived.mydate                                                                     AS 2ndpcrrecieved,
       repeatpcr.mydate                                                                          AS repeatpcrdate,
       TIMESTAMPDIFF(MONTH, eiddob.dob, repeatpcr.mydate)                                        AS age3rdpcr,
       repeatpcrresult.name,
       repeatpcrreceived.mydate                                                                  AS repeatpcrrecieved,
       rapidtest.mydate                                                                          AS rapidtestdate,
       TIMESTAMPDIFF(MONTH, eiddob.dob, rapidtest.mydate)                                        AS ageatrapidtest,
       rapidtestresult.name,
       finaloutcome.name,
       linkageno.value_text,
       IF(nvp.mydate IS NULL, '', IF(TIMESTAMPDIFF(DAY, eiddob.dob, nvp.mydate) <= 2, 'Y', 'N')) AS nvp,
       stopped_bf.latest_date                                                                    AS breast_feeding_stopped,
       cohort.PMTCT,
       pmtct_enrollment_date,
       babies                                                                                    AS baby

from (
         # mothers with babies
          SELECT person_a AS patient, person_b AS babies, pmtct_enrollment_date, preg_status.status AS pmtct
          FROM relationship r
                   INNER JOIN person p ON r.person_a = p.person_id
                   INNER JOIN relationship_type rt
                              ON r.relationship = rt.relationship_type_id AND
                                 rt.uuid = '8d91a210-c2cc-11de-8d13-0010c6dffd0f'
                   LEFT JOIN (SELECT client_id, MIN(encounter_date) pmtct_enrollment_date
                              FROM mamba_fact_encounter_hiv_art_card
                              WHERE pregnant = 'Breast feeding'
                                 OR pregnant = 'YES' AND encounter_date <= CURRENT_DATE()
                                  AND encounter_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 33 MONTH)
                              GROUP BY client_id) pmtct_enrollment ON pmtct_enrollment.client_id = person_a
                   LEFT JOIN (SELECT client_id, status FROM mamba_fact_patients_latest_pregnancy_status) preg_status
                             ON preg_status.client_id = person_a
          WHERE p.gender = 'F'
            AND r.person_b IN (SELECT DISTINCT e.patient_id
                               FROM encounter e
                                        INNER JOIN encounter_type et
                                                   ON e.encounter_type = et.encounter_type_id
                               WHERE e.voided = 0
                                 AND et.uuid = '9fcfcc91-ad60-4d84-9710-11cc25258719'
                                 AND encounter_datetime <= CURRENT_DATE()
                                 AND encounter_datetime >= DATE_SUB(CURRENT_DATE(), INTERVAL 33 MONTH))
          UNION
          # mothers without babies
          SELECT DISTINCT mfehac.client_id   AS patient,
                          NULL               AS babies,
                          pmtct_enrollment_date,
                          preg_status.status AS pmtct
          FROM mamba_fact_encounter_hiv_art_card mfehac
                   LEFT JOIN (SELECT client_id, MIN(encounter_date) pmtct_enrollment_date
                              FROM mamba_fact_encounter_hiv_art_card
                              WHERE pregnant = 'Breast feeding'
                                 OR pregnant = 'YES' AND encounter_date <= CURRENT_DATE()
                                  AND encounter_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 24 MONTH)
                              GROUP BY client_id) pmtct_enrollment ON mfehac.client_id = pmtct_enrollment.client_id
                   INNER JOIN (SELECT client_id, status
                               FROM mamba_fact_patients_latest_pregnancy_status
                               WHERE status = 'pregnant'
                                  OR status = 'Breast feeding') preg_status
                              ON mfehac.client_id = preg_status.client_id
          WHERE pregnant = 'Breast feeding'
             OR pregnant = 'YES'
              AND mfehac.encounter_date <= CURRENT_DATE()
              AND mfehac.encounter_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 12 MONTH)
          UNION
          # babies without parents in emr
          SELECT NULL AS patient, e.patient_id AS babies, NULL AS pmtct_enrollment_date, 'HIE with caregiver' AS pmtct
          FROM encounter e
                   INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id
          WHERE e.voided = 0
            AND et.uuid = '9fcfcc91-ad60-4d84-9710-11cc25258719'
            AND encounter_datetime <= CURRENT_DATE()
            AND encounter_datetime >= DATE_SUB(CURRENT_DATE(), INTERVAL 33 MONTH)
            AND patient_id NOT IN (SELECT person_b AS parent
                                   FROM relationship r
                                            INNER JOIN relationship_type rt
                                                       ON r.relationship = rt.relationship_type_id AND
                                                          rt.uuid = '8d91a210-c2cc-11de-8d13-0010c6dffd0f')) cohort
         LEFT JOIN (SELECT person_id, max(DATE (value_datetime))as edd_date FROM obs WHERE concept_id=5596 and voided=0 and obs_datetime>= DATE_SUB(CURRENT_DATE(), INTERVAL 16 MONTH) and  obs_datetime<=CURRENT_DATE()  group by person_id)EDD on patient = EDD.person_id
         LEFT JOIN (SELECT o.person_id,DATE(value_datetime) mydate  from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from  obs  where concept_id=99771 and obs.voided=0 group by person_id)A
on o.person_id = A.person_id where o.concept_id=99771 and obs_datetime =A.latest_date and o.voided=0  group by o.person_id) NVP on babies = NVP.person_id
    LEFT JOIN (SELECT patient_id ,pi.identifier as id  from  patient_identifier pi  INNER JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id and pit.uuid='2c5b695d-4bf3-452f-8a7c-fe3ee3432ffe') EIDNO on babies = EIDNO.patient_id
    LEFT JOIN (SELECT person_id,p.birthdate as dob  from person p ) EIDDOB on babies = EIDDOB.person_id
    LEFT JOIN (SELECT o.person_id,value_numeric  from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from  obs where  concept_id=5089 and obs.voided=0  group by person_id)A
    on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=5089 and obs_datetime =A.latest_date and o.voided=0  group by o.person_id) EID_W on babies = EID_W.person_id
    LEFT JOIN (SELECT o.person_id,value_datetime from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from  obs where concept_id=5096 and obs.voided=0   group by person_id)A on o.person_id = A.person_id
    LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=5096 and obs_datetime =A.latest_date and o.voided=0  group by person_id) EID_NEXT_APPT on babies = EID_NEXT_APPT.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99451 and obs.voided=0   group by person_id)A  on o.person_id = A.person_id
    LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99451 and obs_datetime =A.latest_date and o.voided=0  group by o.person_id) EID_FEEDING on babies = EID_FEEDING.person_id
    LEFT JOIN (SELECT o.person_id,DATE(value_datetime) mydate  from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from  obs where concept_id=99773 and obs.voided=0   group by person_id)A on o.person_id = A.person_id
    LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99773 and obs_datetime =A.latest_date and o.voided=0  group by o.person_id) CTX on babies = CTX.person_id
    LEFT JOIN (SELECT o.person_id,DATE(value_datetime) mydate  from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from  obs where concept_id=99606 and obs.voided=0   group by person_id)A on o.person_id = A.person_id where o.concept_id=99606 and obs_datetime =A.latest_date and o.voided=0  group by o.person_id) 1stPCR on babies = 1stPCR.person_id
    LEFT JOIN (SELECT o.person_id,cn.name  from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from  obs where concept_id=99435 and obs.voided=0   group by person_id)A       on o.person_id = A.person_id
    LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99435 and obs_datetime =A.latest_date and o.voided=0  group by o.person_id) 1stPCRResult on babies = 1stPCRResult.person_id
    LEFT JOIN (SELECT o.person_id,DATE(value_datetime) mydate  from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99438 and obs.voided=0   group by person_id)A
    on o.person_id = A.person_id where o.concept_id=99438 and obs_datetime =A.latest_date and o.voided=0  group by o.person_id) 1stPCRReceived on babies = 1stPCRReceived.person_id
    LEFT JOIN (SELECT o.person_id,DATE(value_datetime) mydate  from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from  obs  where concept_id=99436 and obs.voided=0   group by person_id)A
    on o.person_id = A.person_id where o.concept_id=99436 and obs_datetime =A.latest_date and o.voided=0  group by person_id) 2ndPCR on babies = 2ndPCR.person_id
    LEFT JOIN (SELECT o.person_id,cn.name  from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from  obs where concept_id=99440 and obs.voided=0   group by person_id)A on o.person_id = A.person_id
    LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99440 and obs_datetime =A.latest_date and o.voided=0  group by person_id) 2ndPCRResult on babies = 2ndPCRResult.person_id
    LEFT JOIN (SELECT o.person_id,DATE(value_datetime) mydate  from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99442 and obs.voided=0   group by person_id)A
    on o.person_id = A.person_id where o.concept_id=99442 and obs_datetime =A.latest_date and o.voided=0  group by person_id) 2ndPCRReceived on babies = 2ndPCRReceived.person_id
    LEFT JOIN (SELECT o.person_id,DATE(value_datetime) mydate  from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165405 and obs.voided=0   group by person_id)A
    on o.person_id = A.person_id where o.concept_id=165405 and obs_datetime =A.latest_date and o.voided=0  group by person_id) repeatPCR on babies = repeatPCR.person_id
    LEFT JOIN (SELECT o.person_id,cn.name  from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165406 and obs.voided=0   group by person_id)A
    on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=165406 and obs_datetime =A.latest_date and o.voided=0  group by person_id) repeatPCRResult on babies = repeatPCRResult.person_id
    LEFT JOIN (SELECT o.person_id,DATE(value_datetime) mydate  from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165408 and obs.voided=0   group by person_id)A
    on o.person_id = A.person_id where o.concept_id=165408 and obs_datetime =A.latest_date and o.voided=0  group by A.person_id) repeatPCRReceived on babies = repeatPCRReceived.person_id
    LEFT JOIN (SELECT o.person_id,DATE(value_datetime) mydate  from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from  obs where concept_id=162879 and obs.voided=0   group by person_id)A
    on o.person_id = A.person_id where o.concept_id=162879 and obs_datetime =A.latest_date and o.voided=0  group by A.person_id) rapidTest on babies = rapidTest.person_id
    LEFT JOIN (SELECT o.person_id,cn.name  from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=162880 and obs.voided=0   group by person_id)A
    on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=162880 and obs_datetime =A.latest_date and o.voided=0  group by o.person_id) rapidTestResult on babies = rapidTestResult.person_id
    LEFT JOIN (SELECT o.person_id,cn.name  from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99797 and obs.voided=0   group by person_id)A on o.person_id = A.person_id
    LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99797 and obs_datetime =A.latest_date and o.voided=0  group by o.person_id) finalOutcome on babies = finalOutcome.person_id
    LEFT JOIN (SELECT o.person_id,value_text  from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs  where concept_id=99751 and obs.voided=0   group by person_id)A
    on o.person_id = A.person_id where o.concept_id=99751 and obs_datetime =A.latest_date and o.voided=0  group by o.person_id) linkageNo on babies= linkageNo.person_id
    LEFT JOIN (SELECT person_id,min(obs_datetime)latest_date from obs where concept_id=99451 and value_coded=99793 and obs.voided=0  group by person_id)stopped_BF ON babies = stopped_BF.person_id;
-- $END