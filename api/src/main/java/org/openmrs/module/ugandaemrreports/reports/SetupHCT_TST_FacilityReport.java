package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
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
public class SetupHCT_TST_FacilityReport extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "cb83f652-62b8-4b38-b755-b71a2a3b9964";
    }

    @Override
    public String getUuid() {
        return "5688b030-d2b7-43b5-affd-9e94a07a1a8a";
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
        CohortDefinition patientThroughSNS = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("2afe1128-c3f6-4b35-b119-d17b9b9958ed"),hivMetadata.getHCTEncounterType(),Arrays.asList(Dictionary.getConcept("43eae374-df77-464c-ad3c-3deb5bfe2447")), BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition ANC1Only = df.getPatientsWithNumericObsDuringPeriod(Dictionary.getConcept("c7231d96-34d8-4bf7-a509-c810f75e3329"), hivMetadata.getHCTEncounterType(), RangeComparator.EQUAL, 1.0, BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition ANC2AndAbove = df.getPatientsWithNumericObsDuringPeriod(Dictionary.getConcept("c7231d96-34d8-4bf7-a509-c810f75e3329"), hivMetadata.getHCTEncounterType(), RangeComparator.GREATER_THAN, 1.0, BaseObsCohortDefinition.TimeModifier.LAST);


        CohortDefinition facilityDeliveryModel = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept(Metadata.Concept.HTC_DELIVERY_MODEL),hivMetadata.getHCTEncounterType(),Arrays.asList(Dictionary.getConcept(Metadata.Concept.FACILITY_BASED)), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition communityDeliveryModel = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept(Metadata.Concept.HTC_DELIVERY_MODEL),hivMetadata.getHCTEncounterType(),Arrays.asList(Dictionary.getConcept(Metadata.Concept.COMMUNITY_TESTING_POINT)), BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition patientThroughSNSFacilityEntryPoint = df.getPatientsInAll(patientThroughSNS,facilityDeliveryModel);
        CohortDefinition patientThroughSNSCommunityEntryPoint = df.getPatientsInAll(patientThroughSNS,communityDeliveryModel);

        CohortDefinition HIVPositiveAndThroughSNS = df.getPatientsInAll(patientThroughSNSFacilityEntryPoint, testedForHIVAndReceivedResultsAndPositive);
        CohortDefinition HIVNegativeAndThroughSNS = df.getPatientsInAll(patientThroughSNSFacilityEntryPoint, testedForHIVAndReceivedResultsAndNegative);

        CohortDefinition HIVPositiveAndThroughSNSCommunity = df.getPatientsInAll( patientThroughSNSCommunityEntryPoint, testedForHIVAndReceivedResultsAndPositive);
        CohortDefinition HIVNegativeAndThroughNSCommunity = df.getPatientsInAll( patientThroughSNSCommunityEntryPoint, testedForHIVAndReceivedResultsAndNegative);


        addGender(dsd,"a","ANC1 positive female ",df.getPatientsInAll(ANC1Only,testedForHIVAndReceivedResultsAndPositive),"female");
        addGender(dsd,"b","ANC1 negative female", df.getPatientsInAll(ANC1Only,testedForHIVAndReceivedResultsAndNegative),"female");

        addGender(dsd,"c","ANC2andAbove positive female ",df.getPatientsInAll(ANC2AndAbove,testedForHIVAndReceivedResultsAndPositive),"female");
        addGender(dsd,"d","ANC2andAbove negative female", df.getPatientsInAll(ANC2AndAbove,testedForHIVAndReceivedResultsAndNegative),"female");

        addGender(dsd,"e","HIVPositiveAndThroughSNS female ",HIVPositiveAndThroughSNS,"female");
        addGender(dsd,"f","HIVPositiveAndThroughSNS male", HIVPositiveAndThroughSNS,"male");

        addGender(dsd,"g","HIVNegativeAndThroughSNS female ",  HIVNegativeAndThroughSNS,"female");
        addGender(dsd,"h","HIVNegativeAndThroughSNS male",  HIVNegativeAndThroughSNS,"male");

        addGender(dsd,"m","HIVPositiveAndThroughSNSCommunity female ",HIVPositiveAndThroughSNSCommunity,"female");
        addGender(dsd,"n","HIVPositiveAndThroughSNSCommunity male", HIVPositiveAndThroughSNSCommunity,"male");

        addGender(dsd,"o","HIVNegativeAndThroughNSCommunity female ", HIVNegativeAndThroughNSCommunity,"female");
        addGender(dsd,"p","tHIVNegativeAndThroughNSCommunity male",  HIVNegativeAndThroughNSCommunity,"male");


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
        return "3.0.6";
    }
}
