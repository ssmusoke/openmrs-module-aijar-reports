package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.ObsForPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.data.converter.BirthDateConverter;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata;
import org.openmrs.module.ugandaemrreports.reporting.utils.CoreUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Component
public class SetupDueForViralLoad extends UgandaEMRDataExportManager {
    @Autowired
    private DataFactory df;

    @Autowired
    CommonCohortDefinitionLibrary definitionLibrary;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    @Autowired
    private BuiltInPatientDataLibrary builtInPatientData;

    @Autowired
    private HIVPatientDataLibrary hivPatientData;

    @Autowired
    private BasePatientDataLibrary basePatientData;

    @Autowired
    private CommonCohortDefinitionLibrary commonCohortDefinitionLibrary;

    @Autowired
    private HIVMetadata hivMetadata;
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private Moh105CohortLibrary cohortLibrary;

    @Autowired
    SharedDataDefintion sdd;

    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "19ec94ea-defe-4b1d-ae29-79c13de557b3";
    }

    @Override
    public String getUuid() {
        return "23ef26dd-8e26-4109-b8f2-d102a119b901";
    }

    @Override
    public String getName() {
        return "Due For Viral Load";
    }

    @Override
    public String getDescription() {
        return "Due For Viral Load";
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
        l.add(buildReportDesign(reportDefinition));
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "DueForViralLoad.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:8,dataset:DUE_FOR_VIRAL_LOAD");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }

    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();

        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        PatientDataSetDefinition dsd = new PatientDataSetDefinition();

        CohortDefinition onArtDuringQuarter = hivCohortDefinitionLibrary.getPatientsHavingRegimenDuringPeriod();
        CohortDefinition onArtBeforeQuarter = hivCohortDefinitionLibrary.getPatientsHavingRegimenBeforePeriod();
        CohortDefinition onArt = df.getPatientsInAny(onArtBeforeQuarter,onArtDuringQuarter);
        CohortDefinition onArtAnd1stANC = df.getPatientsInAll(onArt,anc1stVisitDuringPeriod());
        CohortDefinition onARTFor6Months = hivCohortDefinitionLibrary.getPatientsWhoStartedArtMonthsAgo("6m");
        CohortDefinition viralLoadDuringperiod = hivCohortDefinitionLibrary.getPatientsWithViralLoadDuringPeriod();
        CohortDefinition viralLoadByEndOfPeriod = hivCohortDefinitionLibrary.getPatientsWithLastViralLoadByEndDate();
        CohortDefinition onEMTCTDuringPeriod = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getEMTCTAtEnrollment(),
                Arrays.asList(hivMetadata.getARTEncounterEncounterType()),Arrays.asList(hivMetadata.getYes()), BaseObsCohortDefinition.TimeModifier.FIRST);

        CohortDefinition startedEMTCTLast6Months =df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getEMTCTAtEnrollment(),
                                                        Arrays.asList(hivMetadata.getARTEncounterEncounterType()),Arrays.asList(hivMetadata.getYes()),"6m", BaseObsCohortDefinition.TimeModifier.FIRST);
        CohortDefinition adultsDueForViralLoad = df.getPatientsInAll(commonCohortDefinitionLibrary.MoHAdult(),
                hivCohortDefinitionLibrary.getPatientsWhoseLastViralLoadWasMonthsAgoFromPeriod("12m"));

        CohortDefinition childDueForViralLoad = df.getPatientsInAll(commonCohortDefinitionLibrary.MoHChildren(),
                hivCohortDefinitionLibrary.getPatientsWhoseLastViralLoadWasMonthsAgoFromPeriod("6m"));
        CohortDefinition firstANCOrEMTCTDuringPeriod = df.getPatientsInAny(onArtAnd1stANC,onEMTCTDuringPeriod);

        CohortDefinition firstANCOrEMTCTAndNoViralLoadTaken = df.getPatientsNotIn(firstANCOrEMTCTDuringPeriod,viralLoadDuringperiod);
        CohortDefinition hadFirstAncOrEmtct6MonthsFromPeriod = df.getPatientsInAny(anc1stVisit6MonthsFromPeriod(), startedEMTCTLast6Months);

        CohortDefinition hadFirstAncOrEmtct6MonthsFromPeriodAndLastViralLoadWas6MonthsAgo =df.getPatientsInAll(hadFirstAncOrEmtct6MonthsFromPeriod,
                hivCohortDefinitionLibrary.getPatientsWhoseLastViralLoadWasMonthsAgoFromPeriod("6m"));

        CohortDefinition deadPatients = df.getDeadPatientsDuringPeriod();
        CohortDefinition transferedOut = hivCohortDefinitionLibrary.getPatientsTransferredOutDuringPeriod();
        CohortDefinition patientsDeadAndtransferedOut =df.getPatientsInAny(deadPatients,transferedOut);


        CohortDefinition onArtFor6MonthsAndNoViralLoadTaken = df.getPatientsNotIn(onARTFor6Months,viralLoadByEndOfPeriod);
        CohortDefinition dueForViralLoad = df.getPatientsInAny(onArtFor6MonthsAndNoViralLoadTaken,childDueForViralLoad,adultsDueForViralLoad,
                firstANCOrEMTCTAndNoViralLoadTaken,hadFirstAncOrEmtct6MonthsFromPeriodAndLastViralLoadWas6MonthsAgo);

        CohortDefinition activeAndDueForViralLoad = df.getPatientsNotIn(dueForViralLoad,patientsDeadAndtransferedOut);
        dsd.setName(getName());
        dsd.setParameters(getParameters());
        dsd.addRowFilter(Mapped.mapStraightThrough(activeAndDueForViralLoad));
        addColumn(dsd, "Clinic No", hivPatientData.getClinicNumber());
        dsd.addColumn( "Patient Name",  new PreferredNameDataDefinition(), (String) null);
        dsd.addColumn( "Sex", new GenderDataDefinition(), (String) null);
        dsd.addColumn("Birth Date", builtInPatientData.getBirthdate(), "", new BirthDateConverter());
        addColumn(dsd, "Age", hivPatientData.getAgeDuringPeriod());
        addColumn(dsd,"Parish",df.getPreferredAddress("address4"));
        addColumn(dsd,"Village",df.getPreferredAddress("address5"));
        addColumn(dsd, "HIV Enrolled Date", hivPatientData.getSummaryPageDate());
        addColumn(dsd, "ART Start Date", hivPatientData.getARTStartDate());
        addColumn(dsd, "Viral Load Date", hivPatientData.getLastViralLoadDateByEndDate());
        addColumn(dsd, "Viral Load Qualitative", hivPatientData.getVLQualitativeByEndDate());
        addColumn(dsd, "Viral Load", hivPatientData.getViralLoadByEndDate());
        addColumn(dsd, "Last Visit Date", hivPatientData.getLastEncounterByEndDate());
        addColumn(dsd, "Appointment Date", hivPatientData.getLastReturnDateByEndDate());
        addColumn(dsd, "Telephone", basePatientData.getTelephone());
        addColumn(dsd,"pregnant",getObsDuringPeriod(hivMetadata.getEMTCTAtEnrollment(), null, TimeQualifier.FIRST, "6m", df.getObsValueCodedConverter()));


        rd.addDataSetDefinition("DUE_FOR_VIRAL_LOAD", Mapped.mapStraightThrough(dsd));
        rd.setBaseCohortDefinition(Mapped.mapStraightThrough(activeAndDueForViralLoad));

        return rd;
    }

    public CohortDefinition anc1stVisitDuringPeriod(){
        return cohortLibrary.femaleAndHasAncVisit(0.0, 1.0);
    }

    public CohortDefinition anc1stVisit6MonthsFromPeriod(){
        return df.getPatientsWithNumericObsDuringPeriod(getConcept("801b8959-4b2a-46c0-a28f-f7d3fc8b98bb"),
                Arrays.asList(CoreUtils.getEncounterType(Metadata.EncounterType.ANC_ENCOUNTER)),"6m", RangeComparator.GREATER_THAN,0.0,RangeComparator.LESS_EQUAL,1.0, BaseObsCohortDefinition.TimeModifier.FIRST);
    }

    private Concept getConcept(String uuid) {
        return Dictionary.getConcept(uuid);
    }

    public PatientDataDefinition getObsDuringPeriod(Concept question, List<EncounterType> encounterTypes, TimeQualifier timeQualifier, String startDateolderThan, DataConverter converter) {
        ObsForPersonDataDefinition def = PatientColumns.createObsForPersonData(question, encounterTypes, Arrays.asList("onOrBefore", "onOrAfter"), timeQualifier);
        String startDate = Parameters.createParameterBeforeDuration("onOrAfter", "startDate", startDateolderThan);
        String endDate = Parameters.ON_OR_BEFORE_END_DATE;
        return df.createPatientDataDefinition(def, converter, Parameters.combineParameters(startDate, endDate));
    }

    @Override
    public String getVersion() {
        return "2.0";
    }
}

