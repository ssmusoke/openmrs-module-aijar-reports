package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * HCT TST FACILITY Report
 */
@Component
public class SetupHCT_TST_Facility2019Report extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private TBCohortDefinitionLibrary tbCohortDefinitionLibrary;

    @Autowired
    private Moh105CohortLibrary moh105CohortLibrary;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "fbc91a59-0d57-4a67-bfa4-49b3afd4f929";
    }

    @Override
    public String getUuid() {
        return "7ecba0ec-e863-4aac-bbcc-8f5e1e470b10";
    }

    @Override
    public String getName() {
        return "HCT_TST_Facility Report";
    }

    @Override
    public String getDescription() {
        return "HCT_TST_Facility Report";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(df.getStartDateParameter());
        l.add(df.getEndDateParameter());
        return l;
    }


    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        return Arrays.asList(buildReportDesign(reportDefinition));
    }

    /**
     * Build the report design for the specified report, this allows a user to override the report design by adding
     * properties and other metadata to the report design
     *
     * @param reportDefinition
     * @return The report design
     */
    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "MER_HCT_TST_Facility.xls");
    }

    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();

        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();

        dsd.setParameters(getParameters());
        rd.addDataSetDefinition("HCT_TST", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension ageDimension =commonDimensionLibrary.getTxNewAgeGenderGroup();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));

        CohortDefinition testedForHIVAndReceivedResults = hivCohortDefinitionLibrary.getPatientsTestedForHIVAndReceivedResults();
        CohortDefinition testedPositiveDuringPeriod = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept(Metadata.Concept.CURRENT_HIV_TEST_RESULTS),hivMetadata.getHCTEncounterType(),Arrays.asList(Dictionary.getConcept(Metadata.Concept.HIV_POSITIVE)), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition testedNegativeDuringPeriod = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept(Metadata.Concept.CURRENT_HIV_TEST_RESULTS),hivMetadata.getHCTEncounterType(),Arrays.asList(Dictionary.getConcept(Metadata.Concept.HIV_NEGATIVE)),BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition testedForHIVAndReceivedResultsAndPositive = df.getPatientsInAll(testedForHIVAndReceivedResults,testedPositiveDuringPeriod);
        CohortDefinition testedForHIVAndReceivedResultsAndNegative = df.getPatientsInAll(testedForHIVAndReceivedResults,testedNegativeDuringPeriod);
        CohortDefinition patientThroughSTIClinicEntryPoint = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("720a1e85-ea1c-4f7b-a31e-cb896978df79"),hivMetadata.getHCTEncounterType(),Arrays.asList(Dictionary.getConcept("dcd98f72-30ab-102d-86b0-7a5022ba4115")), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition patientThroughTBClinicEntryPoint = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("720a1e85-ea1c-4f7b-a31e-cb896978df79"),hivMetadata.getHCTEncounterType(),Arrays.asList(Dictionary.getConcept("165048AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition PWIDS = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("927563c5-cb91-4536-b23c-563a72d3f829"),hivMetadata.getHCTEncounterType(),
                Arrays.asList(Dictionary.getConcept("160666AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition PIPS = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("927563c5-cb91-4536-b23c-563a72d3f829"),hivMetadata.getHCTEncounterType(),
                Arrays.asList(Dictionary.getConcept("162277AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition HIVPositiveAndThroughSTIClinic = df.getPatientsInAll( patientThroughSTIClinicEntryPoint, testedForHIVAndReceivedResultsAndPositive);
        CohortDefinition HIVNegativeAndThroughSTIClinic = df.getPatientsInAll( patientThroughSTIClinicEntryPoint, testedForHIVAndReceivedResultsAndNegative);

        CohortDefinition HIVPositiveAndThroughTBClinic = df.getPatientsInAll( patientThroughTBClinicEntryPoint, testedForHIVAndReceivedResultsAndPositive);
        CohortDefinition HIVNegativeAndThroughTBClinic = df.getPatientsInAll( patientThroughTBClinicEntryPoint, testedForHIVAndReceivedResultsAndNegative);

        CohortDefinition PIPAndPositive = df.getPatientsInAll(PIPS,testedForHIVAndReceivedResultsAndPositive);
        CohortDefinition PIPAndNegative = df.getPatientsInAll(PIPS,testedForHIVAndReceivedResultsAndNegative);

        CohortDefinition PWIDAndPositive = df.getPatientsInAll(PWIDS,testedForHIVAndReceivedResultsAndPositive);
        CohortDefinition PWIDAndNegative = df.getPatientsInAll(PWIDS,testedForHIVAndReceivedResultsAndNegative);

        addGender(dsd,"e","testedHIV+AndThroughSTI female ",HIVPositiveAndThroughSTIClinic,"female");
        addGender(dsd,"f","testedHIV+AndThroughSTI male", HIVPositiveAndThroughSTIClinic,"male");

        addGender(dsd,"g","testedHIV-AndThroughSTI female ",  HIVNegativeAndThroughSTIClinic,"female");
        addGender(dsd,"h","testedHIV-AndThroughSTI male",  HIVNegativeAndThroughSTIClinic,"male");

        addGender(dsd,"m","testedHIV+AndThroughTB female ",HIVPositiveAndThroughTBClinic,"female");
        addGender(dsd,"n","testedHIV+AndThroughTB male", HIVPositiveAndThroughTBClinic,"male");

        addGender(dsd,"o","testedHIV-AndThroughTB female ", HIVNegativeAndThroughTBClinic,"female");
        addGender(dsd,"p","testedHIV-AndThroughTB male",  HIVNegativeAndThroughTBClinic,"male");

        addIndicator(dsd, "PIPa", "PIP positive female",PIPAndPositive, "age=female");
        addIndicator(dsd, "PIPb", "PIP positive male",PIPAndPositive, "age=male");

        addIndicator(dsd, "PIPc", "PIP negative female",PIPAndNegative, "age=female");
        addIndicator(dsd, "PIPd", "PIP negative male",PIPAndNegative, "age=male");

        addIndicator(dsd, "PWIDSa", "PWID positive female",PWIDAndPositive, "age=female");
        addIndicator(dsd, "PWIDSb", "PWID positive male",PWIDAndPositive, "age=male");

        addIndicator(dsd, "PWIDSc", "PWID negative female",PWIDAndNegative, "age=female");
        addIndicator(dsd, "PWIDSd", "PWID negative male",PWIDAndNegative, "age=male");

        return rd;
    }

    public void addGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition,String gender){

            addIndicator(dsd, "1"+key, label,cohortDefinition, "age=below1"+gender);
            addIndicator(dsd, "2"+key, label, cohortDefinition, "age=between1and4"+gender);
            addIndicator(dsd, "3"+key, label, cohortDefinition, "age=between5and9"+gender);
            addIndicator(dsd, "4"+key, label, cohortDefinition, "age=between10and14"+gender);
            addIndicator(dsd, "5"+key, label, cohortDefinition, "age=between15and19"+gender);
            addIndicator(dsd, "6"+key, label, cohortDefinition, "age=between20and24"+gender);
            addIndicator(dsd, "7"+key, label, cohortDefinition, "age=between25and29"+gender);
            addIndicator(dsd, "8"+key, label, cohortDefinition, "age=between30and34"+gender);
            addIndicator(dsd, "9"+key,label, cohortDefinition, "age=between35and39"+gender);
            addIndicator(dsd, "10"+key,label, cohortDefinition, "age=between40and44"+gender);
            addIndicator(dsd, "11"+key,label, cohortDefinition, "age=between45and49"+gender);
            addIndicator(dsd, "12"+key,label, cohortDefinition, "age=above50"+gender);
    }

    public void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition, String dimensionOptions) {
        CohortIndicator ci = new CohortIndicator();
        ci.addParameter(ReportingConstants.START_DATE_PARAMETER);
        ci.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ci.setType(CohortIndicator.IndicatorType.COUNT);
        ci.setCohortDefinition(Mapped.mapStraightThrough(cohortDefinition));
        dsd.addColumn(key, label, Mapped.mapStraightThrough(ci), dimensionOptions);
    }


    @Override
    public String getVersion() {
        return "0.1";
    }
}
