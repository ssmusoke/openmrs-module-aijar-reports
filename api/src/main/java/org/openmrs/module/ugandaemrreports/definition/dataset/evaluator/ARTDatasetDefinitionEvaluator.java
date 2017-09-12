package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import org.joda.time.LocalDate;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.ArtPatientData;
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.ARTDatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 */
@Handler(supports = {ARTDatasetDefinition.class})
public class ARTDatasetDefinitionEvaluator implements DataSetEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        ARTDatasetDefinition definition = (ARTDatasetDefinition) dataSetDefinition;

        String date = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");

        LocalDate workingDate = StubDate.dateOf(date);

        int beginningMonth = workingDate.getMonthOfYear();
        int beginningYear = workingDate.getYear();

        context = ObjectUtil.nvl(context, new EvaluationContext());

        String sql = "SELECT\n" +
                "  A.value_datetime                                                                                      AS art_start,\n" +
                "  A.patient_id                                                                                          AS unique_id,\n" +
                "  o1.value_datetime                                                                                     AS ti_date,\n" +
                "  pi.identifier,\n" +
                "  pn.family_name,\n" +
                "  pn.given_name,\n" +
                "  p.gender,\n" +
                "  if((year(a.value_datetime) - year(p.birthdate) - (right(a.value_datetime, 5) < right(p.birthdate, 5))) <= 2,\n" +
                "     TIMESTAMPDIFF(MONTH, p.birthdate, A.value_datetime),\n" +
                "     year(a.value_datetime) - year(p.birthdate) - (right(a.value_datetime, 5) < right(p.birthdate, 5))) AS age,\n" +
                "  pa.county_district                                                                                    AS district,\n" +
                "  pa.address3                                                                                           AS sub_county,\n" +
                "  pa.address4                                                                                           AS parish,\n" +
                "  pa.address5                                                                                           AS village,\n" +
                "  YEAR(\n" +
                "      p.death_date)                                                                                     AS death_year,\n" +
                "  MONTH(\n" +
                "      p.death_date)                                                                                     AS death_month\n" +
                "\n" +
                "FROM\n" +
                "  (SELECT\n" +
                "     e.patient_id,\n" +
                "     e.encounter_datetime,\n" +
                "     o.value_datetime\n" +
                "   FROM encounter e INNER JOIN obs o\n" +
                String.format("       ON (o.person_id = e.patient_id AND o.concept_id = 99161 AND YEAR(o.value_datetime) = %s AND\n", beginningYear) +
                String.format("           MONTH(o.value_datetime) = %s AND e.encounter_type = 14)) A INNER JOIN person p\n", beginningMonth) +
                "    ON (p.person_id = A.patient_id)\n" +
                "  LEFT JOIN person_name pn ON (p.person_id = pn.person_id)\n" +
                "  LEFT JOIN person_address pa ON (p.person_id = pa.person_id)\n" +
                "  LEFT JOIN patient_identifier pi ON (p.person_id = pi.patient_id AND pi.identifier_type = 4)\n" +
                "  LEFT JOIN obs o1 ON (o1.person_id = p.person_id AND o1.concept_id = 99160)";

        String followupSql = "SELECT B.*\n" +
                "FROM\n" +
                "  (SELECT\n" +
                "     e.patient_id,\n" +
                "     e.encounter_datetime,\n" +
                "     o.value_datetime\n" +
                "   FROM encounter e INNER JOIN obs o\n" +
                "       ON (o.person_id = e.patient_id AND o.concept_id = 99161 AND YEAR(o.value_datetime) = 2014 AND\n" +
                "           MONTH(o.value_datetime) = 2 AND e.encounter_type = (SELECT et.encounter_type_id\n" +
                "                                                               FROM encounter_type et\n" +
                "                                                               WHERE\n" +
                "                                                                 et.uuid = '8d5b27bc-c2cc-11de-8d13-0010c6dffd0f'))) A\n" +
                "  INNER JOIN\n" +
                "  (SELECT\n" +
                "     e.encounter_id,\n" +
                "     o.person_id,\n" +
                "     o.concept_id,\n" +
                "     o.value_coded,\n" +
                "     DATE(e.encounter_datetime)    AS enc_date,\n" +
                "     DATE(o.value_datetime)        AS dt_date,\n" +
                "     o.value_numeric,\n" +
                "     o.value_text,\n" +
                "     YEAR(e.encounter_datetime)    AS enc_year,\n" +
                "     YEAR(o.value_datetime)        AS dt_year,\n" +
                "     MONTH(e.encounter_datetime)   AS enc_month,\n" +
                "     MONTH(o.value_datetime)       AS dt_month\n" +
                "   FROM obs o INNER JOIN encounter e ON (o.person_id = e.patient_id AND o.encounter_id = e.encounter_id AND\n" +
                "                                         e.encounter_type = (SELECT et.encounter_type_id\n" +
                "                                                             FROM encounter_type et\n" +
                "                                                             WHERE\n" +
                "                                                               et.uuid = '8d5b2be0-c2cc-11de-8d13-0010c6dffd0f'))) B\n" +
                "    ON (A.patient_id = B.person_id AND B.enc_date >= A.value_datetime)";

        SqlQueryBuilder q = new SqlQueryBuilder(sql);

        SqlQueryBuilder followupQuery = new SqlQueryBuilder(followupSql);


        List<Object[]> results = evaluationService.evaluateToList(q, context);

        List<Object[]> followupResults = evaluationService.evaluateToList(followupQuery, context);

        PatientDataHelper pdh = new PatientDataHelper();

        for (Object[] r : results) {
            DataSetRow row = new DataSetRow();

            String name = new StringBuilder()
                    .append(r[4] + "\n")
                    .append(r[5])
                    .toString();

            String address = new StringBuilder()
                    .append(r[8] + "\n")
                    .append(r[9] + "\n")
                    .append(r[10] + "\n")
                    .append(r[11] + "\n")
                    .toString();

            pdh.addCol(row, "Date ART Started", r[0]);
            pdh.addCol(row, "Unique ID no", r[1]);
            pdh.addCol(row, "Patient Clinic ID", r[3]);
            pdh.addCol(row, "TI", r[2]);
            pdh.addCol(row, "Name", name);
            pdh.addCol(row, "Gender", r[6]);
            pdh.addCol(row, "Age", r[7]);
            pdh.addCol(row, "Address", address);

            for (int i = 0; i <= 72; i++) {
                String key = String.valueOf(i);
                LocalDate currentDate = workingDate.plusMonths(i);

                int month = currentDate.getMonthOfYear();
                int year = currentDate.getYear();

                ArtPatientData artPatientData = convertToPatientData(followupResults, String.valueOf(r[1]), "90315", String.valueOf(year), String.valueOf(month));

                if (artPatientData != null) {
                    pdh.addCol(row, "FUS" + key, artPatientData.getValueCoded());
                } else {
                    pdh.addCol(row, "FUS" + key, "");
                }

            }

            dataSet.addRow(row);
        }
        return dataSet;
    }

    private ArtPatientData convertToPatientData(List<Object[]> data, String patientId, String concept, String year, String month) {

        for (Object[] r : data) {

            String encounterIdString = (String.valueOf(r[0])).equalsIgnoreCase("null") ? "" : String.valueOf(r[0]);
            String patientIdString = (String.valueOf(r[1])).equalsIgnoreCase("null") ? "" : String.valueOf(r[1]);
            String conceptIdString = (String.valueOf(r[2])).equalsIgnoreCase("null") ? "" : String.valueOf(r[2]);
            String valueCodedString = (String.valueOf(r[3])).equalsIgnoreCase("null") ? "" : String.valueOf(r[3]);
            String encounterDateString = (String.valueOf(r[4])).equalsIgnoreCase("null") ? "" : String.valueOf(r[4]);
            String valueDatetimeString = (String.valueOf(r[5])).equalsIgnoreCase("null") ? "" : String.valueOf(r[5]);
            String valueNumericString = (String.valueOf(r[6])).equalsIgnoreCase("null") ? "" : String.valueOf(r[6]);
            String valueTextString = (String.valueOf(r[7])).equalsIgnoreCase("null") ? "" : String.valueOf(r[7]);
            String encounterYearString = (String.valueOf(r[8])).equalsIgnoreCase("null") ? "" : String.valueOf(r[8]);
            String valueDatetimeYearString = (String.valueOf(r[9])).equalsIgnoreCase("null") ? "" : String.valueOf(r[9]);
            String encounterMonthString = (String.valueOf(r[10])).equalsIgnoreCase("null") ? "" : String.valueOf(r[10]);
            String valueDatetimeMonthString = (String.valueOf(r[11])).equalsIgnoreCase("null") ? "" : String.valueOf(r[11]);


            System.out.println(encounterIdString + "," + patientIdString + "," + conceptIdString + "," + valueCodedString + "," + encounterDateString + "," + valueDatetimeString + "," + valueNumericString + "," + valueTextString + "," + encounterYearString + "," + valueDatetimeYearString + "," + encounterMonthString + "," + valueDatetimeMonthString);

            /*Integer encounterId = Integer.valueOf(String.valueOf(r[0]));
            Integer currentPatientId = Integer.valueOf(String.valueOf(r[1]));
            Integer conceptId = Integer.valueOf(String.valueOf(r[2]));
            Integer valueCoded = Strings.isNullOrEmpty(String.valueOf(r[3])) || String.valueOf(r[3]).equalsIgnoreCase("null") ? null : Integer.valueOf(String.valueOf(r[3]));
            Date encounterDate = (Date) r[4];
            Date valueDatetime = (Date) r[5];
            Double valueNumeric = Strings.isNullOrEmpty(String.valueOf(r[6])) || String.valueOf(r[6]).equalsIgnoreCase("null") ? null : Double.valueOf(String.valueOf(r[6]));
            String valueText = String.valueOf(r[7]);
            Integer encounterYear = Integer.valueOf(String.valueOf(r[8]));
            Integer valueDatetimeYear = Strings.isNullOrEmpty(String.valueOf(r[9])) || String.valueOf(r[9]).equalsIgnoreCase("null") ? null : Integer.valueOf(String.valueOf(r[9]));
            Integer encounterMonth = Integer.valueOf(String.valueOf(r[10]));
            Integer valueDatetimeMonth = Strings.isNullOrEmpty(String.valueOf(r[11])) || String.valueOf(r[11]).equalsIgnoreCase("null") ? null : Integer.valueOf(String.valueOf(r[11]));*/

            if (patientId.equals(patientIdString) && concept.equals(conceptIdString) && encounterYearString.equalsIgnoreCase(year) && encounterMonthString.equalsIgnoreCase(month)) {
                return new ArtPatientData(encounterIdString, patientIdString, conceptIdString, valueCodedString, encounterDateString, valueDatetimeString, valueNumericString, valueTextString, encounterYearString, valueDatetimeYearString, encounterMonthString, valueDatetimeMonthString);
            }

        }
        return new ArtPatientData();
    }
}
