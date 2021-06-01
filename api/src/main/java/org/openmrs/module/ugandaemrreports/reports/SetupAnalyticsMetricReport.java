package org.openmrs.module.ugandaemrreports.reports;

import com.google.common.base.Joiner;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.ObjectUtil;
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
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.ARTCohortLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


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

    @Autowired
    private SharedDataDefintion sharedDataDefintion;


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
        rd.addDataSetDefinition("aijar", Mapped.mapStraightThrough(getUgandaEMRVersion()));
        rd.addDataSetDefinition("S", Mapped.mapStraightThrough(CommonDatasetLibrary.settings()));
        rd.addDataSetDefinition("date", Mapped.mapStraightThrough(getDateToday()));
        rd.addDataSetDefinition("H", Mapped.mapStraightThrough(sharedDataDefintion.healthFacilityName()));

        Location reception = commonDimensionLibrary.getLocationByUuid("4501e132-07a2-4201-9dc8-2f6769b6d412");
        Location triage = commonDimensionLibrary.getLocationByUuid("ff01eaab-561e-40c6-bf24-539206b521ce");
        Location counselor = commonDimensionLibrary.getLocationByUuid("7c231e1a-1db5-11ea-978f-2e728ce88125");
        Location ART_Clinician = commonDimensionLibrary.getLocationByUuid("86863db4-6101-4ecf-9a86-5e716d6504e4");
        Location Lab = commonDimensionLibrary.getLocationByUuid("ba158c33-dc43-4306-9a4a-b4075751d36c");
        Location Pharmacy = commonDimensionLibrary.getLocationByUuid("3ec8ff90-3ec1-408e-bf8c-22e4553d6e17");
        Location Community = commonDimensionLibrary.getLocationByUuid("841cb8d9-b662-41ad-9e7f-d476caac48aa");

        VisitType muzima= Context.getVisitService().getVisitTypeByUuid("1d697d92-a000-11ea-b1a0-d0577bb73cd4");
        VisitType community= Context.getVisitService().getVisitTypeByUuid("2ce24f40-8f4c-4bfa-8fde-09d475783468");
        VisitType facility= Context.getVisitService().getVisitTypeByUuid("7b0f5697-27e3-40c4-8bae-f4049abfb4ed");


        CohortDefinition ARTEncounter =  getNumbersEncountersDuringPeriodBy(null,hivMetadata.getARTEncounterPageEncounterType(),null);
        CohortDefinition ARTSummary=  getNumbersEncountersDuringPeriodBy(null,hivMetadata.getARTSummaryPageEncounterType(),null);
        CohortDefinition eidSummary=  getNumbersEncountersDuringPeriodBy(null,hivMetadata.getEIDSummaryPageEncounterType(),null );
        CohortDefinition missedAppointment=  getNumbersEncountersDuringPeriodBy(null,hivMetadata.getMissedAppointmentRegisterEncounterType(),null);
        CohortDefinition healthEducation=  getNumbersEncountersDuringPeriodBy(null,Arrays.asList(artCohortLibrary.ARTHealthEducationEncounterType()),null);
        CohortDefinition HTSEncounter =  getNumbersEncountersDuringPeriodBy(null,hivMetadata.getHCTEncounterType(),null);
        CohortDefinition EIDCounter =  getNumbersEncountersDuringPeriodBy(null,hivMetadata.getEIDEncounterPageEncounterType(),null);

        CohortDefinition patientsThroughReception = getNumbersEncountersDuringPeriodBy(null,null, reception);
        CohortDefinition patientsThroughTriage = getNumbersEncountersDuringPeriodBy(null,null,triage);
        CohortDefinition patientsThroughCounselor = getNumbersEncountersDuringPeriodBy(null,null,counselor);
        CohortDefinition patientsThroughLab = getNumbersEncountersDuringPeriodBy(null,null,Lab);
        CohortDefinition patientsThroughPharmacy = getNumbersEncountersDuringPeriodBy(null,null,Pharmacy);
        CohortDefinition patientsThroughARTClinician = getNumbersEncountersDuringPeriodBy(null,null,ART_Clinician);
        CohortDefinition patientsThroughCommunity = getNumbersEncountersDuringPeriodBy(null,null,Community);

        addIndicator(dsd, "l", " ART summary ", ARTSummary, "");
        addIndicator(dsd, "a", "ART Encounter", ARTEncounter, "");
        addIndicator(dsd, "m", "art health education", healthEducation, "");
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
        addIndicator(dsd, "n", "Patients Served through Community", patientsThroughCommunity, "");

        addIndicator(dsd,"o", "ART Summary from community", getNumbersEncountersDuringPeriodBy(Arrays.asList(community),hivMetadata.getARTSummaryPageEncounterType(),null),"");
        addIndicator(dsd,"p", "ART Summary from facility", getNumbersEncountersDuringPeriodBy(Arrays.asList(facility),hivMetadata.getARTSummaryPageEncounterType(),null),"");
        addIndicator(dsd,"q", "ART Encounter from community", getNumbersEncountersDuringPeriodBy(Arrays.asList(community),hivMetadata.getARTEncounterPageEncounterType(),null),"");
        addIndicator(dsd,"r", "ART Encounter from facility", getNumbersEncountersDuringPeriodBy(Arrays.asList(facility),hivMetadata.getARTEncounterPageEncounterType(),null),"");
        addIndicator(dsd,"s", "healthEducation from community", getNumbersEncountersDuringPeriodBy(Arrays.asList(community),Arrays.asList(artCohortLibrary.ARTHealthEducationEncounterType()),null),"");
        addIndicator(dsd,"t", "healthEducation from facility", getNumbersEncountersDuringPeriodBy(Arrays.asList(facility),Arrays.asList(artCohortLibrary.ARTHealthEducationEncounterType()),null),"");
        addIndicator(dsd,"u", "HTSEncounter from community", getNumbersEncountersDuringPeriodBy(Arrays.asList(community),hivMetadata.getHCTEncounterType(),null),"");
        addIndicator(dsd,"v", "HTSEncounter from facility", getNumbersEncountersDuringPeriodBy(Arrays.asList(facility),hivMetadata.getHCTEncounterType(),null),"");
        addIndicator(dsd,"w", "eidSummary from community", getNumbersEncountersDuringPeriodBy(Arrays.asList(community),hivMetadata.getEIDSummaryPageEncounterType(),null),"");
        addIndicator(dsd,"x", "eidSummary from facility", getNumbersEncountersDuringPeriodBy(Arrays.asList(facility),hivMetadata.getEIDSummaryPageEncounterType(),null),"");
        addIndicator(dsd,"y", "EIDCounter from community", getNumbersEncountersDuringPeriodBy(Arrays.asList(community),hivMetadata.getEIDEncounterPageEncounterType(),null),"");
        addIndicator(dsd,"z", "EIDCounter from facility",  getNumbersEncountersDuringPeriodBy(Arrays.asList(facility),hivMetadata.getEIDEncounterPageEncounterType(),null),"");
        addIndicator(dsd,"a1", "missedAppointment from community",  getNumbersEncountersDuringPeriodBy(Arrays.asList(community),hivMetadata.getMissedAppointmentEncounterType(),null),"");
        addIndicator(dsd,"a2", "missedAppointment from facility", getNumbersEncountersDuringPeriodBy(Arrays.asList(facility),hivMetadata.getMissedAppointmentEncounterType(),null),"");

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

    public DataSetDefinition getUgandaEMRVersion(){
        EMRVersionDatasetDefinition dsd= new EMRVersionDatasetDefinition();
        return dsd;
    }

    public DataSetDefinition getDateToday(){
        TodayDateDatasetDefinition dsd= new TodayDateDatasetDefinition();
        return dsd;
    }

    /**
     *
     * @param visitTypes
     * @param encounterTypes
     * @param location
     * @return
     * @should return all patients with encounters during the period  if all parameters are null
     * @should return correct patients when one of  the parameters is set
     * @should return correct patients when all the parameters are set
     */
    public CohortDefinition getNumbersEncountersDuringPeriodBy(List<VisitType> visitTypes, List<EncounterType>encounterTypes, Location location) {
        String query = "select encounter_id from encounter e inner join encounter_type et on e.encounter_type = et.encounter_type_id " +
                "inner join visit v on e.visit_id = v.visit_id inner join visit_type vt on v.visit_type_id = vt.visit_type_id left join location l on e.location_id = l.location_id" +
                "  where encounter_datetime between :startDate and :endDate and e.voided=0 ";
        String whereClause ="";
        if(visitTypes!=null && !visitTypes.isEmpty()) {
         String ids=Joiner.on(",").join(visitTypes.stream().map(VisitType::getId).collect(Collectors.toList()));
            whereClause+= "and vt.visit_type_id in ("+ids+") ";
        }

        if(encounterTypes!=null && !encounterTypes.isEmpty()) {
         String ids=Joiner.on(",").join(encounterTypes.stream().map(EncounterType::getId).collect(Collectors.toList()));
            whereClause+= "and et.encounter_type_id in ("+ids+") ";
        }
        if(location!=null) {
            whereClause+= "and l.location_id in ("+location.getId()+") ";
        }
        query = query +whereClause;
        SqlCohortDefinition cohortDefinition = new SqlCohortDefinition(query);
        cohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
        cohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));

        return df.convert(cohortDefinition, ObjectUtil.toMap("startDate=startDate,endDate=endDate"));

    }

    @Override
    public String getVersion() {
        return "0.3.2";
    }
}