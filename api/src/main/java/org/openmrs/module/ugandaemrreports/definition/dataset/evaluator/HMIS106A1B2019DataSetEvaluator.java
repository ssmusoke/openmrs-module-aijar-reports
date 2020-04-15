package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.joda.time.LocalDate;
import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.DateObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.ObjectUtil;
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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.HMIS106A1B2019DataSetDefinition;
import org.openmrs.module.ugandaemrreports.library.CommonCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DecimalFormat;
import java.util.*;

/**
 */
@Handler(supports = {HMIS106A1B2019DataSetDefinition.class})
public class HMIS106A1B2019DataSetEvaluator implements DataSetEvaluator {
    @Autowired
    private EvaluationService evaluationService;
    @Autowired
    DataFactory df;
    @Autowired
    private CommonCohortDefinitionLibrary commonCohortDefinitionLibrary;
    @Autowired
    private HIVMetadata hivMetadata;
    @Autowired
    private PatientDataService patientDataService;

    String indicators[] = {"CA01.","HC02.","HC03."};
    DecimalFormat dft = new DecimalFormat("###.##");

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evaluationContext) throws EvaluationException {

        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, evaluationContext);
        HMIS106A1B2019DataSetDefinition definition = (HMIS106A1B2019DataSetDefinition) dataSetDefinition;

        String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");

        evaluationContext = ObjectUtil.nvl(evaluationContext, new EvaluationContext());

        PatientDataHelper pdh = new PatientDataHelper();

        LocalDate date = StubDate.dateOf(definition.getStartDate());
        List<LocalDate> q1 = Periods.subtractQuarters(date, 2);
        List<LocalDate> q2 = Periods.subtractQuarters(date, 4);
        List<LocalDate> q3 = Periods.subtractQuarters(date, 8);

        List<String> periods = new ArrayList<String>();

        periods.add(q1.get(0).toString("MMM") + " - " + q1.get(1).toString("MMM") + " " + q1.get(1).toString("yyyy"));
        periods.add(q2.get(0).toString("MMM") + " - " + q2.get(1).toString("MMM") + " " + q2.get(1).toString("yyyy"));
        periods.add(q3.get(0).toString("MMM") + " - " + q3.get(1).toString("MMM") + " " + q3.get(1).toString("yyyy"));

        List<String> quarters = new ArrayList<String>();


        quarters.add(String.valueOf(q1.get(0).getYear()) + "Q" + String.valueOf(((q1.get(1).getMonthOfYear() - 1) / 3) + 1));
        quarters.add(String.valueOf(q2.get(0).getYear()) + "Q" + String.valueOf(((q2.get(1).getMonthOfYear() - 1) / 3) + 1));
        quarters.add(String.valueOf(q3.get(0).getYear()) + "Q" + String.valueOf(((q3.get(1).getMonthOfYear() - 1) / 3) + 1));

        String query = "SELECT\n" +
                "     person_id,\n" +
                "     CONCAT_WS('', YEAR(value_datetime), 'Q', quarter(value_datetime)) AS q\n" +
                "   FROM obs\n" +
                "   WHERE concept_id = 99161 and voided = 0";
        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(query);

        List<Object[]> results = evaluationService.evaluateToList(q, evaluationContext);
        Multimap<String, Integer> finalData = convert(results);

        List<Integer> enrolledViaPMTCT = getPregnantAtArtStart(evaluationContext);

        CohortDefinition artTransferInRegimen = df.getPatientsWithConcept(hivMetadata.getArtRegimenTransferInDate(), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition artTransferInRegimenOther = df.getPatientsWithConcept(hivMetadata.getOtherArtTransferInRegimen(), BaseObsCohortDefinition.TimeModifier.ANY);

        CohortDefinition agedBetween0and9 = commonCohortDefinitionLibrary.agedAtMost(9);
        CohortDefinition agedBetween10and19 = commonCohortDefinitionLibrary.agedBetween(10,19);
        CohortDefinition aged20AndAbove = commonCohortDefinitionLibrary.agedAtLeast(20);

        Cohort agedBetween0and9Patients = Context.getService(CohortDefinitionService.class).evaluate(agedBetween0and9, null);
        Cohort agedBetween10and19Patients = Context.getService(CohortDefinitionService.class).evaluate(agedBetween10and19, null);
        Cohort aged20AndAbovePatients = Context.getService(CohortDefinitionService.class).evaluate(aged20AndAbove, null);

        List<Cohort> ageCohorts = new ArrayList<>();
        ageCohorts.add(agedBetween0and9Patients);
        ageCohorts.add(agedBetween10and19Patients);
        ageCohorts.add(aged20AndAbovePatients);

        DateObsCohortDefinition artTransferInDate = new DateObsCohortDefinition();
        artTransferInDate.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        artTransferInDate.setQuestion(hivMetadata.getArtRegimenTransferInDate());

        CohortDefinition artTransferIn = df.getPatientsInAny(artTransferInRegimen, artTransferInRegimenOther, artTransferInDate);

        Cohort transferInPatients = Context.getService(CohortDefinitionService.class).evaluate(artTransferIn, null);

        Map<Integer, Object> baselineCD4 = new HashMap<Integer, Object>();
        Map<Integer, Object> baselineCD4Mothers = new HashMap<Integer, Object>();


       DataSetRow all = new DataSetRow();
        for (int i = 0; i < periods.size(); i++) {

            String col = indicators[i];

            Set<Integer> allStarted = new HashSet<Integer>(finalData.get(quarters.get(i)));


            Collection allMothers = CollectionUtils.intersection(allStarted, enrolledViaPMTCT);

            pdh.addCol(all, col+"2A", periods.get(i));
            pdh.addCol(all, col+"2B", periods.get(i));
            pdh.addCol(all, col+"2C", periods.get(i));

            pdh.addCol(all, col+"2D", periods.get(i));

            if (allStarted.size() > 0) {
                Collection startedArtAtThisFacility = CollectionUtils.subtract(allStarted, transferInPatients.getMemberIds());
                Collection transferIn = CollectionUtils.intersection(allStarted, transferInPatients.getMemberIds());

                Map<Integer, Object> cD4L200 = new HashMap<Integer, Object>();
                Map<Integer, Object> transferOut = new HashMap<Integer, Object>();
                Map<Integer, Object> dead = new HashMap<Integer, Object>();
                Map<String, Cohort> lost = new HashMap<String, Cohort>();
                Set<Integer> stopped = new HashSet<Integer>();


                if (startedArtAtThisFacility.size() > 0) {
                    baselineCD4 = getPatientBaselineCD4Data(Joiner.on(",").join(allStarted));
                    cD4L200 = getPatientBaselineCD4DataLS200(baselineCD4);
                    transferOut = getPatientTransferredOut(Joiner.on(",").join(startedArtAtThisFacility), endDate);

                }

                Collection net = CollectionUtils.subtract(allStarted, transferOut.keySet());

                if (net.size() > 0) {
                    lost = getLostPatients(Joiner.on(",").join(net), endDate);
                    stopped = getPatientStopped(Joiner.on(",").join(net), endDate);
                    dead = getDeadPatients(Joiner.on(",").join(net), endDate);
                }


                Cohort patientsLost = lost.get("lost");
                Cohort patientsDropped = lost.get("dropped");

                Collection allLostAndDied = CollectionUtils.union(CollectionUtils.union(stopped, dead.keySet()), CollectionUtils.union(patientsLost.getMemberIds(), patientsDropped.getMemberIds()));

                Collection alive = CollectionUtils.subtract(net, allLostAndDied);



                addAgeCohortIndicators(pdh,indicators[i],3,all,startedArtAtThisFacility,ageCohorts);

                addAgeCohortIndicatorsWithBaseFraction(pdh,indicators[i],4,all,cD4L200,baselineCD4,ageCohorts);
                addAgeCohortIndicatorsWithMedian(pdh,indicators[i],5,all,baselineCD4,ageCohorts);

                addAgeCohortIndicators(pdh,indicators[i],6,all,transferIn,ageCohorts);

                addAgeCohortIndicators(pdh,indicators[i],7,all,getPatientTransferredOut(Joiner.on(",").join(startedArtAtThisFacility), endDate),ageCohorts);

                addAgeCohortIndicators(pdh,indicators[i],8,all,net,ageCohorts);
                addAgeCohortIndicators(pdh,indicators[i],9,all,stopped,ageCohorts);
                addAgeCohortIndicators(pdh,indicators[i],10,all,dead,ageCohorts);

                addAgeCohortIndicators(pdh,indicators[i],11,all,patientsLost,ageCohorts);

                addAgeCohortIndicators(pdh,indicators[i],12,all,patientsDropped,ageCohorts);

                addAgeCohortIndicators(pdh,indicators[i],13,all,alive,ageCohorts);

                addAgeCohortIndicatorsWithPercentage(pdh,indicators[i],14,all,alive,net,ageCohorts);

            } else {
                pdh.addCol(all, indicators[i]+String.valueOf(3)+"A", 0);
                pdh.addCol(all, indicators[i]+String.valueOf(3)+"B",0);
                pdh.addCol(all, indicators[i]+String.valueOf(3)+"C", 0);

                pdh.addCol(all, indicators[i]+String.valueOf(4)+"A", "-");
                pdh.addCol(all, indicators[i]+String.valueOf(4)+"B","-");
                pdh.addCol(all, indicators[i]+String.valueOf(4)+"C", "-");

                pdh.addCol(all, indicators[i]+String.valueOf(12)+"A", 0);
                pdh.addCol(all, indicators[i]+String.valueOf(12)+"B",0);
                pdh.addCol(all, indicators[i]+String.valueOf(12)+"C", 0);

                pdh.addCol(all, indicators[i]+String.valueOf(5)+"A", "-");
                pdh.addCol(all, indicators[i]+String.valueOf(5)+"B","-");
                pdh.addCol(all, indicators[i]+String.valueOf(5)+"C", "-");

                pdh.addCol(all, indicators[i]+String.valueOf(6)+"A", 0);
                pdh.addCol(all, indicators[i]+String.valueOf(6)+"B",0);
                pdh.addCol(all, indicators[i]+String.valueOf(6)+"C", 0);

                pdh.addCol(all, indicators[i]+String.valueOf(7)+"A", 0);
                pdh.addCol(all, indicators[i]+String.valueOf(7)+"B",0);
                pdh.addCol(all, indicators[i]+String.valueOf(7)+"C", 0);

                pdh.addCol(all, indicators[i]+String.valueOf(8)+"A", 0);
                pdh.addCol(all, indicators[i]+String.valueOf(8)+"B",0);
                pdh.addCol(all, indicators[i]+String.valueOf(8)+"C", 0);

                pdh.addCol(all, indicators[i]+String.valueOf(9)+"A", 0);
                pdh.addCol(all, indicators[i]+String.valueOf(9)+"B",0);
                pdh.addCol(all, indicators[i]+String.valueOf(9)+"C", 0);

                pdh.addCol(all, indicators[i]+String.valueOf(10)+"A", 0);
                pdh.addCol(all, indicators[i]+String.valueOf(10)+"B",0);
                pdh.addCol(all, indicators[i]+String.valueOf(10)+"C", 0);

                pdh.addCol(all, indicators[i]+String.valueOf(11)+"A", 0);
                pdh.addCol(all, indicators[i]+String.valueOf(11)+"B",0);
                pdh.addCol(all, indicators[i]+String.valueOf(11)+"C", 0);

                pdh.addCol(all, indicators[i]+String.valueOf(13)+"A", 0);
                pdh.addCol(all, indicators[i]+String.valueOf(13)+"B",0);
                pdh.addCol(all, indicators[i]+String.valueOf(13)+"C", 0);

                pdh.addCol(all, indicators[i]+String.valueOf(14)+"A", "-");
                pdh.addCol(all, indicators[i]+String.valueOf(14)+"B","-");
                pdh.addCol(all, indicators[i]+String.valueOf(14)+"C", "-");


            }

            if (allMothers.size() > 0) {
                Collection allMotherStartedAtThisFacility = CollectionUtils.subtract(allMothers, transferInPatients.getMemberIds());
                Collection mothersTransferIn = CollectionUtils.intersection(allMothers, transferInPatients.getMemberIds());


                Map<Integer, Object> mothersCD4L200 = new HashMap<Integer, Object>();
                Map<Integer, Object> transferOutMothers = new HashMap<Integer, Object>();
                Map<Integer, Object> deadMothers = new HashMap<Integer, Object>();
                Map<String, Cohort> lostMothers = new HashMap<String, Cohort>();
                Set<Integer> stoppedMothers = new HashSet<Integer>();


                if (allMotherStartedAtThisFacility.size() > 0) {
                    baselineCD4Mothers = getPatientBaselineCD4Data(Joiner.on(",").join(allMothers));
                    mothersCD4L200 = getPatientBaselineCD4DataLS200(baselineCD4Mothers);
                    transferOutMothers = getPatientTransferredOut(Joiner.on(",").join(allMotherStartedAtThisFacility), endDate);

                }
                Collection netMothers = CollectionUtils.subtract(allMothers, transferOutMothers.keySet());

                if (netMothers.size() > 0) {
                    lostMothers = getLostPatients(Joiner.on(",").join(netMothers), endDate);
                    stoppedMothers = getPatientStopped(Joiner.on(",").join(netMothers), endDate);
                    deadMothers = getDeadPatients(Joiner.on(",").join(netMothers), endDate);
                }


                Cohort patientsLost = lostMothers.get("lost");
                Cohort patientsDropped = lostMothers.get("dropped");

                Collection allLostAndDied = CollectionUtils.union(CollectionUtils.union(stoppedMothers, deadMothers.keySet()), CollectionUtils.union(patientsLost.getMemberIds(), patientsDropped.getMemberIds()));

                Collection alive = CollectionUtils.subtract(netMothers, allLostAndDied);


                pdh.addCol(all, indicators[i]+"3D", allMotherStartedAtThisFacility.size());
                pdh.addCol(all, indicators[i]+"6D", mothersTransferIn.size());

                pdh.addCol(all, indicators[i]+"4D", dft.format(((double) mothersCD4L200.size()) / baselineCD4Mothers.size()));
                pdh.addCol(all, indicators[i]+"5D", getMedianCD4(baselineCD4Mothers));
                pdh.addCol(all, indicators[i]+"7D", transferOutMothers.size());
                pdh.addCol(all, indicators[i]+"8D", netMothers.size());
                pdh.addCol(all, indicators[i]+"9D", getPatientStopped(Joiner.on(",").join(netMothers), endDate));
                pdh.addCol(all, indicators[i]+"10D", getDeadPatients(Joiner.on(",").join(netMothers), endDate).size());
                pdh.addCol(all, indicators[i]+"11D", lostMothers.get("lost").getSize());
                pdh.addCol(all, indicators[i]+"12D", lostMothers.get("dropped").getSize());
                pdh.addCol(all, indicators[i]+"13D", alive.size());
                pdh.addCol(all, indicators[i]+"14D", dft.format((alive.size() * 100.00) / netMothers.size()));

            } else {
                pdh.addCol(all, indicators[i]+"3D", 0);
                pdh.addCol(all, indicators[i]+"6D",0);

                pdh.addCol(all, indicators[i]+"4D", "-");
                pdh.addCol(all, indicators[i]+"5D", "-");
                pdh.addCol(all, indicators[i]+"7D", 0);
                pdh.addCol(all, indicators[i]+"8D", 0);
                pdh.addCol(all, indicators[i]+"9D", 0);
                pdh.addCol(all, indicators[i]+"10D", 0);
                pdh.addCol(all, indicators[i]+"11D", 0);
                pdh.addCol(all, indicators[i]+"12D",0);
                pdh.addCol(all, indicators[i]+"13D", 0);
                pdh.addCol(all, indicators[i]+"14D", "-");
            }


        }
        dataSet.addRow(all);


        return dataSet;
    }

    private void addAgeCohortIndicators(PatientDataHelper pdh,String indicatorLabel,Integer indicatorNo, DataSetRow dataSetRow,Collection collection,List<Cohort> cohorts) {
            pdh.addCol(dataSetRow, indicatorLabel + String.valueOf(indicatorNo) + "A", CollectionUtils.intersection(collection, cohorts.get(0).getMemberIds()).size());
            pdh.addCol(dataSetRow, indicatorLabel + String.valueOf(indicatorNo) + "B", CollectionUtils.intersection(collection, cohorts.get(1).getMemberIds()).size());
            pdh.addCol(dataSetRow, indicatorLabel + String.valueOf(indicatorNo) + "C", CollectionUtils.intersection(collection, cohorts.get(2).getMemberIds()).size());
    }

    private void addAgeCohortIndicators(PatientDataHelper pdh,String indicatorLabel,Integer indicatorNo, DataSetRow dataSetRow,Cohort cohort,List<Cohort> cohorts) {
            pdh.addCol(dataSetRow, indicatorLabel + String.valueOf(indicatorNo) + "A", CollectionUtils.intersection(cohort.getMemberIds(), cohorts.get(0).getMemberIds()).size());
            pdh.addCol(dataSetRow, indicatorLabel + String.valueOf(indicatorNo) + "B", CollectionUtils.intersection(cohort.getMemberIds(), cohorts.get(1).getMemberIds()).size());
            pdh.addCol(dataSetRow, indicatorLabel + String.valueOf(indicatorNo) + "C", CollectionUtils.intersection(cohort.getMemberIds(), cohorts.get(2).getMemberIds()).size());
    }

    private void addAgeCohortIndicators(PatientDataHelper pdh,String indicatorLabel,Integer indicatorNo, DataSetRow dataSetRow,Map<Integer,Object> map,List<Cohort> cohorts) {
            pdh.addCol(dataSetRow, indicatorLabel + String.valueOf(indicatorNo) + "A", CollectionUtils.intersection(map.keySet(), cohorts.get(0).getMemberIds()).size());
            pdh.addCol(dataSetRow, indicatorLabel + String.valueOf(indicatorNo) + "B", CollectionUtils.intersection(map.keySet(), cohorts.get(1).getMemberIds()).size());
            pdh.addCol(dataSetRow, indicatorLabel + String.valueOf(indicatorNo) + "C", CollectionUtils.intersection(map.keySet(), cohorts.get(2).getMemberIds()).size());
    }
    private void addAgeCohortIndicatorsWithMedian(PatientDataHelper pdh,String indicatorLabel,Integer indicatorNo, DataSetRow dataSetRow,Map<Integer,Object> map,List<Cohort> cohorts) {
        String[] parameters = {"A","B","C"};
           for (int i = 0; i < 3; i++) {
           Set<Integer> ageCohort = cohorts.get(i).getMemberIds();
           Map<Integer,Object> cohortMembers = new HashMap<>();
           Collection<Object>commonMembers = CollectionUtils.intersection(map.keySet(),ageCohort);
           if(commonMembers.size()>0) {
               for (Object integer : commonMembers) {
                       cohortMembers.put((int)integer, map.get((int)integer));
               }
               pdh.addCol(dataSetRow, indicatorLabel + String.valueOf(indicatorNo) + parameters[i],getMedianCD4(cohortMembers));
           }else{
               pdh.addCol(dataSetRow, indicatorLabel + String.valueOf(indicatorNo) + parameters[i],"-");
           }
            }
    }

    private void addAgeCohortIndicatorsWithBaseFraction(PatientDataHelper pdh,String indicatorLabel,Integer indicatorNo, DataSetRow dataSetRow,Map<Integer,Object> CD4,Map<Integer,Object> baseLineCD4,List<Cohort> cohorts) {
        String[] parameters = {"A","B","C"};
        for (int x = 0; x < 3; x++)
        {
            for (int i = 0; i < 3; i++) {
                Set<Integer> ageCohort = cohorts.get(i).getMemberIds();
                Collection cd4L250 = CollectionUtils.intersection(CD4.keySet(),ageCohort);
                Collection baselineCD4 = CollectionUtils.intersection( baseLineCD4.keySet(),ageCohort);
                if(baselineCD4.size()>0) {
                    pdh.addCol(dataSetRow, indicatorLabel + String.valueOf(indicatorNo) + parameters[i], dft.format(((double) cd4L250.size())/baseLineCD4.size()));
                }else{
                    pdh.addCol(dataSetRow, indicatorLabel + String.valueOf(indicatorNo) + parameters[i],"-");
                }
            }
        }
    }

    private void addAgeCohortIndicatorsWithPercentage(PatientDataHelper pdh,String indicatorLabel,Integer indicatorNo, DataSetRow dataSetRow,Collection alive,Collection net,List<Cohort> cohorts) {
        String[] parameters = {"A","B","C"};
        for (int x = 0; x < 3; x++)
        {
            for (int i = 0; i < 3; i++) {
                Set<Integer> ageCohort = cohorts.get(i).getMemberIds();
                Collection aliveMembers = CollectionUtils.intersection(alive,ageCohort);
                Collection netMembers = CollectionUtils.intersection( net,ageCohort);
                if(netMembers.size()>0) {
                    pdh.addCol(dataSetRow, indicatorLabel + String.valueOf(indicatorNo) + parameters[i],  dft.format((aliveMembers.size() * 100.00) / netMembers.size()));
                }else{
                    pdh.addCol(dataSetRow, indicatorLabel + String.valueOf(indicatorNo) + parameters[i],"-");
                }
            }
        }
    }

    private Map<Integer, Object> getPatientBaselineCD4Data(String cohort) throws EvaluationException {
        String sql = String.format("select o.person_id, o.value_numeric from obs o where o.voided = 0 and o.concept_id = 99071 and person_id in (select o.person_id from obs o inner join person p using(person_id) where o.concept_id = 99161 and o.voided = 0 and YEAR(o.value_datetime) - YEAR(p.birthdate) - (RIGHT(o.value_datetime, 5) < RIGHT(p.birthdate, 5)) > 5 and p.person_id in(%s))", cohort);
        SqlPatientDataDefinition definition = new SqlPatientDataDefinition();
        definition.setSql(sql);
        EvaluationContext context = new EvaluationContext();
        context.setBaseCohort(new Cohort(cohort));
        return patientDataService.evaluate(definition, context).getData();
    }


    private Map<Integer, Object> getPatientTransferredOut(String cohort, String endDate) throws EvaluationException {
        String sql = String.format("select o.person_id, o.value_datetime from obs o where o.voided = 0 and o.person_id in (%s) and o.concept_id = 99165 and o.value_datetime <= '%s'", cohort, endDate);
        SqlPatientDataDefinition definition = new SqlPatientDataDefinition();
        definition.setSql(sql);
        EvaluationContext context = new EvaluationContext();
        context.setBaseCohort(new Cohort(cohort));
        return patientDataService.evaluate(definition, context).getData();
    }

    private List<Integer> getPregnantAtArtStart(EvaluationContext evaluationContext) throws EvaluationException {
        String sql = "select person_id from (select person_id,value_coded from obs where concept_id = 99072 and value_coded = 90003 and voided = 0 group by person_id union all select person_id,value_coded from obs where concept_id = 99603 and value_coded = 90003 and voided = 0 group by person_id) A group by person_id";
        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(sql);
        return evaluationService.evaluateToList(q, Integer.class, evaluationContext);
    }

    private Set<Integer> getPatientStopped(String cohort, String endDate) throws EvaluationException {
        String sqlStopped = String.format("select person_id, MAX(DATE(o.value_datetime)) from obs o where o.voided = 0 and o.person_id in (%s) and o.concept_id = 99084 and o.value_datetime <= '%s' group by person_id", cohort, endDate);
        String sqlRestarted = String.format("select person_id, MAX(DATE(o.value_datetime)) from obs o where o.voided = 0 and o.person_id in (%s) and o.concept_id = 99085 and o.value_datetime <= '%s' group by person_id", cohort, endDate);
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
                String.format("  (select person_id,Date(value_datetime) as death_date from obs where voided = 0 and concept_id = 90272 and person_id in(%s) and value_datetime < '%s'\n", cohort, endDate) +
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
                "   WHERE patient_id IN(1,2,3,4,5,6,7) AND voided = 0 AND encounter_datetime < '2015-03-31'\n" +
                "   GROUP BY patient_id) A\n" +
                "  LEFT JOIN\n" +
                "  (SELECT\n" +
                "     person_id,\n" +
                "     MAX(DATE(value_datetime)) AS visit\n" +
                "   FROM obs\n" +
                "   WHERE concept_id = 5096 AND voided = 0 AND person_id IN(1,2,3,4,5,6,7) AND value_datetime < '2015-03-31'\n" +
                "   GROUP BY person_id) B ON (A.patient_id = B.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT\n" +
                "     person_id,\n" +
                "     MIN(DATE(value_datetime)) AS nextVisit\n" +
                "   FROM obs\n" +
                "   WHERE concept_id = 5096 AND voided = 0 AND person_id IN(1,2,3,4,5,6,7) AND value_datetime >= '2015-03-31') C\n" +
                "    ON (A.patient_id = C.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT\n" +
                "     patient_id,\n" +
                "     MIN(DATE(encounter_datetime)) AS nextEncounter\n" +
                "   FROM encounter\n" +
                "   WHERE patient_id IN(1,2,3,4,5,6,7) AND voided = 0 AND encounter_datetime >= '2015-03-31'\n" +
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

    private Map<Integer, Object> getPatientBaselineCD4DataLS200(Map<Integer, Object> data) {
        Map<Integer, Object> result = new HashMap<Integer, Object>();
        for (Map.Entry<Integer, Object> o : data.entrySet()) {
            if (Double.valueOf(String.valueOf(o.getValue())) <= 200.0) {
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

    private Multimap<String, Integer> convert(List<Object[]> results) {
        Multimap<String, Integer> myMultimap = ArrayListMultimap.create();

        for (Object[] patient : results) {
            myMultimap.put(String.valueOf(patient[1]), Integer.valueOf(String.valueOf(patient[0])));
        }
        return myMultimap;
    }
}