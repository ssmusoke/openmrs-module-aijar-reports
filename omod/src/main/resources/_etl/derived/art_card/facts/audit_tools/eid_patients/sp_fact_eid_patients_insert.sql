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
    NVP_AT_BIRTH
)
SELECT patient,
                                     IFNULL(EDD.edd_date,''),
                                     IFNULL(EIDNO.id,'') as EIDNO,
                                     IFNULL(EIDDOB.dob,'') as EID_DOB,
                                     IFNULL(TIMESTAMPDIFF(MONTH , EIDDOB.dob, CURRENT_DATE()),'') as EID_age,
                                     IFNULL(EID_W.value_numeric,'') as EID_Weight,
                                     IFNULL(EID_NEXT_APPT.value_datetime,'')AS NEXT_APPOINTMENT_DATE,
                                     IFNULL(EID_FEEDING.name,'') as Feeding,
                                     IFNULL(CTX.mydate,'') as CTX_START,
                                     IFNULL(TIMESTAMPDIFF(MONTH, CTX.mydate, CURRENT_DATE()),'') as agectx,
                                     IFNULL(1stPCR.mydate,'') as 1stPCRDATE,
                                     IFNULL(TIMESTAMPDIFF(MONTH, 1stPCR.mydate, CURRENT_DATE()),'') as age1stPCR,
                                     IFNULL(1stPCRResult.name,''),
                                     IFNULL(1stPCRReceived.mydate,'') as 1stPCRRecieved,
                                     IFNULL(2ndPCR.mydate,'') as 2ndPCRDATE,
                                     IFNULL(TIMESTAMPDIFF(MONTH, 2ndPCR.mydate, CURRENT_DATE()),'') as age2ndPCR,
                                     IFNULL(2ndPCRResult.name,''),
                                     IFNULL(2ndPCRReceived.mydate,'') as 2ndPCRRecieved,
                                     IFNULL(repeatPCR.mydate,'') as repeatPCRDATE,
                                     IFNULL(TIMESTAMPDIFF(MONTH, repeatPCR.mydate, CURRENT_DATE()),'') as age3rdPCR,
                                     IFNULL(repeatPCRResult.name,''),
                                     IFNULL(repeatPCRReceived.mydate,'') as repeatPCRRecieved,
                                     IFNULL(rapidTest.mydate,'') as rapidTestDate,
                                     IFNULL(TIMESTAMPDIFF(MONTH, rapidTest.mydate, CURRENT_DATE()),'') as ageatRapidTest,
                                     IFNULL(rapidTestResult.name,''),
                                     IFNULL(finalOutcome.name,''),
                                     IFNULL(linkageNo.value_text,''),
                                     IF(NVP.mydate IS NULL,'', IF(TIMESTAMPDIFF(DAY , NVP.mydate, CURRENT_DATE())<=2,'Y','N')) as NVP

    FROM  ( select DISTINCT o.person_id as patient from obs o WHERE o.voided = 0 and concept_id=90041 and value_coded in (1065,99601) and obs_datetime<= CURRENT_DATE() and obs_datetime>= DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) union
                SELECT person_a as patient from relationship r inner join person p on r.person_a = p.person_id inner join relationship_type rt on r.relationship = rt.relationship_type_id and rt.uuid='8d91a210-c2cc-11de-8d13-0010c6dffd0f' where p.gender='F' and r.person_b in (SELECT DISTINCT e.patient_id from encounter e INNER JOIN encounter_type et
                ON e.encounter_type = et.encounter_type_id WHERE e.voided = 0 and et.uuid in('9fcfcc91-ad60-4d84-9710-11cc25258719','4345dacb-909d-429c-99aa-045f2db77e2b') and encounter_datetime<= CURRENT_DATE() and encounter_datetime>= DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR))) cohort join
        person p on p.person_id = cohort.patient
        LEFT JOIN (SELECT person_id, max(DATE (value_datetime))as edd_date FROM obs WHERE concept_id=5596 and voided=0 and  obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_id)EDD on patient=EDD.person_id
        LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)   where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')and concept_id=99771 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
        on o.person_id = A.person_b where o.concept_id=99771 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) NVP on patient = NVP.parent
        LEFT JOIN (SELECT person_a as parent,pi.identifier as id  from relationship left join patient_identifier pi on person_b = patient_id inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR) INNER JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id and pit.uuid='2c5b695d-4bf3-452f-8a7c-fe3ee3432ffe'  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f') and pi.voided=0) EIDNO on patient = EIDNO.parent
        LEFT JOIN (SELECT person_a as parent,p.birthdate as dob  from relationship inner join person p on person_b = p.person_id and p.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR) where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f') ) EIDDOB on patient = EIDDOB.parent
        LEFT JOIN (SELECT parent,value_numeric  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)   where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=5089 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
        on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=5089 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) EID_W on patient = EID_W.parent
        LEFT JOIN (SELECT parent,value_datetime from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)   where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=5096 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A       on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=5096 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) EID_NEXT_APPT on patient = EID_NEXT_APPT.parent
        LEFT JOIN (SELECT parent,cn.name from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)   where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=99451 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A       on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99451 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) EID_FEEDING on patient = EID_FEEDING.parent
        LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=99773 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A       on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99773 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) CTX on patient = CTX.parent
        LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)   where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=99606 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A       on o.person_id = A.person_b where o.concept_id=99606 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) 1stPCR on patient = 1stPCR.parent
        LEFT JOIN (SELECT parent,cn.name  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)   where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=99435 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A       on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99435 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) 1stPCRResult on patient = 1stPCRResult.parent
        LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=99438 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b where o.concept_id=99438 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) 1stPCRReceived on patient = 1stPCRReceived.parent
        LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=99436 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b where o.concept_id=99436 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) 2ndPCR on patient = 2ndPCR.parent
        LEFT JOIN (SELECT parent,cn.name  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=99440 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99440 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) 2ndPCRResult on patient = 2ndPCRResult.parent
        LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=99442 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b where o.concept_id=99442 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) 2ndPCRReceived on patient = 2ndPCRReceived.parent
        LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=165405 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b where o.concept_id=165405 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) repeatPCR on patient = repeatPCR.parent
        LEFT JOIN (SELECT parent,cn.name  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=165406 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=165406 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) repeatPCRResult on patient = repeatPCRResult.parent
        LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=165408 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b where o.concept_id=165408 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) repeatPCRReceived on patient = repeatPCRReceived.parent
        LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=162879 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b where o.concept_id=162879 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) rapidTest on patient = rapidTest.parent
        LEFT JOIN (SELECT parent,cn.name  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=162880 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=162880 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) rapidTestResult on patient = rapidTestResult.parent
        LEFT JOIN (SELECT parent,cn.name  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=99797 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99797 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) finalOutcome on patient = finalOutcome.parent
        LEFT JOIN (SELECT parent,value_text  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
           and concept_id=99751 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b where o.concept_id=99751 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) linkageNo on patient = linkageNo.parent;

-- $END