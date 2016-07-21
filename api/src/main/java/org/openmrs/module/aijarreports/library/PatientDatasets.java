package org.openmrs.module.aijarreports.library;

import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;

import java.util.Date;

/**
 * Created by carapai on 12/07/2016.
 */
public class PatientDatasets {

    public static SqlPatientDataDefinition getPatientsWhoEnrolledInCareInYear(Date startDate, Date endDate) {

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append("    enc1.patient_id, enc2.enc_date ");
        sb.append("FROM ");
        sb.append("    (SELECT ");
        sb.append("        e1.patient_id ");
        sb.append("    FROM ");
        sb.append("        encounter e1 ");
        sb.append("    GROUP BY e1.patient_id) enc1");
        sb.append("        LEFT JOIN");
        sb.append("    (SELECT ");
        sb.append("        e2.patient_id, MAX(e2.encounter_datetime) as 'enc_date'");
        sb.append("    FROM");
        sb.append("        encounter e2 ");
        sb.append("    WHERE");
        sb.append("        e2.encounter_datetime BETWEEN '" + DateUtil.formatDate(startDate, "yyyy-MM-dd") + "' AND '" + DateUtil.formatDate(endDate, "yyyy-MM-dd") + "' group by e2.patient_id) enc2 ON (enc1.patient_id = enc2.patient_id)");

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();

        sqlPatientDataDefinition.setSql(sb.toString());

        return sqlPatientDataDefinition;
    }

    public static SqlPatientDataDefinition getFUStatus(Date startDate, Date endDate) {

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT \n");
        sb.append("    enc1.patient_id,\n");
        sb.append("    CONCAT_WS(',', COALESCE(DATE(enc2.enc_date), '-'), COALESCE(ob.visit, '-'),COALESCE(DATE(per.death_date), '-'),COALESCE(ob1.value_coded, '-'),COALESCE(ob2.next_visit, '-'))\n");
        sb.append("FROM\n");
        sb.append("    (SELECT \n");
        sb.append("        e1.patient_id\n");
        sb.append("    FROM\n");
        sb.append("        encounter e1\n");
        sb.append("    GROUP BY e1.patient_id) enc1\n");
        sb.append("        LEFT JOIN\n");
        sb.append("    (SELECT \n");
        sb.append("        e2.patient_id, MAX(e2.encounter_datetime) AS 'enc_date'\n");
        sb.append("    FROM\n");
        sb.append("        encounter e2\n");
        sb.append("    WHERE\n");
        sb.append("        e2.encounter_datetime BETWEEN '" + DateUtil.formatDate(startDate, "yyyy-MM-dd") + "' AND '" + DateUtil.formatDate(endDate, "yyyy-MM-dd") + "'\n");
        sb.append("    GROUP BY e2.patient_id) enc2 ON (enc1.patient_id = enc2.patient_id)\n");
        sb.append("        LEFT JOIN\n");
        sb.append("    (SELECT \n");
        sb.append("        o.person_id,\n");
        sb.append("            DATEDIFF('" + DateUtil.formatDate(endDate, "yyyy-MM-dd") + "', MAX(o.value_datetime)) AS 'visit'\n");
        sb.append("    FROM\n");
        sb.append("        obs o\n");
        sb.append("    WHERE\n");
        sb.append("        o.concept_id = 5096\n");
        sb.append("            AND o.value_datetime <= '" + DateUtil.formatDate(endDate, "yyyy-MM-dd") + "'\n");
        sb.append("    GROUP BY o.person_id) ob ON (enc1.patient_id = ob.person_id)\n");
        sb.append("        LEFT JOIN\n");
        sb.append("    (SELECT \n");
        sb.append("        p.person_id, p.death_date AS 'death_date'\n");
        sb.append("    FROM\n");
        sb.append("        person p\n");
        sb.append("    WHERE\n");
        sb.append("        p.death_date BETWEEN '" + DateUtil.formatDate(startDate, "yyyy-MM-dd") + "' AND '" + DateUtil.formatDate(endDate, "yyyy-MM-dd") + "') per ON (per.person_id = enc1.patient_id)\n");
        sb.append("        LEFT JOIN\n");
        sb.append("    (SELECT \n");
        sb.append("        o2.value_coded, o2.person_id\n");
        sb.append("    FROM\n");
        sb.append("        obs o2\n");
        sb.append("    WHERE\n");
        sb.append("        concept_id = 90306\n");
        sb.append("            AND o2.obs_datetime <= '" + DateUtil.formatDate(endDate, "yyyy-MM-dd") + "') ob1 ON (ob1.person_id = enc1.patient_id)\n");
        sb.append("        LEFT OUTER JOIN\n");
        sb.append("    (SELECT \n");
        sb.append("        o3.person_id, MIN(o3.value_datetime) AS 'next_visit'\n");
        sb.append("    FROM\n");
        sb.append("        obs o3\n");
        sb.append("    WHERE\n");
        sb.append("        o3.concept_id = 5096\n");
        sb.append("            AND o3.value_datetime > '" + DateUtil.formatDate(endDate, "yyyy-MM-dd") + "'\n");
        sb.append("    GROUP BY o3.person_id) ob2 ON (ob2.person_id = enc1.patient_id);");

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();

        sqlPatientDataDefinition.setSql(sb.toString());

        return sqlPatientDataDefinition;
    }
}
