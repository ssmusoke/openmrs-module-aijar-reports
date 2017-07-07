package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import com.google.common.base.Joiner;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.joda.time.LocalDate;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.*;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.service.PatientDataService;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.common.Periods;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.HMIS106A1BDataSetDefinition;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by carapai on 17/10/2016.
 */
@Handler(supports = {HMIS106A1BDataSetDefinition.class})
public class HMIS106A1BDataSetEvaluator implements DataSetEvaluator {
    @Autowired
    private EvaluationService evaluationService;
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;
    @Autowired
    DataFactory df;
    @Autowired
    private CohortDefinitionService cohortDefinitionService;
    @Autowired
    private HIVMetadata hivMetadata;
    @Autowired
    private PatientDataService patientDataService;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evaluationContext) throws EvaluationException {

        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, evaluationContext);
        HMIS106A1BDataSetDefinition definition = (HMIS106A1BDataSetDefinition) dataSetDefinition;

        String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");

        evaluationContext = ObjectUtil.nvl(evaluationContext, new EvaluationContext());

        PatientDataHelper pdh = new PatientDataHelper();

        LocalDate date = StubDate.dateOf(definition.getStartDate());
        List<LocalDate> q1 = Periods.subtractQuarters(date, 2);
        List<LocalDate> q2 = Periods.subtractQuarters(date, 4);
        List<LocalDate> q3 = Periods.subtractQuarters(date, 8);
        List<LocalDate> q4 = Periods.subtractQuarters(date, 12);
        List<LocalDate> q5 = Periods.subtractQuarters(date, 16);
        List<LocalDate> q6 = Periods.subtractQuarters(date, 20);
        List<LocalDate> q7 = Periods.subtractQuarters(date, 24);

        List<String> periods = new ArrayList<String>();
        List<String> endDates = new ArrayList<String>();
        endDates.add(q1.get(1).toString("yyyy-MM-dd"));
        endDates.add(q1.get(2).toString("yyyy-MM-dd"));
        endDates.add(q1.get(3).toString("yyyy-MM-dd"));
        endDates.add(q1.get(4).toString("yyyy-MM-dd"));
        endDates.add(q1.get(5).toString("yyyy-MM-dd"));
        endDates.add(q1.get(6).toString("yyyy-MM-dd"));
        endDates.add(q1.get(7).toString("yyyy-MM-dd"));

        periods.add(q1.get(0).toString("MMM") + " - " + q1.get(1).toString("MMM") + " " + q1.get(1).toString("yyyy"));
        periods.add(q2.get(0).toString("MMM") + " - " + q2.get(1).toString("MMM") + " " + q2.get(1).toString("yyyy"));
        periods.add(q3.get(0).toString("MMM") + " - " + q3.get(1).toString("MMM") + " " + q3.get(1).toString("yyyy"));
        periods.add(q4.get(0).toString("MMM") + " - " + q4.get(1).toString("MMM") + " " + q4.get(1).toString("yyyy"));
        periods.add(q5.get(0).toString("MMM") + " - " + q5.get(1).toString("MMM") + " " + q5.get(1).toString("yyyy"));
        periods.add(q6.get(0).toString("MMM") + " - " + q6.get(1).toString("MMM") + " " + q6.get(1).toString("yyyy"));
        periods.add(q7.get(0).toString("MMM") + " - " + q7.get(1).toString("MMM") + " " + q7.get(1).toString("yyyy"));

        List<String> quarters = new ArrayList<String>();

        quarters.add(String.valueOf(q1.get(0).getYear()) + "Q" + String.valueOf(((q1.get(0).getMonthOfYear() - 1) / 3) + 1));
        quarters.add(String.valueOf(q2.get(0).getYear()) + "Q" + String.valueOf(((q2.get(0).getMonthOfYear() - 1) / 3) + 1));
        quarters.add(String.valueOf(q3.get(0).getYear()) + "Q" + String.valueOf(((q3.get(0).getMonthOfYear() - 1) / 3) + 1));
        quarters.add(String.valueOf(q4.get(0).getYear()) + "Q" + String.valueOf(((q4.get(0).getMonthOfYear() - 1) / 3) + 1));
        quarters.add(String.valueOf(q5.get(0).getYear()) + "Q" + String.valueOf(((q5.get(0).getMonthOfYear() - 1) / 3) + 1));
        quarters.add(String.valueOf(q6.get(0).getYear()) + "Q" + String.valueOf(((q6.get(0).getMonthOfYear() - 1) / 3) + 1));
        quarters.add(String.valueOf(q7.get(0).getYear()) + "Q" + String.valueOf(((q7.get(0).getMonthOfYear() - 1) / 3) + 1));

        String query = "SELECT\n" +
                "  CONCAT_WS('',YEAR(value_datetime),'Q',quarter(value_datetime)),\n" +
                "  group_concat(DISTINCT person_id)\n" +
                "FROM obs\n" +
                "WHERE concept_id = 99161\n" +
                "GROUP BY CONCAT_WS('',YEAR(value_datetime),'Q',quarter(value_datetime))";
        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(query);

        Map<String, String> results = evaluationService.evaluateToMap(q, String.class, String.class, evaluationContext);

        CohortDefinition pregnant = hivCohortDefinitionLibrary.getPregnantPatientsAtArtStart();
        CohortDefinition lactating = hivCohortDefinitionLibrary.getLactatingPatientsAtArtStart();
        CohortDefinition pregnantOrLactating = df.getPatientsInAny(pregnant, lactating);

        Cohort enrolledViaPMTCT = Context.getService(CohortDefinitionService.class).evaluate(pregnantOrLactating, null);

        CohortDefinition artTransferInRegimen = df.getPatientsWithConcept(hivMetadata.getArtRegimenTransferInDate(), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition artTransferInRegimenOther = df.getPatientsWithConcept(hivMetadata.getOtherArtTransferInRegimen(), BaseObsCohortDefinition.TimeModifier.ANY);

        DateObsCohortDefinition artTransferInDate = new DateObsCohortDefinition();
        artTransferInDate.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        artTransferInDate.setQuestion(hivMetadata.getArtRegimenTransferInDate());

        CohortDefinition artTransferIn = df.getPatientsInAny(artTransferInRegimen, artTransferInRegimenOther, artTransferInDate);

        Cohort transferInPatients = Context.getService(CohortDefinitionService.class).evaluate(artTransferIn, null);

        Map<Integer, Object> baselineCD4;
        Map<Integer, Object> baselineCD4Mothers;

        int months = 6;
        DecimalFormat df = new DecimalFormat("###.##");

        for (int i = 0; i < periods.size(); i++) {
            if (i > 0) {
                months = i * 12;
            }
            DataSetRow all = new DataSetRow();
            DataSetRow eMTCT = new DataSetRow();
            Map<Integer, Object> above5AtArtStart = getPatientsAged5AndAbove(results.get(quarters.get(i)), endDates.get(i));
            Cohort allStarted = new Cohort(results.get(quarters.get(i)));

            Collection startedArt = CollectionUtils.subtract(allStarted.getMemberIds(), transferInPatients.getMemberIds());
            Collection transferIn = CollectionUtils.intersection(allStarted.getMemberIds(), transferInPatients.getMemberIds());

            Collection allMothers = CollectionUtils.intersection(allStarted.getMemberIds(), enrolledViaPMTCT.getMemberIds());
            Collection allMotherStarted = CollectionUtils.subtract(allMothers, transferInPatients.getMemberIds());
            Collection mothersTransferIn = CollectionUtils.intersection(allMothers, transferInPatients.getMemberIds());


            pdh.addCol(all, "patients", "All patients " + String.valueOf(months) + " months");
            pdh.addCol(eMTCT, "patients", "eMTCT Mothers " + String.valueOf(months) + " months");

            pdh.addCol(all, "when", periods.get(i));
            pdh.addCol(eMTCT, "when", periods.get(i));

            pdh.addCol(all, "enrolled", startedArt.size());
            pdh.addCol(eMTCT, "enrolled", allMotherStarted.size());

            if (startedArt.size() > 0) {
                baselineCD4 = getPatientBaselineCD4Data(Joiner.on(",").join(startedArt));
                Map<Integer, Object> cD4L500 = getPatientBaselineCD4DataLS500(baselineCD4);
                Map<Integer, Object> transferOut = getPatientTransferredOut(Joiner.on(",").join(startedArt), endDate);
                Collection net = CollectionUtils.subtract(allStarted.getMemberIds(), transferOut.keySet());
                Map<String, Cohort> lost = getLostPatients(Joiner.on(",").join(net), endDate);
                Set<Integer> stopped = getPatientStopped(Joiner.on(",").join(net), endDate);
                Map<Integer, Object> dead = getDeadPatients(Joiner.on(",").join(net), endDate);
                Cohort patientsLost = lost.get("lost");
                Cohort patientsDropped = lost.get("dropped");

                Collection allLostAndDied = CollectionUtils.union(CollectionUtils.union(stopped, dead.keySet()), CollectionUtils.union(patientsLost.getMemberIds(), patientsDropped.getMemberIds()));
                Collection alive = CollectionUtils.subtract(net, allLostAndDied);

                Map<Integer, Object> cCD4 = getPatientWithRecentCD4(Joiner.on(",").join(alive), endDate);
                Map<Integer, Object> pCD4L500 = getPatientBaselineCD4DataLS500(cCD4);
                pdh.addCol(all, "baseFraction", df.format(((double) cD4L500.size()) / baselineCD4.size()));
                pdh.addCol(all, "baseMedian", getMedianCD4(cD4L500));
                pdh.addCol(all, "transferOut", getPatientTransferredOut(Joiner.on(",").join(startedArt), endDate).size());
                pdh.addCol(all, "netCohort", net.size());
                pdh.addCol(all, "stopped", stopped.size());
                pdh.addCol(all, "died", dead.size());
                pdh.addCol(all, "lost", patientsLost.getSize());
                pdh.addCol(all, "dropped", patientsDropped.getSize());
                pdh.addCol(all, "alive", alive.size());
                pdh.addCol(all, "percentageAlive", df.format((alive.size() * 100.00) / net.size()));
                pdh.addCol(all, "fraction", df.format(((double) pCD4L500.size()) / cCD4.size()));
                pdh.addCol(all, "median", getMedianCD4(pCD4L500));

            } else {
                pdh.addCol(all, "baseFraction", "-");
                pdh.addCol(all, "baseMedian", "-");
                pdh.addCol(all, "transferOut", 0);
                pdh.addCol(all, "netCohort", 0);
                pdh.addCol(all, "stopped", 0);
                pdh.addCol(all, "died", 0);
                pdh.addCol(all, "lost", 0);
                pdh.addCol(all, "dropped", 0);
                pdh.addCol(all, "alive", 0);
                pdh.addCol(all, "percentageAlive", 0);
                pdh.addCol(all, "fraction", "-");
                pdh.addCol(all, "median", "-");
            }

            if (allMotherStarted.size() > 0) {
                baselineCD4Mothers = getPatientBaselineCD4Data(Joiner.on(",").join(allMotherStarted));
                Map<Integer, Object> mothersCD4L500 = getPatientBaselineCD4DataLS500(baselineCD4Mothers);
                Map<Integer, Object> transferOutMothers = getPatientTransferredOut(Joiner.on(",").join(allMotherStarted), endDate);
                Collection netMothers = CollectionUtils.subtract(allMothers, transferOutMothers.keySet());
                Map<String, Cohort> lostMothers = getLostPatients(Joiner.on(",").join(netMothers), endDate);
                Set<Integer> stoppedMothers = getPatientStopped(Joiner.on(",").join(netMothers), endDate);
                Map<Integer, Object> deadMothers = getDeadPatients(Joiner.on(",").join(netMothers), endDate);
                Cohort patientsLost = lostMothers.get("lost");
                Cohort patientsDropped = lostMothers.get("dropped");
                Collection allLostAndDied = CollectionUtils.union(CollectionUtils.union(stoppedMothers, deadMothers.keySet()), CollectionUtils.union(patientsLost.getMemberIds(), patientsDropped.getMemberIds()));
                Collection alive = CollectionUtils.subtract(netMothers, allLostAndDied);
                Map<Integer, Object> cCD4W = getPatientWithRecentCD4(Joiner.on(",").join(alive), endDate);
                Map<Integer, Object> pCD4L500W = getPatientBaselineCD4DataLS500(cCD4W);
                pdh.addCol(eMTCT, "baseFraction", df.format(((double) mothersCD4L500.size()) / baselineCD4Mothers.size()));
                pdh.addCol(eMTCT, "baseMedian", getMedianCD4(mothersCD4L500));
                pdh.addCol(eMTCT, "transferOut", transferOutMothers.size());
                pdh.addCol(eMTCT, "netCohort", netMothers.size());
                pdh.addCol(eMTCT, "stopped", getPatientStopped(Joiner.on(",").join(netMothers), endDate));
                pdh.addCol(eMTCT, "died", getDeadPatients(Joiner.on(",").join(netMothers), endDate).size());
                pdh.addCol(eMTCT, "lost", lostMothers.get("lost").getSize());
                pdh.addCol(eMTCT, "dropped", lostMothers.get("dropped").getSize());
                pdh.addCol(eMTCT, "alive", alive.size());
                pdh.addCol(eMTCT, "percentageAlive", df.format((alive.size() * 100.00) / netMothers.size()));

                pdh.addCol(eMTCT, "fraction", df.format(((double) pCD4L500W.size()) / cCD4W.size()));
                pdh.addCol(eMTCT, "median", getMedianCD4(pCD4L500W));
            } else {
                pdh.addCol(eMTCT, "baseFraction", "-");
                pdh.addCol(eMTCT, "baseMedian", "-");
                pdh.addCol(eMTCT, "transferOut", 0);
                pdh.addCol(eMTCT, "netCohort", 0);
                pdh.addCol(eMTCT, "stopped", 0);
                pdh.addCol(eMTCT, "died", 0);
                pdh.addCol(eMTCT, "lost", 0);
                pdh.addCol(eMTCT, "dropped", 0);
                pdh.addCol(eMTCT, "alive", 0);
                pdh.addCol(eMTCT, "percentageAlive", 0.0);
                pdh.addCol(eMTCT, "fraction", "-");
                pdh.addCol(eMTCT, "median", "-");
            }

            pdh.addCol(all, "transferIn", transferIn.size());
            pdh.addCol(eMTCT, "transferIn", mothersTransferIn.size());

            dataSet.addRow(all);
            dataSet.addRow(eMTCT);
        }

        return dataSet;
    }

    private Map<Integer, Object> getPatientBaselineCD4Data(String cohort) throws EvaluationException {
        String sql = String.format("select o.person_id, o.value_numeric from obs o where o.concept_id = 99071 and person_id in (select o.person_id from obs o inner join person p using(person_id) where o.concept_id = 99161 and YEAR(o.value_datetime) - YEAR(p.birthdate) - (RIGHT(o.value_datetime, 5) < RIGHT(p.birthdate, 5)) > 5 and p.person_id in(%s))", cohort);
        SqlPatientDataDefinition definition = new SqlPatientDataDefinition();
        definition.setSql(sql);
        EvaluationContext context = new EvaluationContext();
        context.setBaseCohort(new Cohort(cohort));
        return patientDataService.evaluate(definition, context).getData();
    }

    private Map<Integer, Object> getPatientWithRecentCD4(String cohort, String endDate) throws EvaluationException {
        String sql = String.format("select DISTINCT A.person_id,A.value_numeric from (select o.person_id, o.value_numeric,o.obs_datetime from obs o where o.person_id in (%s) and o.concept_id = 5497 and obs_datetime <= '%s' and voided = 0) A  LEFT JOIN (select o.person_id, o.value_numeric,o.obs_datetime from obs o where o.person_id in (%s) and o.concept_id = 5497 and obs_datetime <= '%s' and voided = 0) B ON(A.person_id = B.person_id AND A.obs_datetime < B.obs_datetime) WHERE B.person_id IS NULL", cohort, endDate, cohort, endDate);
        SqlPatientDataDefinition definition = new SqlPatientDataDefinition();
        definition.setSql(sql);
        EvaluationContext context = new EvaluationContext();
        context.setBaseCohort(new Cohort(cohort));
        return patientDataService.evaluate(definition, context).getData();
    }

    private Map<Integer, Object> getPatientTransferredOut(String cohort, String endDate) throws EvaluationException {
        String sql = String.format("select o.person_id, o.value_datetime from obs o where o.person_id in (%s) and o.concept_id = 99165 and o.value_datetime <= '%s'", cohort, endDate);
        SqlPatientDataDefinition definition = new SqlPatientDataDefinition();
        definition.setSql(sql);
        EvaluationContext context = new EvaluationContext();
        context.setBaseCohort(new Cohort(cohort));
        return patientDataService.evaluate(definition, context).getData();
    }

    private Map<Integer, Object> getPatientsAged5AndAbove(String cohort, String endDate) throws EvaluationException {
        String sql = String.format("select person_id,birthdate, YEAR('%s') - YEAR(birthdate) - (RIGHT('%s', 5) < RIGHT(birthdate, 5)) as age from person where person_id in(%s) and  birthdate is not null HAVING  age > 5", endDate, endDate, cohort);
        SqlPatientDataDefinition definition = new SqlPatientDataDefinition();
        definition.setSql(sql);
        EvaluationContext context = new EvaluationContext();
        context.setBaseCohort(new Cohort(cohort));
        return patientDataService.evaluate(definition, context).getData();
    }

    private Set<Integer> getPatientStopped(String cohort, String endDate) throws EvaluationException {
        String sqlStopped = String.format("select person_id, MAX(DATE(o.value_datetime)) from obs o where o.person_id in (%s) and o.concept_id = 99084 and o.value_datetime <= '%s' group by person_id", cohort, endDate);
        String sqlRestarted = String.format("select person_id, MAX(DATE(o.value_datetime)) from obs o where o.person_id in (%s) and o.concept_id = 99085 and o.value_datetime <= '%s' group by person_id", cohort, endDate);
        SqlPatientDataDefinition stoppedDefinition = new SqlPatientDataDefinition();
        SqlPatientDataDefinition restartedDefinition = new SqlPatientDataDefinition();

        stoppedDefinition.setSql(sqlStopped);
        restartedDefinition.setSql(sqlRestarted);
        EvaluationContext context = new EvaluationContext();
        context.setBaseCohort(new Cohort(cohort));
        Set<Integer> clients = new HashSet<Integer>();

        Map<Integer, Object> stoppedPatients = patientDataService.evaluate(stoppedDefinition, context).getData();
        Map<Integer, Object> restartedPatients = patientDataService.evaluate(restartedDefinition, context).getData();
        for (Map.Entry<Integer, Object> o : stoppedPatients.entrySet()) {
            Date stopDate = DateUtil.parseDate(String.valueOf(o.getValue()), "yyyy-MM-dd");
            Object restartDate = restartedPatients.get(o.getKey());
            if (restartDate != null) {
                Date r = DateUtil.parseDate(String.valueOf(restartDate), "yyyy-MM-dd");
                if (r.before(stopDate)) {
                    clients.add(o.getKey());
                }
            } else {
                clients.add(o.getKey());
            }
        }
        return clients;
    }

    private Map<Integer, Object> getDeadPatients(String cohort, String endDate) throws EvaluationException {
        String sql = "select * from\n" +
                String.format("  (select person_id,Date(value_datetime) as death_date from obs where concept_id = 90272 and person_id in(%s) and value_datetime < '%s'\n", cohort, endDate) +
                "union\n" +
                String.format("select person_id, DATE(death_date) from person WHERE death_date is not null and person_id in(%s) and death_date < '%s') A group by person_id", cohort, endDate);
        SqlPatientDataDefinition definition = new SqlPatientDataDefinition();
        definition.setSql(sql);
        EvaluationContext context = new EvaluationContext();
        context.setBaseCohort(new Cohort(cohort));
        return patientDataService.evaluate(definition, context).getData();
    }

    private Map<String, Cohort> getLostPatients(String cohort, String endDate) throws EvaluationException {
        Map<String, Cohort> l = new HashMap<String, Cohort>();
        String sql = "SELECT\n" +
                "  A.patient_id,\n" +
                "  CASE\n" +
                "  WHEN B.visit IS NULL\n" +
                "    THEN\n" +
                "      CASE\n" +
                "      WHEN DATEDIFF('2015-03-31', A.encounter) BETWEEN 8 AND 89\n" +
                "        THEN 'LOST'\n" +
                "      WHEN DATEDIFF('2015-03-31', A.encounter) >= 90\n" +
                "        THEN 'DROPPED'\n" +
                "      ELSE 'ACTIVE'\n" +
                "      END\n" +
                "  WHEN A.encounter >= B.visit AND B.visit IS NOT NULL\n" +
                "    THEN\n" +
                "      CASE\n" +
                "      WHEN DATEDIFF(A.encounter, B.visit) BETWEEN 8 AND 89\n" +
                "        THEN 'LOST'\n" +
                "      WHEN DATEDIFF(A.encounter, B.visit) >= 90\n" +
                "        THEN 'DROPPED'\n" +
                "      ELSE 'ACTIVE'\n" +
                "      END\n" +
                "  WHEN B.visit > A.encounter AND B.visit IS NOT NULL\n" +
                "    THEN\n" +
                "      CASE\n" +
                "      WHEN DATEDIFF('2015-03-31', B.visit) BETWEEN 8 AND 89\n" +
                "        THEN 'LOST'\n" +
                "      WHEN DATEDIFF('2015-03-31', B.visit) >= 90\n" +
                "        THEN 'DROPPED'\n" +
                "      ELSE 'ACTIVE'\n" +
                "      END\n" +
                "  END as status\n" +
                "FROM\n" +
                "  (SELECT\n" +
                "     patient_id,\n" +
                "     MAX(DATE(encounter_datetime)) AS encounter\n" +
                "   FROM encounter\n" +
                "   WHERE patient_id IN(1,2,3,4,5,6,7) AND encounter_datetime < '2015-03-31'\n" +
                "   GROUP BY patient_id) A\n" +
                "  LEFT JOIN\n" +
                "  (SELECT\n" +
                "     person_id,\n" +
                "     MAX(DATE(value_datetime)) AS visit\n" +
                "   FROM obs\n" +
                "   WHERE concept_id = 5096 AND person_id IN(1,2,3,4,5,6,7) AND value_datetime < '2015-03-31'\n" +
                "   GROUP BY person_id) B ON (A.patient_id = B.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT\n" +
                "     person_id,\n" +
                "     MIN(DATE(value_datetime)) AS nextVisit\n" +
                "   FROM obs\n" +
                "   WHERE concept_id = 5096 AND person_id IN(1,2,3,4,5,6,7) AND value_datetime >= '2015-03-31') C\n" +
                "    ON (A.patient_id = C.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT\n" +
                "     patient_id,\n" +
                "     MIN(DATE(encounter_datetime)) AS nextEncounter\n" +
                "   FROM encounter\n" +
                "   WHERE patient_id IN(1,2,3,4,5,6,7) AND encounter_datetime >= '2015-03-31'\n" +
                "   GROUP BY patient_id) D ON (A.patient_id = D.patient_id)";

        sql = sql.replace("2015-03-31", endDate).replace("1,2,3,4,5,6,7", cohort);
        SqlPatientDataDefinition definition = new SqlPatientDataDefinition();
        definition.setSql(sql);
        EvaluationContext context = new EvaluationContext();
        context.setBaseCohort(new Cohort(cohort));
        Set<Integer> lost = new HashSet<Integer>();
        Set<Integer> dropped = new HashSet<Integer>();
        Map<Integer, Object> data = patientDataService.evaluate(definition, context).getData();
        for (Map.Entry<Integer, Object> o : data.entrySet()) {
            if (String.valueOf(o.getValue()).contains("LOST")) {
                lost.add(o.getKey());
            }

            if (String.valueOf(o.getValue()).contains("DROPPED")) {
                dropped.add(o.getKey());
            }
        }

        Cohort lostPatients = new Cohort();
        lostPatients.setMemberIds(lost);
        Cohort droppedPatients = new Cohort();
        droppedPatients.setMemberIds(dropped);
        l.put("lost", lostPatients);
        l.put("dropped", droppedPatients);
        return l;
    }

    private Map<Integer, Object> getPatientBaselineCD4DataLS500(Map<Integer, Object> data) {
        Map<Integer, Object> result = new HashMap<Integer, Object>();
        for (Map.Entry<Integer, Object> o : data.entrySet()) {
            if (Double.valueOf(String.valueOf(o.getValue())) < 500.0) {
                result.put(o.getKey(), o.getValue());
            }
        }
        return result;
    }

    private Double getMedianCD4(Map<Integer, Object> data) {
        double[] medainData = new double[data.keySet().size()];
        int i = 0;
        for (Map.Entry<Integer, Object> o : data.entrySet()) {
            medainData[i++] = Double.valueOf(String.valueOf(o.getValue()));
        }

        if (medainData.length > 0) {
            return StatUtils.percentile(medainData, 50);
        }
        return null;
    }
}
