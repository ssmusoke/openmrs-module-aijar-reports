package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import com.google.common.collect.Lists;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xpath.SourceTree;
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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.ARTDatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by carapai on 11/05/2016.
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

        String dateToday = DateUtil.formatDate(new Date(), "yyyy-MM-dd");

        LocalDate workingDate = StubDate.dateOf(date);

        LocalDate startDate = Periods.monthStartFor(workingDate);
        LocalDate endDate = Periods.monthEndFor(workingDate);

        String startDateString = startDate.toString("yyyy-MM-dd");
        String endDateString = endDate.toString("yyyy-MM-dd");

        context = ObjectUtil.nvl(context, new EvaluationContext());

        String sql = "SELECT A.person_id,DATE(A.value_datetime),B.ti,C.identifier,D.family_name,D.given_name,E.gender,TIMESTAMPDIFF(YEAR, E.birthdate, '" + date + "') as age,\n" +
                "  F.county_district,G.functional_status,H.baseline_weight,I.muac,J.baseline_ci,K.baseline_cd4,L.baseline_cd4_per,M.viral_load,\n" +
                "  N.cpt,O.inh,P.tb_reg,Q.tb_start_date,R.tb_stop_date,S.pregnancy,T.baseline_regimen,U.baseline_regimen_other,V.line1_sub_date,\n" +
                "  W.line1_sub_reason,X.line2_sub_date,Y.line2_sub_reason,Z.line3_sub_date,AA.line3_sub_reason,AB.current_arv,AC.tb_status,AD.adh,\n" +
                "  AE.ci,AF.wieght,AG.cd4,AH.cd4_per,AI.encounters,AJ.appointments,E.dead,DATE(E.death_date),AK.trans_out,AL.trans_out_date,AM.lost,AN.lost_date,AO.interupt,AP.stop_date,AQ.restart_date\n" +
                "FROM\n" +
                "  (SELECT person_id,value_datetime FROM obs WHERE concept_id = 99161 AND value_datetime BETWEEN '" + startDateString + "' AND '" + endDateString + "' group by person_id) A\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) as ti FROM obs where concept_id = 99110 group by person_id) B\n" +
                "    ON (A.person_id = B.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT patient_id as person_id,identifier FROM patient_identifier WHERE identifier_type = 4 group by person_id) C\n" +
                "    ON (A.person_id = C.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,family_name,given_name FROM person_name group by person_id) D\n" +
                "    ON (A.person_id = D.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,gender,birthdate,dead,death_date FROM person group by person_id) E\n" +
                "    ON (A.person_id = E.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,county_district FROM person_address group by person_id) F\n" +
                "    ON (A.person_id = F.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) as functional_status FROM obs where concept_id = 90235  group by person_id) G\n" +
                "    ON (A.person_id = G.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_numeric,'|',DATE(obs_datetime))) as baseline_weight FROM obs where concept_id = 99069  group by person_id) H\n" +
                "    ON (A.person_id = H.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) as muac FROM obs where concept_id = 99030  group by person_id) I\n" +
                "    ON (A.person_id = I.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) as baseline_ci FROM obs where concept_id = 99070  group by person_id) J\n" +
                "    ON (A.person_id = J.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_numeric,'|',DATE(obs_datetime))) as baseline_cd4 FROM obs where concept_id = 99071  group by person_id) K\n" +
                "    ON (A.person_id = K.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) as baseline_cd4_per FROM obs where concept_id = 99151  group by person_id) L\n" +
                "    ON (A.person_id = L.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_numeric,'|',DATE(obs_datetime))) as viral_load FROM obs where concept_id = 856  group by person_id) M\n" +
                "    ON (A.person_id = M.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_numeric,'|',DATE(obs_datetime))) AS cpt FROM obs WHERE concept_id = 99037 group by person_id) N\n" +
                "    ON(A.person_id = N.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS inh FROM obs WHERE concept_id = 99604 group by person_id) O\n" +
                "    ON(A.person_id = O.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_text,'|',DATE(obs_datetime))) AS tb_reg FROM obs WHERE concept_id = 99031 group by person_id) P\n" +
                "    ON(A.person_id = P.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) AS tb_start_date FROM obs WHERE concept_id = 90217 group by person_id) Q\n" +
                "    ON(A.person_id = Q.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) AS tb_stop_date FROM obs WHERE concept_id = 90310 group by person_id) R\n" +
                "    ON(A.person_id = R.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) AS pregnancy FROM obs WHERE concept_id = 5596 group by person_id) S\n" +
                "    ON(A.person_id = S.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS baseline_regimen FROM obs WHERE concept_id = 99061 group by person_id) T\n" +
                "    ON(A.person_id = T.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_text,'|',DATE(obs_datetime))) AS baseline_regimen_other FROM obs WHERE concept_id = 99268 group by person_id) U\n" +
                "    ON(A.person_id = U.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) AS line1_sub_date FROM obs WHERE concept_id = 99163 group by person_id) V\n" +
                "    ON(A.person_id = V.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS line1_sub_reason FROM obs WHERE concept_id = 90246 group by person_id) W\n" +
                "    ON(A.person_id = W.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) AS line2_sub_date FROM obs WHERE concept_id = 99164 group by person_id) X\n" +
                "    ON(A.person_id = X.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS line2_sub_reason FROM obs WHERE concept_id = 90247 group by person_id) Y\n" +
                "    ON(A.person_id = Y.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) AS line3_sub_date FROM obs WHERE concept_id = 162991 group by person_id) Z\n" +
                "    ON(A.person_id = Z.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS line3_sub_reason FROM obs WHERE concept_id = 90247 group by person_id) AA\n" +
                "    ON(A.person_id = AA.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS current_arv FROM obs WHERE concept_id = 90315 group by person_id) AB\n" +
                "    ON(A.person_id = AB.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS tb_status FROM obs WHERE concept_id = 90216 group by person_id) AC\n" +
                "    ON(A.person_id = AC.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS adh FROM obs WHERE concept_id = 90220 group by person_id) AD\n" +
                "    ON(A.person_id = AD.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS ci FROM obs WHERE concept_id = 90203 group by person_id) AE\n" +
                "    ON(A.person_id = AE.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_numeric,'|',DATE(obs_datetime))) AS wieght FROM obs WHERE concept_id = 90236 group by person_id) AF\n" +
                "    ON(A.person_id = AF.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS cd4 FROM obs WHERE concept_id = 5497 group by person_id) AG\n" +
                "    ON(A.person_id = AG.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS cd4_per FROM obs WHERE concept_id = 730 group by person_id) AH\n" +
                "    ON(A.person_id = AH.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT patient_id as person_id,group_concat(concat(encounter_id,'|',encounter_type,'|',DATE(encounter_datetime))) as encounters FROM encounter group by person_id) AI\n" +
                "    ON (A.person_id = AI.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) AS appointments FROM obs WHERE concept_id = 5096 group by person_id) AJ\n" +
                "    ON(A.person_id = AJ.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS trans_out FROM obs WHERE concept_id = 90306 group by person_id) AK\n" +
                "    ON(A.person_id = AK.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) AS trans_out_date FROM obs WHERE concept_id = 99165 group by person_id) AL\n" +
                "    ON(A.person_id = AL.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS lost FROM obs WHERE concept_id = 5240 group by person_id) AM\n" +
                "    ON(A.person_id = AM.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) AS lost_date FROM obs WHERE concept_id = 90209 group by person_id) AN\n" +
                "    ON(A.person_id = AN.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS interupt FROM obs WHERE concept_id = 99132 group by person_id) AO\n" +
                "    ON(A.person_id = AO.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) AS stop_date FROM obs WHERE concept_id = 99084 group by person_id) AP\n" +
                "    ON(A.person_id = AP.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,obs_datetime,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) AS restart_date FROM obs WHERE concept_id = 99085 group by person_id) AQ\n" +
                "    ON(A.person_id = AQ.person_id)";

        SqlQueryBuilder q = new SqlQueryBuilder(sql);

        TreeMap<String, Interval> periods = Periods.getMonths(startDate, 72);

        List<Object[]> results = evaluationService.evaluateToList(q, context);

        PatientDataHelper pdh = new PatientDataHelper();

        for (Object[] r : results) {
            DataSetRow row = new DataSetRow();

            String cptStartDate = "";
            String inhStartDate = "";
            String tbStartDate = "";
            String tbStopDate = "";
            String functionalStatus = "";

            String cd4 = "";
            String viralLoad = "";
            String weight = "";
            String muac = "";
            String ci = "";
            String cd4Per = "";
            String baselineARV = "";

            TreeMap<String, String> encounters = Helper.splitQuery((String) r[37], 1, 3);

            List<String> encounterDates = Periods.listOfDatesInPeriods(periods, Lists.newArrayList(Helper.splitQuery((String) r[37], 3, 1).keySet()));

            System.out.println(encounterDates);

            TreeMap<String, String> cpts = Periods.changeKeys(Helper.splitQuery((String) r[16], 1, 2), encounters);
            TreeMap<String, String> inhs = Periods.changeKeys(Helper.splitQuery((String) r[17], 1, 2), encounters);
            TreeMap<String, String> tbStarts = Helper.splitQuery((String) r[19], 2, 1);
            TreeMap<String, String> tbStops = Helper.splitQuery((String) r[20], 2, 1);

            String deathDate = DateUtil.formatDate((Date) r[40], "yyyy-MM-dd");
            List<String> lostDates = Periods.listOfDatesInPeriods(periods, Lists.newArrayList(Helper.splitQuery((String) r[44], 2, 1).keySet()));
            List<String> stoppedDates = Periods.listOfDatesInPeriods(periods, Lists.newArrayList(Helper.splitQuery((String) r[46], 2, 1).keySet()));
            List<String> restartDates = Periods.listOfDatesInPeriods(periods, Lists.newArrayList(Helper.splitQuery((String) r[47], 2, 1).keySet()));
            List<String> appointments = Periods.listOfDatesInPeriods(periods, Lists.newArrayList(Helper.splitQuery((String) r[38], 2, 1).keySet()));
            List<String> tos = Periods.listOfDatesInPeriods(periods, Lists.newArrayList(Helper.splitQuery((String) r[42], 2, 1).keySet()));


            TreeMap<String, String> funcStatuses = Periods.changeKeys(Helper.splitQuery((String) r[9], 1, 2), encounters);

            String artStartDate = DateUtil.formatDate((Date) r[1], "yyyy-MM-dd");

            TreeMap<String, String> cd4s = Periods.changeKeys(Helper.splitQuery((String) r[13], 1, 2), encounters);
            TreeMap<String, String> viralLoads = Periods.changeKeys(Helper.splitQuery((String) r[15], 1, 2), encounters);
            TreeMap<String, String> weights = Periods.changeKeys(Helper.splitQuery((String) r[10], 1, 2), encounters);
            TreeMap<String, String> muacs = Periods.changeKeys(Helper.splitQuery((String) r[11], 1, 2), encounters);
            TreeMap<String, String> cis = Periods.changeKeys(Helper.splitQuery((String) r[12], 1, 2), encounters);
            TreeMap<String, String> cd4sPer = Periods.changeKeys(Helper.splitQuery((String) r[14], 1, 2), encounters);
            TreeMap<String, String> baselinesARV = Periods.changeKeys(Helper.splitQuery((String) r[22], 1, 2), encounters);

            TreeMap<String, String> tbs = Periods.changeKeys(Helper.splitQuery((String) r[31], 1, 2), encounters);
            TreeMap<String, String> currentARVS = Periods.changeKeys(Helper.splitQuery((String) r[30], 1, 2), encounters);
            TreeMap<String, String> adhs = Periods.changeKeys(Helper.splitQuery((String) r[32], 1, 2), encounters);

            TreeMap<String, TreeMap<String, String>> tbMap = Periods.listOfDatesInPeriods(periods, tbs);
            TreeMap<String, TreeMap<String, String>> cptMap = Periods.listOfDatesInPeriods(periods, cpts);
            TreeMap<String, TreeMap<String, String>> inhMap = Periods.listOfDatesInPeriods(periods, inhs);
            TreeMap<String, TreeMap<String, String>> arvMap = Periods.listOfDatesInPeriods(periods, currentARVS);
            TreeMap<String, TreeMap<String, String>> adhMap = Periods.listOfDatesInPeriods(periods, adhs);

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

            if (MapUtils.isNotEmpty(funcStatuses)) {
                functionalStatus = funcStatuses.entrySet().iterator().next().getValue();
            }


            if (MapUtils.isNotEmpty(cd4s)) {
                cd4 = cd4s.entrySet().iterator().next().getValue();
            }

            if (MapUtils.isNotEmpty(viralLoads)) {
                viralLoad = viralLoads.entrySet().iterator().next().getValue();
            }

            if (MapUtils.isNotEmpty(weights)) {
                weight = weights.entrySet().iterator().next().getValue();
            }

            if (MapUtils.isNotEmpty(muacs)) {
                muac = muacs.entrySet().iterator().next().getValue();
            }

            if (MapUtils.isNotEmpty(cis)) {
                ci = cis.entrySet().iterator().next().getValue();
            }
            if (MapUtils.isNotEmpty(cd4sPer)) {
                cd4Per = cd4sPer.entrySet().iterator().next().getValue();
            }

            if (MapUtils.isNotEmpty(baselinesARV)) {
                baselineARV = baselinesARV.entrySet().iterator().next().getValue();
            }

            Integer biggestPeriod = 0;

            Integer largestPeriod = Periods.isDateInTheInterval(dateToday, periods);

            Integer deathPeriod = null;
            if (StringUtils.isNotBlank(deathDate)) {
                deathPeriod = Periods.isDateInTheInterval(deathDate, periods);
            }

            if (StringUtils.isNotBlank(artStartDate)) {
                biggestPeriod = Periods.isDateInTheInterval(artStartDate, periods);
            }

            if (biggestPeriod == null) {
                biggestPeriod = 0;
            }

            if (largestPeriod == null) {
                largestPeriod = 72;
            }

            String name = new StringBuilder()
                    .append(r[4] + "\n")
                    .append(r[5])
                    .toString();

            String tb1 = new StringBuilder()
                    .append(r[18] + "\n")
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
            String viralCD4 = new StringBuilder().append(cd4 + "/" + cd4Per + "\n").append(viralLoad).toString();
            pdh.addCol(row, "Date ART Started", artStartDate);
            pdh.addCol(row, "Unique ID no", r[0]);
            pdh.addCol(row, "Patient Clinic ID", r[3]);
            pdh.addCol(row, "TI", r[2]);
            pdh.addCol(row, "Name", name);
            pdh.addCol(row, "Gender", r[6]);
            pdh.addCol(row, "Age", r[7]);
            pdh.addCol(row, "Address", r[8]);
            pdh.addCol(row, "FUS", functionalStatus);
            pdh.addCol(row, "Weight", weight);
            pdh.addCol(row, "Muac", muac);
            pdh.addCol(row, "CS", ci);
            pdh.addCol(row, "CDVL", viralCD4);

            pdh.addCol(row, "CPT", cpt1);
            pdh.addCol(row, "INH", inh1);

            pdh.addCol(row, "TB", tb1);

            //TODO Process Pregnancies
            pdh.addCol(row, "P1", r[21]);
            pdh.addCol(row, "P2", r[21]);
            pdh.addCol(row, "P3", r[21]);
            pdh.addCol(row, "P4", r[21]);

            pdh.addCol(row, "BASE REGIMEN", Helper.getString(baselineARV));

            //TODO Format Subs and Switch Dates
            pdh.addCol(row, "L1S", r[25]);
            pdh.addCol(row, "L2S", r[26]);
            pdh.addCol(row, "L3S", r[27]);
            /*pdh.addCol(row, "L1 Sub Date", r[24]);
            pdh.addCol(row, "L1 Sub Reason", r[25]);
            pdh.addCol(row, "L2 Switch Date", r[26]);
            pdh.addCol(row, "L2 Switch Reason", r[27]);
            pdh.addCol(row, "L3 Switch Date", r[28]);
            pdh.addCol(row, "L3 Switch Reason", r[29]);*/
            for (int i = 0; i <= 72; i++) {
                String key = String.valueOf(i);
                List<String> fusStates = new ArrayList<String>();
                if (i >= biggestPeriod && i <= largestPeriod) {
                    String fuStatus = "";
                    String tbStatus = "";
                    String cptInhStatus = "";
                    String currentArv = "";
                    String adherence = "";

                    if (deathPeriod != null) {
                        if (deathPeriod == i) {
                            fusStates.add("1");
                        }
                    }
                    if (stoppedDates.contains(key)) {
                        fusStates.add("2");
                    }
                    if (restartDates.contains(key)) {
                        fusStates.add("6");
                    }
                    if (tos.contains(key)) {
                        fusStates.add("5");
                    }
                    if (deathPeriod != null) {
                        if (!encounterDates.contains(key) && deathPeriod != i && !appointments.contains(key) && !stoppedDates.contains(key) && !restartDates.contains(key) && !tos.contains(key)) {
                            fusStates.add("\u2713");
                        } else if (!encounterDates.contains(key) && appointments.contains(key)) {
                            fusStates.add("3");
                        }
                    } else if (!encounterDates.contains(key) && !appointments.contains(key) && !stoppedDates.contains(key) && !restartDates.contains(key) && !tos.contains(key)) {
                        fusStates.add("\u2713");
                    } else if (!encounterDates.contains(key) && appointments.contains(key)) {
                        fusStates.add("3");
                    }

                    TreeMap<String, String> tb = tbMap.get(key);
                    TreeMap<String, String> cpt = cptMap.get(key);
                    TreeMap<String, String> inh = inhMap.get(key);
                    TreeMap<String, String> arv = arvMap.get(key);
                    TreeMap<String, String> adh = adhMap.get(key);

                    if (MapUtils.isNotEmpty(tb)) {
                        tbStatus = tb.lastEntry().getValue();
                    }

                    if (MapUtils.isNotEmpty(inh) || MapUtils.isNotEmpty(cpt)) {
                        cptInhStatus = "Y";
                    } else {
                        cptInhStatus = "N";
                    }

                    if (MapUtils.isNotEmpty(arv)) {
                        currentArv = arv.lastEntry().getValue();
                    }
                    if (MapUtils.isNotEmpty(adh)) {
                        adherence = adh.lastEntry().getValue();
                    }

                    StringBuilder cptAdherence = new StringBuilder();
                    if (StringUtils.isNotBlank(cptInhStatus) && StringUtils.isNotBlank(adherence)) {
                        cptAdherence.append(cptInhStatus + "|" + Helper.getString(adherence));
                    } else if (StringUtils.isNotBlank(cptInhStatus) && StringUtils.isBlank(adherence)) {
                        cptAdherence.append(cptInhStatus);
                    } else if (StringUtils.isBlank(cptInhStatus) && StringUtils.isNotBlank(adherence)) {
                        cptAdherence.append(StringUtils.isBlank(adherence));
                    } else {
                        cptAdherence.append("");
                    }

                    String followupStatus = new StringBuilder()
                            .append(Helper.getString(currentArv) + "|" + StringUtils.join(fusStates, ",") + "\n")
                            .append(Helper.getString(tbStatus) + "\n")
                            .append(cptAdherence.toString())
                            .toString();
                    pdh.addCol(row, "FUS" + key, followupStatus);
                    //TODO create ci,w,CDVL over the periods
                    if (i == 6 || i == 12 || i == 24 || i == 36 || i == 48 || i == 60 || i == 72) {
                        pdh.addCol(row, "CI" + key, "");
                        pdh.addCol(row, "W" + key, "");
                        pdh.addCol(row, "CDVL" + key, "");
                    }
                } else {
                    pdh.addCol(row, "FUS" + key, "");

                    if (i == 6 || i == 12 || i == 24 || i == 36 || i == 48 || i == 60 || i == 72) {
                        pdh.addCol(row, "CI" + key, "");
                        pdh.addCol(row, "W" + key, "");
                        pdh.addCol(row, "CDVL" + key, "");
                    }
                }
            }

            dataSet.addRow(row);
        }
        return dataSet;
    }
}
