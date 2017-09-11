package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.TransformerUtils;
import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.service.PatientDataService;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.ugandaemrreports.common.PatientARV;
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.common.PatientMonthData;
import org.openmrs.module.ugandaemrreports.common.ViralLoad;
import org.openmrs.module.ugandaemrreports.definition.data.definition.CBSPatientARVDataDefinition;
import org.openmrs.module.ugandaemrreports.definition.data.definition.CBSPatientDataDefinition;
import org.openmrs.module.ugandaemrreports.definition.data.definition.ViralLoadPatientDataDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.CBSAdultFollowupDatasetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.predicates.*;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Handler(supports = {CBSAdultFollowupDatasetDefinition.class})
public class CBSAdultFollowupDataSetEvaluator implements org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator {
    @Autowired
    private CohortDefinitionService cohortDefinitionService;
    @Autowired
    private HIVMetadata hivMetadata;
    @Autowired
    private org.openmrs.module.ugandaemrreports.library.CommonCohortDefinitionLibrary commonCohortDefinitionLibrary;
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    PatientDataHelper pdh = new PatientDataHelper();

    public org.openmrs.module.reporting.dataset.DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evaluationContext)
            throws org.openmrs.module.reporting.evaluation.EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, evaluationContext);
        CBSAdultFollowupDatasetDefinition definition = (CBSAdultFollowupDatasetDefinition) dataSetDefinition;

        Date startDate = definition.getStartDate();
        Date endDate = definition.getEndDate();

        ViralLoadPatientDataDefinition viralLoadPatientDataDefinition = new ViralLoadPatientDataDefinition();
        viralLoadPatientDataDefinition.setEndDate(endDate);
        viralLoadPatientDataDefinition.setStartDate(startDate);

        EvaluatedPatientData data = Context.getService(PatientDataService.class).evaluate(viralLoadPatientDataDefinition, evaluationContext);

        Collection<ViralLoad> viralLoads = convertDataToObjects(data.getData());


        EncounterCohortDefinition cd = new EncounterCohortDefinition();
        cd.setEncounterTypeList(this.hivMetadata.getARTSummaryPageEncounterType());
        cd.setOnOrAfter(startDate);
        cd.setOnOrBefore(endDate);


        Cohort enrolledThisQuarter = this.cohortDefinitionService.evaluate(cd, null);


        CBSPatientARVDataDefinition cbsPatientARVDataDefinition = new CBSPatientARVDataDefinition();
        cbsPatientARVDataDefinition.setStartDate(startDate);
        cbsPatientARVDataDefinition.setCohort(enrolledThisQuarter);

        CBSPatientDataDefinition cbsPatientDataDefinition = new CBSPatientDataDefinition();
        cbsPatientDataDefinition.setStartDate(startDate);
        cbsPatientDataDefinition.setCohort(enrolledThisQuarter);


        EvaluatedPatientData cbsPatientARVData = Context.getService(PatientDataService.class).evaluate(cbsPatientARVDataDefinition, evaluationContext);
        EvaluatedPatientData cbsPatientData = Context.getService(PatientDataService.class).evaluate(cbsPatientDataDefinition, evaluationContext);

        Collection<PatientARV> patientARVs = convertARVDataToObjects(cbsPatientARVData.getData());
        Collection<PatientMonthData> patientData = convertPatientDataToObjects(cbsPatientData.getData());


        List<String> aliveColumns = Arrays.asList("", "% alive and on ART treatment", "Numbere alive and on ART treatment", "All clients enrolled in a cohort");
        List<String> secondLineColumns = Arrays.asList("", "% of clients on 2nd line ARV regimen ", "Number of clients on 2nd line regimen", "All clients on ART");
        List<String> failingOnSecondLineColumns = Arrays.asList("", "% of clients  failing  on 2nd line ARV regimen", "Number of clients failing on 2nd line ARV regimen", "Number of clients on 2nd line regimen");
        List<String> thirdLineColumns = Arrays.asList("", "% of clients on 3rd  line ARV regimen", "Number of clients on 3rd line regimen", "All clients on ART");

        List<String> failingOnThirdLineColumns = Arrays.asList("", "% of clients failing on 3rd  line ARV regimen", "Number of clients failing on 3rd line ARV regimen", "Number of clients on 3nd line regimen");
        List<String> lostToFollowupColumns = Arrays.asList("", "% who are Lost to follow up ", "Number of clients lost", "All clients enrolled in a cohort");
        List<String> deadPatientsColumns = Arrays.asList("", "% who died ", "Number of clients dead", "All clients enrolled in a cohort");

        Map<String, Integer> monthsMap = new HashMap<String, Integer>();

        monthsMap.put("6", 6);
        monthsMap.put("12", 12);
        monthsMap.put("24", 24);
        monthsMap.put("36", 36);
        monthsMap.put("48", 48);
        monthsMap.put("60", 60);
        monthsMap.put("72", 72);

        Map<String, Integer> months = new TreeMap<String, Integer>(monthsMap);

        Map<String, Map<String, Cohort>> aliveMap = new HashMap<String, Map<String, Cohort>>();
        Map<String, Map<String, Cohort>> secondLineMap = new HashMap<String, Map<String, Cohort>>();
        Map<String, Map<String, Cohort>> failingSecondLineMap = new HashMap<String, Map<String, Cohort>>();
        Map<String, Map<String, Cohort>> thirdLineMap = new HashMap<String, Map<String, Cohort>>();
        Map<String, Map<String, Cohort>> failingThirdLineMap = new HashMap<String, Map<String, Cohort>>();
        Map<String, Map<String, Cohort>> lostToFollowupMap = new HashMap<String, Map<String, Cohort>>();
        Map<String, Map<String, Cohort>> diedMap = new HashMap<String, Map<String, Cohort>>();


        for (Map.Entry<String, Integer> o : months.entrySet()) {
            Map<String, Cohort> aliveCohort = new HashMap<String, Cohort>();
            Map<String, Cohort> secondLineCohort = new HashMap<String, Cohort>();
            Map<String, Cohort> failingSecondLineCohort = new HashMap<String, Cohort>();
            Map<String, Cohort> thirdLineCohort = new HashMap<String, Cohort>();
            Map<String, Cohort> failingThirdLineCohort = new HashMap<String, Cohort>();
            Map<String, Cohort> lostToFollowupCohort = new HashMap<String, Cohort>();
            Map<String, Cohort> diedCohort = new HashMap<String, Cohort>();

            final Integer i = o.getValue();
            String key = o.getKey();

            Collection<Integer> secondLineDrugsChildren = CollectionUtils.collect(CBSAdultFollowupDataSetEvaluator.this.hivMetadata.getSecondLineDrugsChildren(), TransformerUtils.invokerTransformer("getConceptId"));
            Collection<Integer> secondLineDrugsAdults = CollectionUtils.collect(CBSAdultFollowupDataSetEvaluator.this.hivMetadata.getSecondLineDrugsAdults(), TransformerUtils.invokerTransformer("getConceptId"));

            Collection<Integer> thirdLineDrugs = CollectionUtils.collect(CBSAdultFollowupDataSetEvaluator.this.hivMetadata.getThirdLineDrugs(), TransformerUtils.invokerTransformer("getConceptId"));


            Cohort diedDuring = getPatientData(Collections2.filter(patientData, new PatientMonthDataFilter(i, 3)));
            Cohort onArtDuring = getPatientARVs(Collections2.filter(patientARVs, new PatientARVFilter(i)));

            Cohort secondLineDuring = getPatientARVs(Collections2.filter(patientARVs, new PatientSecondLineARVFilter(i, secondLineDrugsChildren, secondLineDrugsAdults)));
            Cohort thirdLineDuring = getPatientARVs(Collections2.filter(patientARVs, new PatientThirdLineARVFilter(i, thirdLineDrugs)));

            Cohort failedDuring = getPatientViral(Collections2.filter(viralLoads, new ViralLoadFailedFilter(i)));

            Cohort hadAVisit = getPatientData(Collections2.filter(patientData, new PatientMonthDataFilter(i, 2)));
            Cohort hadAnEncounter = getPatientData(Collections2.filter(patientData, new PatientMonthDataFilter(i, 1)));

            Set<Integer> difference = new HashSet(CollectionUtils.subtract(hadAVisit.getMemberIds(), hadAnEncounter.getMemberIds()));

            Set<Integer> failedSecondLine = new HashSet(CollectionUtils.subtract(secondLineDuring.getMemberIds(), failedDuring.getMemberIds()));
            Set<Integer> failedThirdLine = new HashSet(CollectionUtils.subtract(thirdLineDuring.getMemberIds(), failedDuring.getMemberIds()));

            Cohort lost = new Cohort();
            lost.setMemberIds(difference);

            Cohort secondLineFailed = new Cohort();
            secondLineFailed.setMemberIds(failedSecondLine);

            Cohort thirdLineFailed = new Cohort();
            thirdLineFailed.setMemberIds(failedThirdLine);

            diedCohort.put("numerator", diedDuring);
            diedCohort.put("denominator", enrolledThisQuarter);

            aliveCohort.put("numerator", onArtDuring);
            aliveCohort.put("denominator", enrolledThisQuarter);

            secondLineCohort.put("numerator", secondLineDuring);
            secondLineCohort.put("denominator", onArtDuring);

            failingSecondLineCohort.put("numerator", secondLineFailed);
            failingSecondLineCohort.put("denominator", secondLineDuring);

            thirdLineCohort.put("numerator", thirdLineDuring);
            thirdLineCohort.put("denominator", onArtDuring);

            failingThirdLineCohort.put("numerator", thirdLineFailed);
            failingThirdLineCohort.put("denominator", thirdLineDuring);

            lostToFollowupCohort.put("numerator", lost);
            lostToFollowupCohort.put("denominator", enrolledThisQuarter);

            aliveMap.put(key, aliveCohort);
            secondLineMap.put(key, secondLineCohort);
            failingSecondLineMap.put(key, failingSecondLineCohort);
            thirdLineMap.put(key, thirdLineCohort);
            failingThirdLineMap.put(key, failingThirdLineCohort);
            lostToFollowupMap.put(key, lostToFollowupCohort);
            diedMap.put(key, diedCohort);
        }

        dataSet.addRow(populate(aliveMap, aliveColumns));
        dataSet.addRow(populate(secondLineMap, secondLineColumns));
        dataSet.addRow(populate(failingSecondLineMap, failingOnSecondLineColumns));
        dataSet.addRow(populate(thirdLineMap, thirdLineColumns));
        dataSet.addRow(populate(failingThirdLineMap, failingOnThirdLineColumns));
        dataSet.addRow(populate(lostToFollowupMap, lostToFollowupColumns));
        dataSet.addRow(populate(diedMap, deadPatientsColumns));

        return dataSet;
    }

    private DataSetRow populate(Map<String, Map<String, Cohort>> data, List<String> otherColumnValues) {
        DataSetRow row = new DataSetRow();

        Iterator<String> otherColumnValuesIterator = otherColumnValues.iterator();
        Iterator<String> otherColumnsIterator = Arrays.asList(new String[]{"sn", "indicator", "numerator", "denominator"}).iterator();


        Map<String, String> otherColumns = new HashMap<String, String>();

        while ((otherColumnsIterator.hasNext()) && (otherColumnValuesIterator.hasNext())) {
            otherColumns.put(otherColumnsIterator.next(), otherColumnValuesIterator.next());
        }

        for (Map.Entry<String, String> o : otherColumns.entrySet()) {
            this.pdh.addCol(row, o.getKey(), o.getValue());
        }


        for (Map.Entry<String, Map<String, Cohort>> d : data.entrySet()) {
            String k = d.getKey();
            Map<String, Cohort> value = d.getValue();
            Integer denominator = value.get("denominator").size();
            Integer numerator = value.get("numerator").size();

            this.pdh.addCol(row, "numerator" + k, numerator);
            this.pdh.addCol(row, "denominator" + k, denominator);
        }

        return row;
    }

    private Collection<PatientARV> convertARVDataToObjects(Map<Integer, Object> hashedMap) {
        Collection<PatientARV> patientARVs = new ArrayList<PatientARV>();
        for (Map.Entry<Integer, Object> o : hashedMap.entrySet()) {
            patientARVs.add((PatientARV) o.getValue());
        }

        return patientARVs;
    }

    private Collection<PatientMonthData> convertPatientDataToObjects(Map<Integer, Object> hashedMap) {
        Collection<PatientMonthData> patientMonthData = new ArrayList<PatientMonthData>();
        for (Map.Entry<Integer, Object> o : hashedMap.entrySet()) {
            patientMonthData.add((PatientMonthData) o.getValue());
        }

        return patientMonthData;
    }

    private Cohort getPatientData(Collection<PatientMonthData> results) {
        Cohort result = new Cohort();
        Set<Integer> patients = new HashSet<Integer>();
        for (PatientMonthData r : results) {
            patients.add(r.getPatientId());
        }
        result.setMemberIds(patients);
        return result;
    }

    private Cohort getPatientARVs(Collection<PatientARV> results) {
        Cohort result = new Cohort();
        Set<Integer> patients = new HashSet<Integer>();
        for (PatientARV r : results) {
            patients.add(r.getPatientId());
        }
        result.setMemberIds(patients);
        return result;
    }

    private Cohort getPatientViral(Collection<ViralLoad> results) {
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
}