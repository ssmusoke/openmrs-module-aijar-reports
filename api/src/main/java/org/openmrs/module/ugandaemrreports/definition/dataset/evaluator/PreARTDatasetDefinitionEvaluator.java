package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Interval;
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
import org.openmrs.module.ugandaemrreports.common.Helper;
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.common.Periods;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.PreARTDatasetDefinition;
import org.openmrs.module.ugandaemrreports.metadata.CommonReportMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.TreeMap;

/**
 */
@Handler(supports = {PreARTDatasetDefinition.class})
public class PreARTDatasetDefinitionEvaluator implements DataSetEvaluator {
    @Autowired
    private CommonReportMetadata commonReportMetadata;

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        PreARTDatasetDefinition definition = (PreARTDatasetDefinition) dataSetDefinition;

        context = ObjectUtil.nvl(context, new EvaluationContext());

        String sql = "SELECT\n" +
                "  A.person_id,DATE(A.encounter_datetime) as enrollement_date, B.art_start_date,C.identifier,D.family_name,D.given_name,E.gender,TIMESTAMPDIFF(YEAR, E.birthdate,'" + DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd") + "') as age,F.county_district,G.entry_point,H.ti,I.cpt,J.inh,TN.tb_reg,TST.tb_start_date,TSP.tb_stop_date, K.tb_status,L.baseline_ci,M.other_ci,N.eligible,O.eligible_ci,P.eligible_cd4,Q.eligible_tb,R.eligible_bf,S.eligible_preg,T.eligible_and_ready,U.encounters\n" +
                "FROM\n" +
                "  (select e.encounter_datetime,e.patient_id  as person_id from encounter e inner join encounter_type et on(e.encounter_type = et.encounter_type_id and et.uuid = '8d5b27bc-c2cc-11de-8d13-0010c6dffd0f' and YEAR('" + DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd") + "') = YEAR(e.encounter_datetime) and e.voided = 0)) A\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,value_datetime,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) as art_start_date FROM obs WHERE concept_id = 99161 group by person_id) B\n" +
                "    ON (A.person_id = B.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT patient_id as person_id,identifier FROM patient_identifier WHERE identifier_type = 4 group by person_id) C\n" +
                "    ON (A.person_id = C.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,family_name,given_name FROM person_name group by person_id) D\n" +
                "    ON (A.person_id = D.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,gender,birthdate FROM person group by person_id) E\n" +
                "    ON (A.person_id = E.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,county_district FROM person_address group by person_id) F\n" +
                "    ON (A.person_id = F.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) as entry_point FROM obs where concept_id = 90200 group by person_id) G\n" +
                "    ON (A.person_id = G.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) as ti FROM obs where concept_id = 99110 group by person_id) H\n" +
                "    ON (A.person_id = H.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_numeric,'|',DATE(obs_datetime))) AS cpt FROM obs WHERE concept_id = 99037 group by person_id) I\n" +
                "    ON(A.person_id = I.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS inh FROM obs WHERE concept_id = 99604 group by person_id) J\n" +
                "    ON(A.person_id = J.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_text,'|',DATE(obs_datetime))) AS tb_reg FROM obs WHERE concept_id = 99031 group by person_id) TN\n" +
                "    ON(A.person_id = TN.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) AS tb_start_date FROM obs WHERE concept_id = 90217 group by person_id) TST\n" +
                "    ON(A.person_id = TST.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) AS tb_stop_date FROM obs WHERE concept_id = 90310 group by person_id) TSP\n" +
                "    ON(A.person_id = TSP.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS tb_status FROM obs WHERE concept_id = 90216 group by person_id) K\n" +
                "    ON(A.person_id = K.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) as baseline_ci FROM obs WHERE concept_id = 99083 group by person_id) L\n" +
                "    ON (A.person_id = L.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) as other_ci FROM obs WHERE concept_id = 90203 group by person_id) M\n" +
                "    ON (A.person_id = M.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) as eligible FROM obs WHERE concept_id = 90297 group by person_id) N\n" +
                "    ON (A.person_id = N.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) as eligible_ci FROM obs WHERE concept_id = 99083 group by person_id) O\n" +
                "    ON (A.person_id = O.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_numeric,'|',DATE(obs_datetime))) as eligible_cd4 FROM obs WHERE concept_id = 99082 group by person_id) P\n" +
                "    ON (A.person_id = P.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_numeric,'|',DATE(obs_datetime))) as eligible_tb FROM obs WHERE concept_id = 99600 group by person_id) Q\n" +
                "    ON (A.person_id = Q.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_numeric,'|',DATE(obs_datetime))) as eligible_bf FROM obs WHERE concept_id = 99601 group by person_id) R\n" +
                "    ON (A.person_id = R.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_numeric,'|',DATE(obs_datetime))) as eligible_preg FROM obs WHERE concept_id = 99602 group by person_id) S\n" +
                "    ON (A.person_id = S.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) as eligible_and_ready FROM obs WHERE concept_id = 90299 group by person_id) T\n" +
                "    ON (A.person_id = T.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT patient_id as person_id,group_concat(concat(encounter_id,'|',encounter_type,'|',DATE(encounter_datetime))) as encounters FROM encounter group by person_id) U\n" +
                "    ON (A.person_id = U.person_id)";

