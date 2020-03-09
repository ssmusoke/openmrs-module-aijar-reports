package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.Location;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.EMRVersionDatasetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.TodayDateDatasetDefinition;
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

    public String getJSONDesignUuid() {
        return "13cf6468-07c2-40f5-a388-6fdc8fa8341e";
    }

    @Override
    public String getUuid() {
        return "dcd1f91a-04c8-4ae1-ac44-6abfdc91c98a";
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
        List<ReportDesign> l = new ArrayList<ReportDesign>();
        l.add(buildJSONReportDesign(reportDefinition));
        return l;
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
        return createExcelTemplateDesign("351284d3-a0da-4632-81e0-23e2d4777504", reportDefinition, "METRICS.xls");
    }


    public ReportDesign buildJSONReportDesign(ReportDefinition reportDefinition) {
        return createJSONTemplateDesign(getJSONDesignUuid(), reportDefinition, "METRICS.json");
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
        rd.addDataSetDefinition("aijar", Mapped.mapStraightThrough(getAijarVersion()));
        rd.addDataSetDefinition("S", Mapped.mapStraightThrough(CommonDatasetLibrary.settings()));
        rd.addDataSetDefinition("date", Mapped.mapStraightThrough(getDateToday()));

        Location reception = commonDimensionLibrary.getLocationByUuid("4501e132-07a2-4201-9dc8-2f6769b6d412");
        Location triage = commonDimensionLibrary.getLocationByUuid("ff01eaab-561e-40c6-bf24-539206b521ce");
        Location counselor = commonDimensionLibrary.getLocationByUuid("7c231e1a-1db5-11ea-978f-2e728ce88125");
        Location ART_Clinician = commonDimensionLibrary.getLocationByUuid("86863db4-6101-4ecf-9a86-5e716d6504e4");
        Location Lab = commonDimensionLibrary.getLocationByUuid("ba158c33-dc43-4306-9a4a-b4075751d36c");
        Location Pharmacy = commonDimensionLibrary.getLocationByUuid("3ec8ff90-3ec1-408e-bf8c-22e4553d6e17");


        CohortDefinition ARTEncounter =  df.getAnyEncounterOfTypesBetweenDates(hivMetadata.getARTEncounterPageEncounterType());
        CohortDefinition ARTSummary=  df.getAnyEncounterOfTypesBetweenDates(hivMetadata.getARTSummaryPageEncounterType());
        CohortDefinition eidSummary=  df.getAnyEncounterOfTypesBetweenDates(hivMetadata.getEIDSummaryPageEncounterType() );
        CohortDefinition missedAppointment=  df.getAnyEncounterOfTypesBetweenDates(hivMetadata.getMissedAppointmentRegisterEncounterType());
        CohortDefinition healthEducation=  df.getAnyEncounterOfTypesBetweenDates(Arrays.asList(artCohortLibrary.ARTHealthEducationEncounterType()));
        CohortDefinition HTSEncounter =  df.getAnyEncounterOfTypesBetweenDates(hivMetadata.getHCTEncounterType());
        CohortDefinition EIDCounter =  df.getAnyEncounterOfTypesBetweenDates(hivMetadata.getEIDEncounterPageEncounterType());

        CohortDefinition patientsThroughReception = df.getAnyEncounterOfTypesBetweenDates(null, Arrays.asList(reception));
        CohortDefinition patientsThroughTriage = df.getAnyEncounterOfTypesBetweenDates(null,Arrays.asList(triage));
        CohortDefinition patientsThroughCounselor = df.getAnyEncounterOfTypesBetweenDates(null,Arrays.asList(counselor));
        CohortDefinition patientsThroughLab = df.getAnyEncounterOfTypesBetweenDates(null,Arrays.asList(Lab));
        CohortDefinition patientsThroughPharmacy = df.getAnyEncounterOfTypesBetweenDates(null,Arrays.asList(Pharmacy));
        CohortDefinition patientsThroughARTClinician = df.getAnyEncounterOfTypesBetweenDates(null,Arrays.asList(ART_Clinician));

        addIndicator(dsd, "a", "ART Encounter", ARTEncounter, "");
        addIndicator(dsd, "b", "HTS Encounters", HTSEncounter, "");
        addIndicator(dsd, "c", "EID Summary",eidSummary, "");
        addIndicator(dsd, "d", "EID Encounter",EIDCounter, "");
        addIndicator(dsd, "e", "missed appointment encounter", missedAppointment, "");
        addIndicator(dsd, "f", "Patients Served At Reception",patientsThroughReception, "");
        addIndicator(dsd, "g", "Patients Served At Triage", patientsThroughTriage, "");
        addIndicator(dsd, "h", "Patients Served At Counselor", patientsThroughCounselor, "");
        addIndicator(dsd, "i", "Patients Served At Clinicians", patientsThroughARTClinician, "");
        addIndicator(dsd, "j", "Patients Served At Lab ", patientsThroughLab, "");
        addIndicator(dsd, "k", "Patients Served At Pharmacy", patientsThroughPharmacy, "");
        addIndicator(dsd, "l", " ART summary ", ARTSummary, "");
        addIndicator(dsd, "m", "art health education", healthEducation, "");
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

    public DataSetDefinition getAijarVersion(){
        EMRVersionDatasetDefinition dsd= new EMRVersionDatasetDefinition();
        return dsd;
    }

    public DataSetDefinition getDateToday(){
        TodayDateDatasetDefinition dsd= new TodayDateDatasetDefinition();
        return dsd;
    }

    @Override
    public String getVersion() {
        return "0.3";
    }
}