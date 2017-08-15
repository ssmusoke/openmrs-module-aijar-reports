package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.DateObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.CBSAdultDatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.CommonCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Handler(supports = {CBSAdultDatasetDefinition.class})
public class CBSAdultDataSetEvaluator implements org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator {
    @Autowired
    private CohortDefinitionService cohortDefinitionService;
    @Autowired
    private HIVMetadata hivMetadata;
    @Autowired
    private CommonCohortDefinitionLibrary commonCohortDefinitionLibrary;
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;
    PatientDataHelper pdh = new PatientDataHelper();

    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evaluationContext)
            throws org.openmrs.module.reporting.evaluation.EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, evaluationContext);
        CBSAdultDatasetDefinition definition = (CBSAdultDatasetDefinition) dataSetDefinition;

        Date startDate = definition.getStartDate();
        Date endDate = definition.getEndDate();


        EncounterCohortDefinition cd = new EncounterCohortDefinition();
        cd.setEncounterTypeList(this.hivMetadata.getARTSummaryPageEncounterType());
        cd.setOnOrAfter(startDate);
        cd.setOnOrBefore(endDate);


        DateObsCohortDefinition withArtStartDate = new DateObsCohortDefinition();
        withArtStartDate.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        withArtStartDate.setQuestion(this.hivMetadata.getArtStartDate());

        DateObsCohortDefinition eligibleAndReady = new DateObsCohortDefinition();
        eligibleAndReady.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        eligibleAndReady.setQuestion(this.hivMetadata.getDateEligibleAndReadyForART());


        CohortDefinition clinicalStage = this.hivCohortDefinitionLibrary.getPatientsWithBaselineClinicalStage();
        CohortDefinition clinicalStage1 = this.hivCohortDefinitionLibrary.getPatientsWithBaselineClinicalStage(Arrays.asList(new Concept[]{this.hivMetadata.getWHOClinicalStage1(), this.hivMetadata.getWHOClinicalStage2()}));
        CohortDefinition clinicalStage2 = this.hivCohortDefinitionLibrary.getPatientsWithBaselineClinicalStage(Arrays.asList(new Concept[]{this.hivMetadata.getWHOClinicalStage3()}));
        CohortDefinition clinicalStage3 = this.hivCohortDefinitionLibrary.getPatientsWithBaselineClinicalStage(Arrays.asList(new Concept[]{this.hivMetadata.getWHOClinicalStage4()}));

        CohortDefinition baseCD4 = this.hivCohortDefinitionLibrary.getPatientsWithBaselineCD4();
        CohortDefinition baseCD4L50 = this.hivCohortDefinitionLibrary.getPatientsWithBaselineCD4After(Double.valueOf(50.0D));
        CohortDefinition baseCD4G50L200 = this.hivCohortDefinitionLibrary.getPatientsWithBaselineCD4(Double.valueOf(50.0D), Double.valueOf(200.0D));
        CohortDefinition baseCD4G200L350 = this.hivCohortDefinitionLibrary.getPatientsWithBaselineCD4(Double.valueOf(200.0D), Double.valueOf(350.0D));
        CohortDefinition baseCD4G350L500 = this.hivCohortDefinitionLibrary.getPatientsWithBaselineCD4(Double.valueOf(350.0D), Double.valueOf(500.0D));
        CohortDefinition baseCD4G500 = this.hivCohortDefinitionLibrary.getPatientsWithBaselineCD4(Double.valueOf(500.0D));


        Cohort enrolledThisQuarter = this.cohortDefinitionService.evaluate(cd, null);

        Cohort c = this.cohortDefinitionService.evaluate(clinicalStage, null);
        Cohort c1 = this.cohortDefinitionService.evaluate(clinicalStage1, null);
        Cohort c2 = this.cohortDefinitionService.evaluate(clinicalStage2, null);
        Cohort c3 = this.cohortDefinitionService.evaluate(clinicalStage3, null);

        Cohort cD4 = this.cohortDefinitionService.evaluate(baseCD4, null);
        Cohort cD4L50 = this.cohortDefinitionService.evaluate(baseCD4L50, null);
        Cohort cD4G50L200 = this.cohortDefinitionService.evaluate(baseCD4G50L200, null);
        Cohort cD4G200L350 = this.cohortDefinitionService.evaluate(baseCD4G200L350, null);
        Cohort cD4G350L500 = this.cohortDefinitionService.evaluate(baseCD4G350L500, null);
        Cohort cD4G500 = this.cohortDefinitionService.evaluate(baseCD4G500, null);

        Cohort onArt = this.cohortDefinitionService.evaluate(withArtStartDate, null);
        Cohort eligible = this.cohortDefinitionService.evaluate(eligibleAndReady, null);


        Cohort males = this.cohortDefinitionService.evaluate(this.commonCohortDefinitionLibrary.males(), null);


        Cohort females = this.cohortDefinitionService.evaluate(this.commonCohortDefinitionLibrary.females(), null);


        Cohort below5Years = this.cohortDefinitionService.evaluate(this.commonCohortDefinitionLibrary.agedAtMost(4, endDate), null);
        Cohort between5And9Years = this.cohortDefinitionService.evaluate(this.commonCohortDefinitionLibrary.agedBetween(5, 9, endDate), null);
        Cohort between10And19Years = this.cohortDefinitionService.evaluate(this.commonCohortDefinitionLibrary.agedBetween(10, 19, endDate), null);
        Cohort above19Years = this.cohortDefinitionService.evaluate(this.commonCohortDefinitionLibrary.agedAtLeast(20, endDate), null);


        Collection baseCohortMembers = enrolledThisQuarter.getMemberIds();

        Cohort enrolledThisQuarterStage1 = getCohort(CollectionUtils.intersection(baseCohortMembers, c1.getMemberIds()));
        Cohort enrolledThisQuarterStage2 = getCohort(CollectionUtils.intersection(baseCohortMembers, c2.getMemberIds()));
        Cohort enrolledThisQuarterStage3 = getCohort(CollectionUtils.intersection(baseCohortMembers, c3.getMemberIds()));
        Cohort enrolledThisQuarterNotStaged = getCohort(CollectionUtils.subtract(baseCohortMembers, c.getMemberIds()));

        Cohort enrolledThisQuarterCD4 = getCohort(CollectionUtils.intersection(baseCohortMembers, cD4.getMemberIds()));
        Cohort enrolledThisQuarterCD4L50 = getCohort(CollectionUtils.intersection(baseCohortMembers, cD4L50.getMemberIds()));
        Cohort enrolledThisQuarterCD4G50L200 = getCohort(CollectionUtils.intersection(baseCohortMembers, cD4G50L200.getMemberIds()));
        Cohort enrolledThisQuarterCD4G200L350 = getCohort(CollectionUtils.intersection(baseCohortMembers, cD4G200L350.getMemberIds()));
        Cohort enrolledThisQuarterCD4G350L500 = getCohort(CollectionUtils.intersection(baseCohortMembers, cD4G350L500.getMemberIds()));
        Cohort enrolledThisQuarterCD4G500 = getCohort(CollectionUtils.intersection(baseCohortMembers, cD4G500.getMemberIds()));

        Cohort enrolledThisQuarterEligibleAndReady = getCohort(CollectionUtils.intersection(baseCohortMembers, eligible.getMemberIds()));

        Cohort enrolledThisQuarterOnArt = getCohort(CollectionUtils.intersection(baseCohortMembers, onArt.getMemberIds()));


        List<String> clinicalStage1Columns = Arrays.asList(new String[]{"", "% of HIV clients enrolled  with HIV infection", "Clients with stage 1 and 2 disease", "All clients enrolled in a cohort"});
        List<String> clinicalStage2Columns = Arrays.asList(new String[]{"", "% of HIV clients enrolled  with Advanced disease", "Clients with stage 3 disease", "All clients enrolled in a cohort"});
        List<String> clinicalStage3Columns = Arrays.asList(new String[]{"", "% of HIV clients enrolled  with AIDS", "Clients with stage 4 disease", "All clients enrolled in a cohort"});
        List<String> clinicalNotStageColumns = Arrays.asList(new String[]{"", "% of clients not staged", "All clients with no clinical stage", "All clients enrolled in a cohort"});

        List<String> cD4Columns = Arrays.asList(new String[]{"", "% of clients with Baseline CD4", "All clients tested for CD4 at enrollment", "All clients enrolled in a cohort"});
        List<String> cD4L50Columns = Arrays.asList(new String[]{"", "% of HIV + clients with very severe HIV infection", "C1 :<50", "All clients enrolled in a cohort"});
        List<String> cD4G50L200Columns = Arrays.asList(new String[]{"", "% of HIV + clients with  severe HIV infection", "C2: 50- <200", "All clients enrolled in a cohort"});
        List<String> cD4G200L350Columns = Arrays.asList(new String[]{"", "% of HIV + clients with  advanced HIV infection", "C2: 200- <350", "All clients enrolled in a cohort"});
        List<String> cD4G350L500Columns = Arrays.asList(new String[]{"", "% of HIV + clients with  mild HIV infection", "C2: 350- <500", "All clients enrolled in a cohort"});
        List<String> cD4G500Columns = Arrays.asList(new String[]{"", "% of HIV + clients with None or not significant HIV infection", "C5: >500", "All clients enrolled in a cohort"});

        List<String> eligibleColumns = Arrays.asList(new String[]{"", "% Initiated on ART", "Number initiated on ART", "Number eligible for ART"});


        Map<String, List<Cohort>> aggregates = new HashMap();

        aggregates.put("1", Arrays.asList(new Cohort[]{below5Years, males}));
        aggregates.put("2", Arrays.asList(new Cohort[]{below5Years, females}));

        aggregates.put("3", Arrays.asList(new Cohort[]{between5And9Years, males}));
        aggregates.put("4", Arrays.asList(new Cohort[]{between5And9Years, females}));

        aggregates.put("5", Arrays.asList(new Cohort[]{between10And19Years, males}));
        aggregates.put("6", Arrays.asList(new Cohort[]{between10And19Years, females}));

        aggregates.put("7", Arrays.asList(new Cohort[]{above19Years, males}));
        aggregates.put("8", Arrays.asList(new Cohort[]{above19Years, females}));

        dataSet.addRow(populate(enrolledThisQuarterStage1, enrolledThisQuarter, aggregates, clinicalStage1Columns));
        dataSet.addRow(populate(enrolledThisQuarterStage2, enrolledThisQuarter, aggregates, clinicalStage2Columns));
        dataSet.addRow(populate(enrolledThisQuarterStage3, enrolledThisQuarter, aggregates, clinicalStage3Columns));
        dataSet.addRow(populate(enrolledThisQuarterNotStaged, enrolledThisQuarter, aggregates, clinicalNotStageColumns));
        dataSet.addRow(populate(enrolledThisQuarterCD4, enrolledThisQuarter, aggregates, cD4Columns));
        dataSet.addRow(populate(enrolledThisQuarterCD4L50, enrolledThisQuarter, aggregates, cD4L50Columns));
        dataSet.addRow(populate(enrolledThisQuarterCD4G50L200, enrolledThisQuarter, aggregates, cD4G50L200Columns));
        dataSet.addRow(populate(enrolledThisQuarterCD4G200L350, enrolledThisQuarter, aggregates, cD4G200L350Columns));
        dataSet.addRow(populate(enrolledThisQuarterCD4G350L500, enrolledThisQuarter, aggregates, cD4G350L500Columns));
        dataSet.addRow(populate(enrolledThisQuarterCD4G500, enrolledThisQuarter, aggregates, cD4G500Columns));
        dataSet.addRow(populate(enrolledThisQuarterOnArt, enrolledThisQuarterEligibleAndReady, aggregates, eligibleColumns));


        return dataSet;
    }

    private DataSetRow populate(Cohort numerator, Cohort denominator, Map<String, List<Cohort>> breakdown, List<String> otherColumnValues) {
        DataSetRow row = new DataSetRow();

        Iterator<String> otherColumnValuesIterator = otherColumnValues.iterator();
        Iterator<String> otherColumnsIterator = Arrays.asList(new String[]{"sn", "indicator", "numerator", "denominator"}).iterator();


        Map<String, String> otherColumns = new HashMap();

        while ((otherColumnsIterator.hasNext()) && (otherColumnValuesIterator.hasNext())) {
            otherColumns.put(otherColumnsIterator.next(), otherColumnValuesIterator.next());
        }

        for (Map.Entry<String, String> o : otherColumns.entrySet()) {
            this.pdh.addCol(row, (String) o.getKey(), o.getValue());
        }

        for (Map.Entry<String, List<Cohort>> b : breakdown.entrySet()) {
            Collection denominatorCohort = denominator.getMemberIds();
            Collection numeratorCohort = numerator.getMemberIds();
            String k = (String) b.getKey();
            List<Cohort> bVal = (List) b.getValue();

            for (Cohort found : bVal) {
                denominatorCohort = CollectionUtils.intersection(denominatorCohort, found.getMemberIds());
                numeratorCohort = CollectionUtils.intersection(numeratorCohort, found.getMemberIds());
            }

            Integer n = Integer.valueOf(numeratorCohort.size());
            Integer d = Integer.valueOf(numeratorCohort.size());

            this.pdh.addCol(row, "numerator" + k, n);
            this.pdh.addCol(row, "denominator" + k, d);
        }


        return row;
    }

    private Cohort getCohort(Collection<Integer> results) {
        Cohort result = new Cohort();
        Set<Integer> patients = new java.util.HashSet();
        for (Integer r : results) {
            patients.add(r);
        }
        result.setMemberIds(patients);
        return result;
    }
}