        SqlQueryBuilder q = new SqlQueryBuilder(sql);

        LocalDate workingDate = StubDate.dateOf(DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd"));

        TreeMap<String, Interval> periods = Periods.getQuarters(workingDate, 16);

        List<Object[]> results = evaluationService.evaluateToList(q, context);

        PatientDataHelper pdh = new PatientDataHelper();

        for (Object[] r : results) {

            DataSetRow row = new DataSetRow();
            String entryPoint = "";
            String cptStartDate = "";
            String inhStartDate = "";
            String tbStartDate = "";
            String tbStopDate = "";
            String dateEligible = "";
            String dateEligibleAndReady = "";
            String artStartDate = "";
            String tbRxNo = "";

            TreeMap<String, String> encounters = Helper.splitQuery((String) r[26], 1, 3);
            TreeMap<String, String> cpts = Periods.changeKeys(Helper.splitQuery((String) r[11], 1, 2), encounters);
            TreeMap<String, String> inhs = Periods.changeKeys(Helper.splitQuery((String) r[12], 1, 2), encounters);
            TreeMap<String, String> tbStarts = Helper.splitQuery((String) r[14], 2, 1);
            TreeMap<String, String> datesEligible = Helper.splitQuery((String) r[19], 2, 1);
            TreeMap<String, String> tbStops = Helper.splitQuery((String) r[15], 2, 1);
            TreeMap<String, String> datesEligibleAndReady = Helper.splitQuery((String) r[25], 2, 1);
            TreeMap<String, String> artStartDates = Helper.splitQuery((String) r[2], 2, 1);
            TreeMap<String, String> tbs = Periods.changeKeys(Helper.splitQuery((String) r[16], 1, 2), encounters);

            TreeMap<String, TreeMap<String, String>> tbMap = Periods.listOfDatesInPeriods(periods, tbs);
            TreeMap<String, TreeMap<String, String>> cptMap = Periods.listOfDatesInPeriods(periods, cpts);
            TreeMap<String, TreeMap<String, String>> inhMap = Periods.listOfDatesInPeriods(periods, inhs);
            //TreeMap<String, TreeMap<String, String>> nutritionalMap = Periods.listOfDatesInPeriods(periods, Helper.splitQuery((String) r[27], 3, 2));
            TreeMap<String, String> entryPointMap = Helper.splitQuery((String) r[9], 1, 2);

            if (StringUtils.isNotBlank((String) r[13])) {
                tbRxNo = (String) r[13];
            }

            if (MapUtils.isNotEmpty(entryPointMap)) {
                entryPoint = entryPointMap.entrySet().iterator().next().getValue();
            }

            if (MapUtils.isNotEmpty(cpts)) {
                cptStartDate = cpts.entrySet().iterator().next().getKey();
            }

            if (MapUtils.isNotEmpty(inhs)) {
                inhStartDate = inhs.entrySet().iterator().next().getKey();
            }

            if (MapUtils.isNotEmpty(tbStarts)) {
                tbStartDate = tbStarts.entrySet().iterator().next().getKey();
            }

            if (MapUtils.isNotEmpty(tbStops)) {
                tbStopDate = tbStops.entrySet().iterator().next().getKey();
            }

            if (MapUtils.isNotEmpty(datesEligible)) {
                dateEligible = datesEligible.entrySet().iterator().next().getKey();
            }

            if (MapUtils.isNotEmpty(datesEligibleAndReady)) {
                dateEligibleAndReady = datesEligibleAndReady.entrySet().iterator().next().getKey();
            }

            if (MapUtils.isNotEmpty(artStartDates)) {
                artStartDate = artStartDates.entrySet().iterator().next().getKey();
            }

            Integer biggestPeriod = 0;
            if (StringUtils.isNotBlank(artStartDate)) {
                biggestPeriod = Periods.isDateInTheInterval(artStartDate, periods);
            }
            if(biggestPeriod == null){
                biggestPeriod = 0;
            }

            String name = new StringBuilder()
                    .append(r[4] + "\n")
                    .append(r[5])
                    .toString();
            String tb1 = new StringBuilder()
                    .append(tbRxNo + "\n")
                    .append(tbStartDate + "\n")
                    .append(tbStopDate)
                    .toString();
            String inh1 = new StringBuilder()
                    .append(inhStartDate + "\n")
                    .append("")
                    .toString();

            String cpt1 = new StringBuilder()
                    .append(cptStartDate + "\n")
                    .append("")
                    .toString();

            pdh.addCol(row, "Date Enrolled", r[1]);
            pdh.addCol(row, "Unique ID no", null);
            pdh.addCol(row, "Patient Clinic ID", r[3]);
            pdh.addCol(row, "Name", name);
            pdh.addCol(row, "Gender", r[6]);
            pdh.addCol(row, "Age", r[7]);
            pdh.addCol(row, "Address", r[8]);
            pdh.addCol(row, "Entry Point", entryPoint);
            pdh.addCol(row, "Enrollment", r[10]);
            pdh.addCol(row, "CPT", cpt1);
            pdh.addCol(row, "INH", inh1);
            pdh.addCol(row, "TB", tb1);

            pdh.addCol(row, "CS1", null);
            pdh.addCol(row, "CS2", null);
            pdh.addCol(row, "CS3", null);
            pdh.addCol(row, "CS4", null);
            pdh.addCol(row, "Date Eligible", dateEligible);
            pdh.addCol(row, "Why Eligible", null);
            pdh.addCol(row, "Date Eligible and Ready", dateEligibleAndReady);
            pdh.addCol(row, "ART Start Date", artStartDate);


            for (int i = 0; i <= 15; i++) {
                String key = String.valueOf(i);
                if (i <= biggestPeriod) {
                    String fuStatus = "";
                    String tbStatus = "";
                    String cptInhStatus = "";
                    String nutritionalStatus = "";


                    TreeMap<String, String> tb = tbMap.get(key);
                    TreeMap<String, String> cpt = cptMap.get(key);
                    TreeMap<String, String> inh = inhMap.get(key);
                    //TreeMap<String, String> nutritional = nutritionalMap.get(key);

                    if (tb != null) {
                        tbStatus = tb.lastEntry().getValue();
                    }

                    if (cpt != null || inh != null) {
                        cptInhStatus = "Y";
                    }

                    StringBuilder fp = new StringBuilder()
                            .append(fuStatus + "\n")
                            .append(tbStatus + "\n");

                    if (StringUtils.isNotBlank(cptInhStatus) && StringUtils.isNotBlank(nutritionalStatus)) {
                        fp.append(cptInhStatus + "|" + nutritionalStatus);
                    } else if (StringUtils.isNotBlank(cptInhStatus) && StringUtils.isBlank(nutritionalStatus)) {
                        fp.append(cptInhStatus);
                    } else if (StringUtils.isBlank(cptInhStatus) && StringUtils.isNotBlank(nutritionalStatus)) {
                        fp.append(nutritionalStatus);
                    } else {
                        fp.append("");
                    }
                    String followupStatus = fp.toString();
                    pdh.addCol(row, "FUS" + key, followupStatus);
                } else {
                    pdh.addCol(row, "FUS" + key, "");
                }
            }

            dataSet.addRow(row);
        }
        return dataSet;
    }
}
