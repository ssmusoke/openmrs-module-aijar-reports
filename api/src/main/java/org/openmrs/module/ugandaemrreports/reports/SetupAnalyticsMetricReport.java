package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.Location;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.ARTCohortLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  TX Current Report
 */
@Component
public class SetupAnalyticsMetricReport extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    @Autowired
    private HIVMetadata hivMetadata;
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private CommonCohortDefinitionLibrary commonCohortDefinitionLibrary;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    private ARTCohortLibrary artCohortLibrary;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "a06b7310-2565-45bd-89af-514b724a1db0";
    }

    @Override
    public String getUuid() {
        return "16a5243f-d0c2-4a15-bc25-411437efb5f3";
    }

    @Override
    public String getName() {
        return "Analytics Metric Report";
    }

    @Override
    public String getDescription() {
        return "Analytics Metric Report";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "METRICS.xls");
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
        rd.addDataSetDefinition("METRIC", Mapped.mapStraightThrough(dsd));

        Location reception = commonDimensionLibrary.getLocationByUuid("4501e132-07a2-4201-9dc8-2f6769b6d412");
        Location triage = commonDimensionLibrary.getLocationByUuid("ff01eaab-561e-40c6-bf24-539206b521ce");
        Location counselor = commonDimensionLibrary.getLocationByUuid("7c231e1a-1db5-11ea-978f-2e728ce88125");
        Location ART_Clinician = commonDimensionLibrary.getLocationByUuid("86863db4-6101-4ecf-9a86-5e716d6504e4");
        Location Lab = commonDimensionLibrary.getLocationByUuid("ba158c33-dc43-4306-9a4a-b4075751d36c");
        Location Pharmacy = commonDimensionLibrary.getLocationByUuid("3ec8ff90-3ec1-408e-bf8c-22e4553d6e17");


        CohortDefinition ARTEncounter =  df.getAnyEncounterOfTypesByEndOfDate(hivMetadata.getArtEncounterTypes());
        CohortDefinition HTSEncounter =  df.getAnyEncounterOfTypesByEndOfDate(hivMetadata.getHCTEncounterType());
        CohortDefinition EIDCounter =  df.getAnyEncounterOfTypesByEndOfDate(hivMetadata.getHCTEncounterType());

        CohortDefinition transferredInTheQuarter = hivCohortDefinitionLibrary.getTransferredInToCareDuringPeriod();

        CohortDefinition enrolledOnOrBeforeQuarter = hivCohortDefinitionLibrary.getEnrolledInCareByEndOfPreviousDate();
        CohortDefinition enrolledInTheQuarter = hivCohortDefinitionLibrary.getEnrolledInCareBetweenDates();

        CohortDefinition everEnrolledByEndQuarter = df.getPatientsNotIn(enrolledOnOrBeforeQuarter, enrolledInTheQuarter);
        CohortDefinition enrolledDuringTheQuarter = df.getPatientsNotIn(enrolledInTheQuarter, transferredInTheQuarter);

        CohortDefinition cumulativeEverEnrolled = df.getPatientsInAny(everEnrolledByEndQuarter, enrolledDuringTheQuarter);

        CohortDefinition patientsThroughReception = df.getAnyEncounterOfTypesByEndOfDate(hivMetadata.getArtEncounterTypes(), Arrays.asList(reception));
        CohortDefinition patientsThroughTriage = df.getAnyEncounterOfTypesByEndOfDate(hivMetadata.getArtEncounterTypes(),Arrays.asList(triage));
        CohortDefinition patientsThroughCounselor = df.getAnyEncounterOfTypesByEndOfDate(hivMetadata.getArtEncounterTypes(),Arrays.asList(counselor));
        CohortDefinition patientsThroughLab = df.getAnyEncounterOfTypesByEndOfDate(hivMetadata.getArtEncounterTypes(),Arrays.asList(Lab));
        CohortDefinition patientsThroughPharmacy = df.getAnyEncounterOfTypesByEndOfDate(hivMetadata.getArtEncounterTypes(),Arrays.asList(Pharmacy));
        CohortDefinition patientsThroughARTClinician = df.getAnyEncounterOfTypesByEndOfDate(hivMetadata.getArtEncounterTypes(),Arrays.asList(ART_Clinician));







        addIndicator(dsd, "", "ART Encounter", ARTEncounter, "");
        addIndicator(dsd, "", "HTS Encounters", HTSEncounter, "");
        addIndicator(dsd, "", "EID Encounters",EIDCounter, "");
        addIndicator(dsd, "", "Active patients In Care", , "");
        addIndicator(dsd, "", "Patients Ever enrolled ", cumulativeEverEnrolled, "");
        addIndicator(dsd, "", "Patients Served At Reception",patientsThroughReception, "");
        addIndicator(dsd, "", "Patients Served At Triage", patientsThroughTriage, "");
        addIndicator(dsd, "", "Patients Served At Counselor", patientsThroughCounselor, "");
        addIndicator(dsd, "", "Patients Served At Clinicians", patientsThroughARTClinician, "");
        addIndicator(dsd, "", "Patients Served At Lab ", patientsThroughLab, "");
        addIndicator(dsd, "", "Patients Served At Pharmacy", patientsThroughPharmacy, "");
        return rd;
    }



    public void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition, String dimensionOptions) {
        CohortIndicator ci = new CohortIndicator();
        ci.addParameter(ReportingConstants.START_DATE_PARAMETER);
        ci.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ci.setType(CohortIndicator.IndicatorType.COUNT);
        ci.setCohortDefinition(Mapped.mapStraightThrough(cohortDefinition));
        dsd.addColumn(key, label, Mapped.mapStraightThrough(ci), dimensionOptions);
    }


    public CohortDefinition addParameters(CohortDefinition cohortDefinition) {
        return df.convert(cohortDefinition, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate"));
    }

    @Override
    public String getVersion() {
        return "0.0.1";
    }
}