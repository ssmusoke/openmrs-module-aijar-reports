package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.TransformerUtils;
import org.joda.time.Years;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.service.PatientDataService;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.common.PatientNonSuppressingData;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.common.ViralLoad;
import org.openmrs.module.ugandaemrreports.definition.data.definition.NonSuppressingPatientDataDefinition;
import org.openmrs.module.ugandaemrreports.definition.data.definition.ViralLoadCohortDataDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.ViralLoadDatasetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.predicates.NonSuppressedDataFilter;
import org.openmrs.module.ugandaemrreports.definition.dataset.predicates.ViralLoadNotDetectedFilter;
import org.openmrs.module.ugandaemrreports.definition.dataset.predicates.ViralLoadTestedFilter;
import org.openmrs.module.ugandaemrreports.library.CommonCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Handler(supports = {ViralLoadDatasetDefinition.class})
public class ViralLoadDataSetEvaluator implements org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator {
    @Autowired
    private CohortDefinitionService cohortDefinitionService;
    @Autowired
    private HIVMetadata hivMetadata;
    @Autowired
    private CommonCohortDefinitionLibrary commonCohortDefinitionLibrary;
    @Autowired
    private EvaluationService evaluationService;
    PatientDataHelper pdh = new PatientDataHelper();

    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evaluationContext)
            throws org.openmrs.module.reporting.evaluation.EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, evaluationContext);
        ViralLoadDatasetDefinition definition = (ViralLoadDatasetDefinition) dataSetDefinition;

        Date startDate = definition.getStartDate();
        Date endDate = definition.getEndDate();

        evaluationContext = ObjectUtil.nvl(evaluationContext, new EvaluationContext());

        ViralLoadCohortDataDefinition viralLoadCohortDataDefinition = new ViralLoadCohortDataDefinition();
        viralLoadCohortDataDefinition.setEndDate(endDate);
        viralLoadCohortDataDefinition.setStartDate(startDate);

        NonSuppressingPatientDataDefinition nonSuppressingPatientDataDefinition = new NonSuppressingPatientDataDefinition();
        nonSuppressingPatientDataDefinition.setEndDate(endDate);
        nonSuppressingPatientDataDefinition.setStartDate(startDate);

        EvaluatedPatientData data = Context.getService(PatientDataService.class).evaluate(viralLoadCohortDataDefinition, evaluationContext);

        EvaluatedPatientData nonSuppressingData = Context.getService(PatientDataService.class).evaluate(nonSuppressingPatientDataDefinition, evaluationContext);


        Collection<ViralLoad> viralLoads = convertDataToObjects(data.getData());
        Collection<PatientNonSuppressingData> nonSuppressed = convertNonSuppressingDataToObjects(nonSuppressingData.getData());

        Map<String, Map<String, Integer>> quartersToSubtract = new HashMap<String, Map<String, Integer>>();

        Map<String, Integer> sixMonths = ImmutableMap.of("from", 6, "to", 8);
        Map<String, Integer> twelveMonths = ImmutableMap.of("from", 12, "to", 14);
        Map<String, Integer> twenty4Months = ImmutableMap.of("from", 24, "to", 26);
        Map<String, Integer> thirty6Months = ImmutableMap.of("from", 36, "to", 38);
        Map<String, Integer> forty8Months = ImmutableMap.of("from", 48, "to", 50);
        Map<String, Integer> sixtyMonths = ImmutableMap.of("from", 60, "to", 62);
        Map<String, Integer> seventy2Months = ImmutableMap.of("from", 72, "to", 74);

        List<String> otherColumnLabels = Arrays.asList("no", "dataElement");


        Map<String, String> columns = new HashMap<String, String>();
        columns.put("h", "No of non-suppressed clients who have completed IAC");
        columns.put("i", "No of non-suppressed clients who have completed IAC, repeat test done and results received for VL (subset of 4)");
        columns.put("j", "No of non-suppressed clients who have completed IAC, tested and achieved VL suppression (subset of 5)");
        columns.put("k", "Number of clients who switched regimens during the reporting period");

        quartersToSubtract.put("a", sixMonths);
        quartersToSubtract.put("b", twelveMonths);
        quartersToSubtract.put("c", twenty4Months);
        quartersToSubtract.put("d", thirty6Months);
        quartersToSubtract.put("e", forty8Months);
        quartersToSubtract.put("f", sixtyMonths);
        quartersToSubtract.put("g", seventy2Months);


        CodedObsCohortDefinition entryThroughPMTCT = new CodedObsCohortDefinition();
        entryThroughPMTCT.setQuestion(this.hivMetadata.getEntryPoint());
        entryThroughPMTCT.setTimeModifier(org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition.TimeModifier.ANY);
        entryThroughPMTCT.setOperator(org.openmrs.module.reporting.common.SetComparator.IN);
        entryThroughPMTCT.setValueList(Collections.singletonList(this.hivMetadata.getEMTCTAtEnrollment()));

        Cohort enrolledViaPMTCT = this.cohortDefinitionService.evaluate(entryThroughPMTCT, null);

        Cohort males = this.cohortDefinitionService.evaluate(this.commonCohortDefinitionLibrary.males(), null);

        Cohort females = this.cohortDefinitionService.evaluate(this.commonCohortDefinitionLibrary.females(), null);


        Cohort below2Years = this.cohortDefinitionService.evaluate(this.commonCohortDefinitionLibrary.agedAtMost(2, endDate), null);
        Cohort between2And5Years = this.cohortDefinitionService.evaluate(this.commonCohortDefinitionLibrary.agedBetween(2, 4, endDate), null);
        Cohort between5And10Years = this.cohortDefinitionService.evaluate(this.commonCohortDefinitionLibrary.agedBetween(5, 9, endDate), null);
        Cohort between10And15Years = this.cohortDefinitionService.evaluate(this.commonCohortDefinitionLibrary.agedBetween(10, 14, endDate), null);
        Cohort between15And20Years = this.cohortDefinitionService.evaluate(this.commonCohortDefinitionLibrary.agedBetween(15, 19, endDate), null);
        Cohort between20And25Years = this.cohortDefinitionService.evaluate(this.commonCohortDefinitionLibrary.agedBetween(20, 24, endDate), null);
        Cohort between25And49Years = this.cohortDefinitionService.evaluate(this.commonCohortDefinitionLibrary.agedBetween(25, 49, endDate), null);
        Cohort above50Years = this.cohortDefinitionService.evaluate(this.commonCohortDefinitionLibrary.agedAtLeast(50, endDate), null);

        Map<String, List<Cohort>> aggregates = new HashMap<String, List<Cohort>>();

        aggregates.put("a", Arrays.asList(below2Years, males));
        aggregates.put("b", Arrays.asList(below2Years, females));

        aggregates.put("c", Arrays.asList(between2And5Years, males));
        aggregates.put("d", Arrays.asList(between2And5Years, females));

        aggregates.put("e", Arrays.asList(between5And10Years, males));
        aggregates.put("f", Arrays.asList(between5And10Years, females));

        aggregates.put("g", Arrays.asList(between10And15Years, males));
        aggregates.put("h", Arrays.asList(between10And15Years, females));

        aggregates.put("i", Arrays.asList(between15And20Years, males));
        aggregates.put("j", Arrays.asList(between15And20Years, females));

        aggregates.put("k", Arrays.asList(between20And25Years, males));
        aggregates.put("l", Arrays.asList(between20And25Years, females));

        aggregates.put("m", Arrays.asList(between25And49Years, males));
        aggregates.put("n", Arrays.asList(between25And49Years, females));

        aggregates.put("o", Arrays.asList(above50Years, males));
        aggregates.put("p", Arrays.asList(above50Years, females));

        aggregates.put("q", Collections.singletonList(males));
        aggregates.put("r", Collections.singletonList(females));

        aggregates.put("s", Arrays.asList(females, enrolledViaPMTCT));

        Integer i = 1;
        for (Map.Entry<String, Map<String, Integer>> o : quartersToSubtract.entrySet()) {
            Map<String, Integer> val = o.getValue();
            final Integer start = val.get("from");
            final Integer end = val.get("to");

            Collection<ViralLoad> tested = Collections2.filter(viralLoads, new ViralLoadTestedFilter(start, end));
            Collection<ViralLoad> notDetected = Collections2.filter(tested, new ViralLoadNotDetectedFilter(start, end));

            Cohort testedCohort = getPatients(tested);
            Cohort suppressed = getPatients(notDetected);
            DataSetRow dataSetRow = joinDatasetRows(disaggregation(testedCohort, aggregates, ""), disaggregation(suppressed, aggregates, "1"));
            dataSetRow = addOtherColumns(dataSetRow, otherColumnLabels, Arrays.asList(String.valueOf(i), String.valueOf(start) + " Months Cohort"));
            dataSet.addRow(dataSetRow);
            if (i == 1) {
                DataSetRow emptyRow = new DataSetRow();
                this.pdh.addCol(emptyRow, "no", "MOST RECENT VL DONE  IN THE RESPECTIVE ART REGISTER COHORTS");
                dataSet.addRow(emptyRow);
            }
            i += 1;
        }

        DataSetRow anotherEmptyRow = new DataSetRow();
        this.pdh.addCol(anotherEmptyRow, "no", "Repeat VL for the non-suppressed");
        dataSet.addRow(anotherEmptyRow);

        Map<String, List<Cohort>> c = getNonSuppressedCohorts(nonSuppressed, startDate, endDate);

        for (Map.Entry<String, List<Cohort>> o : c.entrySet()) {
            DataSetRow ds = joinDatasetRows(disaggregation(o.getValue().get(0), aggregates, ""), disaggregation(o.getValue().get(1), aggregates, "1"));
            ds = addOtherColumns(ds, otherColumnLabels, Arrays.asList(String.valueOf(i), columns.get(o.getKey())));
            dataSet.addRow(ds);
            i += 1;
        }

        DataSetRow dataSetRow12 = new DataSetRow();
        DataSetRow dataSetRow13 = new DataSetRow();
        DataSetRow anotherEmptyRow1 = new DataSetRow();

        this.pdh.addCol(anotherEmptyRow1, "no", "");
        dataSet.addRow(anotherEmptyRow1);

        this.pdh.addCol(dataSetRow12, "no", "12");
        this.pdh.addCol(dataSetRow12, "dataElement", "No of VL samples dispatched from the facility within the reporting period");
        this.pdh.addCol(dataSetRow12, "a", "");

        this.pdh.addCol(dataSetRow13, "no", "13");
        this.pdh.addCol(dataSetRow13, "dataElement", "No. of dispatched samples whose results were returned to the facility within 14 days");
        this.pdh.addCol(dataSetRow13, "a", "");

        dataSet.addRow(dataSetRow12);
        dataSet.addRow(dataSetRow13);

        return dataSet;
    }

    private DataSetRow disaggregation(Cohort baseCohort, Map<String, List<Cohort>> cohorts, String addToKey) {
        DataSetRow row = new DataSetRow();
        for (Map.Entry<String, List<Cohort>> cohort : cohorts.entrySet()) {
            Collection baseMembers = baseCohort.getMemberIds();
            String key = cohort.getKey();
            List<Cohort> newCohortList = cohort.getValue();
            for (Cohort found : newCohortList) {
                baseMembers = CollectionUtils.intersection(baseMembers, found.getMemberIds());
            }
            this.pdh.addCol(row, key + addToKey, baseMembers.size());
        }
        return row;
    }

    private DataSetRow addOtherColumns(DataSetRow dataSetRow, List<String> otherColumnLabels, List<String> otherColumnValues) {

        Iterator<String> otherColumnValuesIterator = otherColumnValues.iterator();
        Iterator<String> otherColumnsIterator = otherColumnLabels.iterator();

        Map<String, String> otherColumns = new HashMap<String, String>();

        while ((otherColumnsIterator.hasNext()) && (otherColumnValuesIterator.hasNext())) {
            otherColumns.put(otherColumnsIterator.next(), otherColumnValuesIterator.next());
        }

        for (Map.Entry<String, String> o : otherColumns.entrySet()) {
            this.pdh.addCol(dataSetRow, o.getKey(), o.getValue());
        }
        return dataSetRow;
    }

    private Cohort getPatients(Collection<ViralLoad> results) {
        Cohort result = new Cohort();
        Set<Integer> patients = new HashSet<Integer>();
        for (ViralLoad r : results) {
            patients.add(r.getPatientId());
        }
        result.setMemberIds(patients);
        return result;
    }

    private Collection<ViralLoad> convertDataToObjects(Map<Integer, Object> hashedMap) {
        Collection<ViralLoad> viralLoads = new ArrayList<ViralLoad>();
        for (Map.Entry<Integer, Object> o : hashedMap.entrySet()) {
            viralLoads.add((ViralLoad) o.getValue());
        }

        return viralLoads;
    }

    private Collection<PatientNonSuppressingData> convertNonSuppressingDataToObjects(Map<Integer, Object> hashedMap) {
        Collection<PatientNonSuppressingData> viralLoads = new ArrayList<PatientNonSuppressingData>();
        for (Map.Entry<Integer, Object> o : hashedMap.entrySet()) {
            viralLoads.add((PatientNonSuppressingData) o.getValue());
        }

        return viralLoads;
    }

    private Map<String, List<Cohort>> getNonSuppressedCohorts(Collection<PatientNonSuppressingData> patientNonSuppressingData, Date startDate, Date endDate) {

        Date defaultDate = DateUtil.parseDate("1900-01-01", "yyyy-MM-dd");
        Integer defaultInteger = -1;
        Double defaultDouble = -1.0;

        Set<Integer> indicator1 = new HashSet<Integer>();
        Set<Integer> indicator2 = new HashSet<Integer>();

        Set<Integer> indicator3 = new HashSet<Integer>();
        Set<Integer> indicator4 = new HashSet<Integer>();

        Set<Integer> indicator5 = new HashSet<Integer>();
        Set<Integer> indicator6 = new HashSet<Integer>();

        Set<Integer> indicator7 = new HashSet<Integer>();
        Set<Integer> indicator8 = new HashSet<Integer>();

        Map<String, List<Cohort>> cohorts = new HashMap<String, List<Cohort>>();

        Collection<Integer> secondLineDrugsChildren = CollectionUtils.collect(hivMetadata.getSecondLineDrugsChildren(), new BeanToPropertyValueTransformer("conceptId"));
        Collection<Integer> secondLineDrugsAdults = CollectionUtils.collect(hivMetadata.getSecondLineDrugsAdults(), new BeanToPropertyValueTransformer("conceptId"));
        Collection<Integer> firstLineDrugsChildren = CollectionUtils.collect(hivMetadata.getFirstLineDrugsChildren(), new BeanToPropertyValueTransformer("conceptId"));
        Collection<Integer> firstLineDrugsAdults = CollectionUtils.collect(hivMetadata.getSecondLineDrugsAdults(), new BeanToPropertyValueTransformer("conceptId"));

        Integer firstLine = 1;

        Multimap<Integer, PatientNonSuppressingData> grouped = Multimaps.index(patientNonSuppressingData,
                new Function<PatientNonSuppressingData, Integer>() {
                    @Override
                    public Integer apply(PatientNonSuppressingData item) {
                        return item.getPatientId();
                    }
                });


        for (Integer patientId : grouped.keySet()) {
            Collection<PatientNonSuppressingData> patientData = grouped.get(patientId);
            PatientNonSuppressingData data = new ArrayList<PatientNonSuppressingData>(patientData).get(0);

            Multimap<Integer, PatientNonSuppressingData> groupedByEncounter = Multimaps.index(patientData,
                    new Function<PatientNonSuppressingData, Integer>() {
                        @Override
                        public Integer apply(PatientNonSuppressingData item) {
                            return item.getEncounterId();
                        }
                    });

            for (Integer encounterId : groupedByEncounter.keySet()) {

                Date resultsReceivedAfterIACDate = defaultDate;
                Integer qualitativeViralLoadValue = defaultInteger;
                Double quantitativeViralLoadValue = defaultDouble;
                Date clinicalDecisionDateValue = defaultDate;
                Integer clinicalDecisionValue = defaultInteger;
                Integer regimenValue = defaultInteger;
                Date regimenStartDateValue = defaultDate;
                Date lastSessionDate = defaultDate;

                Collection<PatientNonSuppressingData> collection = groupedByEncounter.get(encounterId);

                Collection<PatientNonSuppressingData> sessions = Collections2.filter(collection, new NonSuppressedDataFilter(163154));
                Collection<PatientNonSuppressingData> resultsReceivedAfterIAC = Collections2.filter(collection, new NonSuppressedDataFilter(163150));
                Collection<PatientNonSuppressingData> qualitativeViralLoads = Collections2.filter(collection, new NonSuppressedDataFilter(1305));
                Collection<PatientNonSuppressingData> quantitativeViralLoads = Collections2.filter(collection, new NonSuppressedDataFilter(856));
                Collection<PatientNonSuppressingData> clinicalDecisionDates = Collections2.filter(collection, new NonSuppressedDataFilter(163167));
                Collection<PatientNonSuppressingData> clinicalDecisions = Collections2.filter(collection, new NonSuppressedDataFilter(163166));
                Collection<PatientNonSuppressingData> regimens = Collections2.filter(collection, new NonSuppressedDataFilter(163152));
                Collection<PatientNonSuppressingData> regimenStartDates = Collections2.filter(collection, new NonSuppressedDataFilter(163172));
                Collection<PatientNonSuppressingData> adherenceCodes = Collections2.filter(collection, new NonSuppressedDataFilter(90221));

                Collection<Date> sessionDates = CollectionUtils.collect(sessions, new BeanToPropertyValueTransformer("valueDatetime"));
                List<Date> list = new ArrayList(sessionDates);
                Collections.sort(list);

                if (!list.isEmpty() && list.size() >= 3) {
                    lastSessionDate = Iterables.getLast(list);
                }

                Collection<Integer> adherenceCodeValues = CollectionUtils.collect(adherenceCodes, new BeanToPropertyValueTransformer("valueCoded"));

                Collection<Date> resultsReceivedAfterIACDates = CollectionUtils.collect(resultsReceivedAfterIAC, new BeanToPropertyValueTransformer("valueDatetime"));

                if (resultsReceivedAfterIACDates.size() != 0) {
                    resultsReceivedAfterIACDate = Iterables.getLast(resultsReceivedAfterIACDates);
                }

                Collection<Integer> qualitativeViralLoadValues = CollectionUtils.collect(qualitativeViralLoads, new BeanToPropertyValueTransformer("valueCoded"));

                if (qualitativeViralLoadValues.size() != 0) {
                    qualitativeViralLoadValue = Iterables.getLast(qualitativeViralLoadValues);
                }

                Collection<Double> quantitativeViralLoadValues = CollectionUtils.collect(quantitativeViralLoads, new BeanToPropertyValueTransformer("valueNumeric"));

                if (quantitativeViralLoadValues.size() != 0) {
                    quantitativeViralLoadValue = Iterables.getLast(quantitativeViralLoadValues);
                }

                Collection<Date> clinicalDecisionDateValues = CollectionUtils.collect(clinicalDecisionDates, new BeanToPropertyValueTransformer("valueDatetime"));

                if (clinicalDecisionDateValues.size() != 0) {
                    clinicalDecisionDateValue = Iterables.getLast(clinicalDecisionDateValues);
                }

                Collection<Integer> clinicalDecisionValues = CollectionUtils.collect(clinicalDecisions, new BeanToPropertyValueTransformer("valueCoded"));
                if (clinicalDecisionValues.size() != 0) {
                    clinicalDecisionValue = Iterables.getLast(clinicalDecisionValues);
                }

                Collection<Integer> regimenValues = CollectionUtils.collect(regimens, new BeanToPropertyValueTransformer("valueCoded"));

                if (regimenValues.size() != 0) {
                    regimenValue = Iterables.getLast(regimenValues);
                }

                Collection<Date> regimenStartDateValues = CollectionUtils.collect(regimenStartDates, new BeanToPropertyValueTransformer("valueDatetime"));
                if (regimenStartDateValues.size() != 0) {
                    regimenStartDateValue = Iterables.getLast(regimenStartDateValues);
                }

                if (regimenValue != -1 && !regimenStartDateValue.equals(defaultDate)) {
                    Years age = Years.yearsBetween(StubDate.dateOf(data.getBirthDate()), StubDate.dateOf(regimenStartDateValue));
                    if (age.getYears() > 10 && secondLineDrugsAdults.contains(regimenValue) || age.getYears() <= 10 && secondLineDrugsChildren.contains(regimenValue)) {
                        firstLine = 2;
                    } else if (age.getYears() > 10 && firstLineDrugsAdults.contains(regimenValue) || age.getYears() <= 10 && firstLineDrugsChildren.contains(regimenValue)) {
                        firstLine = 1;
                    }
                }


                if (!lastSessionDate.equals(defaultDate) && (startDate.compareTo(lastSessionDate) * lastSessionDate.compareTo(endDate) >= 0)) {
                    if (adherenceCodeValues.size() >= 3) {
                        List<Integer> codeList = new ArrayList<Integer>(adherenceCodeValues);
                        List<Integer> sub = codeList.subList(adherenceCodeValues.size() - 3, adherenceCodeValues.size());
                        Set<Integer> unique = new HashSet<Integer>(sub);
                        if (unique.size() == 1 && sub.get(sub.size() - 1) == 90156) {
                            if (firstLine == 1) {
                                indicator1.add(patientId);
                            } else if (firstLine == 2) {
                                indicator2.add(patientId);
                            }
                        }
                    }
                }

                if (!resultsReceivedAfterIACDate.equals(defaultDate) && (startDate.compareTo(resultsReceivedAfterIACDate) * resultsReceivedAfterIACDate.compareTo(endDate) >= 0)) {

                    if (firstLine == 1) {
                        indicator3.add(patientId);
                    } else if (firstLine == 2) {
                        indicator4.add(patientId);
                    }

                    if (qualitativeViralLoadValue == 1306 || (quantitativeViralLoadValue < 1000 && quantitativeViralLoadValue > -1)) {
                        if (firstLine == 1) {
                            indicator5.add(patientId);
                        } else if (firstLine == 2) {
                            indicator6.add(patientId);
                        }
                    }
                }

                if (!clinicalDecisionDateValue.equals(defaultDate) && (startDate.compareTo(clinicalDecisionDateValue) * clinicalDecisionDateValue.compareTo(endDate) >= 0)) {
                    if (clinicalDecisionValue == 163162) {
                        indicator7.add(patientId);
                    } else if (clinicalDecisionValue == 163164) {
                        indicator8.add(patientId);
                    }
                }
            }
        }
        cohorts.put("h", Arrays.asList(new Cohort(indicator1), new Cohort(indicator2)));
        cohorts.put("i", Arrays.asList(new Cohort(indicator3), new Cohort(indicator4)));
        cohorts.put("j", Arrays.asList(new Cohort(indicator5), new Cohort(indicator6)));
        cohorts.put("k", Arrays.asList(new Cohort(indicator7), new Cohort(indicator8)));
        return cohorts;
    }

    private DataSetRow joinDatasetRows(DataSetRow dataSetRow1, DataSetRow dataSetRow2) {
        DataSetRow finalDataSetRow = new DataSetRow();

        for (Map.Entry<DataSetColumn, Object> data : dataSetRow1.getColumnValues().entrySet()) {
            finalDataSetRow.addColumnValue(data.getKey(), data.getValue());
        }

        for (Map.Entry<DataSetColumn, Object> data : dataSetRow2.getColumnValues().entrySet()) {
            finalDataSetRow.addColumnValue(data.getKey(), data.getValue());
        }
        return finalDataSetRow;
    }
}