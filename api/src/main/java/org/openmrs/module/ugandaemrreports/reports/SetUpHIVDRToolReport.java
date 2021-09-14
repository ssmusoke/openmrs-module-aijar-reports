package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObsValueConverter;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.common.Enums;
import org.openmrs.module.ugandaemrreports.data.converter.ObsDataConverter;
import org.openmrs.module.ugandaemrreports.definition.data.converter.*;
import org.openmrs.module.ugandaemrreports.definition.data.definition.AdherencePatientDataDefinition;
import org.openmrs.module.ugandaemrreports.definition.data.definition.ClientCareStatusDataDefinition;
import org.openmrs.module.ugandaemrreports.definition.data.definition.IACPatientDataDefinition;
import org.openmrs.module.ugandaemrreports.definition.data.definition.ObsForPersonInPeriodDataDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.ARTCohortLibrary;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.CommonCohortLibrary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary.getConcept;
import static org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary.getConceptList;

/**
 */
@Component
public class SetUpHIVDRToolReport extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    SharedDataDefintion sdd;


    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    @Autowired
    HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private BuiltInPatientDataLibrary builtInPatientData;

    @Autowired
    private HIVPatientDataLibrary hivPatientData;

    @Autowired
    private BasePatientDataLibrary basePatientData;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private CommonCohortLibrary commonCohortLibrary;

    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getUuid() {
        return "8c029f5e-4467-482c-9ca5-6c32982286dd";
    }

    @Override
    public String getExcelDesignUuid() {
        return "8b13560e-9b63-4d2b-af59-fd1a7e1c6695";
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        List<ReportDesign> l = new ArrayList<ReportDesign>();
        l.add(buildReportDesign(reportDefinition));
        return l;
    }

    /**
     * Build the report design for the specified report, this allows a user to override the report design by adding properties and other metadata to the report design
     * @SolemaBrothers
     * @param reportDefinition
     * @return The report design
     */
    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "HIVDR_Tool.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:4,dataset:NON_SUPPRESSED_VIRAL_LOAD");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }


    @Override
    public String getName() {
        return "HIVDR Tool Report";
    }

    @Override
    public String getDescription() {
        return "HIVDR Tool";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(df.getStartDateParameter());
        l.add(df.getEndDateParameter());
        return l;
    }

    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();

        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        PatientDataSetDefinition dsd = new PatientDataSetDefinition();
        CohortDefinition nonSupressedVL = df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getViralLoadCopies(),hivMetadata.getARTEncounterPageEncounterType(), RangeComparator.GREATER_EQUAL,1000.0, BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition onATVOrLPVRegimenDuringPeriod = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), getATVAndLPVRRegimens(),BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition adultsAbove19Years = commonCohortLibrary.agedAtLeast(19);
        CohortDefinition firstCohort = df.getPatientsInAll(adultsAbove19Years,nonSupressedVL,onATVOrLPVRegimenDuringPeriod);

        CohortDefinition below19Years = commonCohortLibrary.agedAtMost(18);
        CohortDefinition secondCohort  = df.getPatientsInAll(below19Years,nonSupressedVL);

        CohortDefinition reportCohort =df.getPatientsInAny(firstCohort,secondCohort);

        dsd.setName(getName());
        dsd.setParameters(getParameters());
        dsd.addRowFilter(Mapped.mapStraightThrough(reportCohort));
        addColumn(dsd, "ID", hivPatientData.getClinicNumber());
        addColumn(dsd, "Age", hivPatientData.getAgeDuringPeriod());
        addColumn(dsd, "Sex", builtInPatientData.getGender());
        dsd.addColumn("Weight", sdd.definition("Weight", getConcept("5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("emtct_status", sdd.definition("pregnant", getConcept("dcda5179-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        addColumn(dsd,"ARTStartDate",hivPatientData.getArtStartDate());
        addColumn(dsd,"DSDM", hivPatientData.getDSDMModel());
        addColumn(dsd,"DSDM_Date",hivPatientData.getDSDMEnrollmentDate());
        addColumn(dsd, "Current_Regimen", hivPatientData.getCurrentRegimen());
        addColumn(dsd, "Regimen_Date", hivPatientData.getCurrentRegimenStartDate());
        addColumn(dsd, "VL_Date", hivPatientData.getLastViralLoadDateByEndDate());

        addColumn(dsd,"optimized",getObsHasValue(hivMetadata.getCurrentRegimen(),null,Dictionary.getConceptList(Metadata.Concept.DTG_TLD_REGIMEN_LIST), new YesConverter()));
        dsd.addColumn("Adherence",sdd.definition("Adherence",getConcept("dce03b2f-30ab-102d-86b0-7a5022ba4115")),"onOrAfter=${startDate},onOrBefore=${endDate}",new ObsDataConverter());
        dsd.addColumn("Reason",sdd.definition("Reason for poor Adherence",getConcept("dce045a4-30ab-102d-86b0-7a5022ba4115")),"onOrAfter=${startDate},onOrBefore=${endDate}",new ObsDataConverter());

        addColumn(dsd,"client_status",getClientStatus());
        dsd.addColumn("pss3",sdd.definition("pss3",hivMetadata.getConcept("1760ea50-8f05-4675-aedd-d55f99541aa8")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("pss10",sdd.definition("pss10",hivMetadata.getConcept("eb7c1c34-59e5-46d5-beba-626694badd54")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        addColumn(dsd,"IAC_Done",df.getObsByEndDate(hivMetadata.getConcept("164988AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), null, TimeQualifier.LAST, new YesConverter()));
        addColumn(dsd,"1st_IAC_Completion",getIAC(0,"date"));
        addColumn(dsd,"1st_IAC_Outcome",getIAC(0,"outcome"));
        addColumn(dsd,"2nd_IAC_Completion",getIAC(1,"date"));
        addColumn(dsd,"2nd_IAC_Outcome",getIAC(1,"outcome"));
        addColumn(dsd,"3rd_IAC_Completion",getIAC(2,"date"));
        addColumn(dsd,"3rd_IAC_Outcome",getIAC(2,"outcome"));
        addColumn(dsd,"4th_IAC_Completion",getIAC(3,"date"));
        addColumn(dsd,"4th_IAC_Outcome",getIAC(3,"outcome"));
        addColumn(dsd,"5th_IAC_Completion",getIAC(4,"date"));
        addColumn(dsd,"5th_IAC_Outcome",getIAC(4,"outcome"));
        addColumn(dsd,"6th_IAC_Completion",getIAC(5,"date"));
        addColumn(dsd,"6th_IAC_Outcome",getIAC(5,"outcome"));
        addColumn(dsd,"vl_results_after_IAC",df.getObsByEndDate(getConcept("dca12261-30ab-102d-86b0-7a5022ba4115"), hivMetadata.getIACEncounters(), TimeQualifier.LAST, new ObsValueConverter()));
        addColumn(dsd,"vl_date_after_IAC",df.getObsByEndDate(getConcept("163150AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), hivMetadata.getIACEncounters(), TimeQualifier.LAST, new ObsValueConverter()));
        addColumn(dsd,"repeat_vl",df.getObsByEndDate(getConcept("0b434cfa-b11c-4d14-aaa2-9aed6ca2da88"), hivMetadata.getIACEncounters(), TimeQualifier.LAST, new ObsValueConverter()));
        addColumn(dsd,"HIVDR_Date",df.getObsByEndDate(getConcept("b913c0d9-f279-4e43-bb8e-3d1a4cf1ad4d"), hivMetadata.getIACEncounters(), TimeQualifier.LAST, new ObsValueConverter()));
        rd.addDataSetDefinition("NON_SUPPRESSED_VIRAL_LOAD", Mapped.mapStraightThrough(dsd));
        rd.setBaseCohortDefinition(Mapped.mapStraightThrough(reportCohort));

        return rd;
    }


    public PatientDataDefinition getObsHasValue(Concept question, List<EncounterType> encounterTypes, List<Concept> answers, DataConverter converter) {
        ObsForPersonInPeriodDataDefinition def = new ObsForPersonInPeriodDataDefinition();
        def.setAnswers(answers);
        def.setQuestion(question);
        def.setEncounterTypes(encounterTypes);
        def.setWhichEncounter(TimeQualifier.ANY);
        def.setPeriod(Enums.Period.YEARLY);
        def.setPeriodToAdd(1);
        def.addParameter(new Parameter("startDate", "Start Date", Date.class));
        return df.convert(def, ObjectUtil.toMap("startDate=startDate"),converter );
    }

    public List<Concept> getATVAndLPVRRegimens() {
        return hivMetadata.getConceptList("14c56659-3d4e-4b88-b3ff-e2d43dbfb865,25186d70-ed8f-486c-83e5-fc31cbe95630,fe78521e-eb7a-440f-912d-0eb9bf2d4b2c,942e427c-7a3b-49b6-97f3-5cdbfeb8d0e3,29439504-5f5d-49ac-b8e4-258adc08c67a," +
                "d4393bd0-3a9e-4716-8968-1057c58c32bc,faf13d3c-7ca8-4995-ab29-749f3960b83d,f00e5ff7-73bb-4385-8ee1-ea7aa772ec3e,4b9c639e-3d06-4f2a-9c34-dd07e44f4fa6,d239c3d5-d296-4458-b49d-8501258886e5,b06bdb63-dd08-4b80-af5a-d17f6b3062a5,f30e9dae-cc6a-4669-98d5-ad25b8a3ce9c," +
                "4a608d68-516f-44d2-9e0b-1783dc0d870e,dd2b9181-30ab-102d-86b0-7a5022ba4115");
    }

    public PatientDataDefinition getIAC(Integer number,String parameter) {
        IACPatientDataDefinition def = new IACPatientDataDefinition();
        def.addParameter(new Parameter("startDate", "startDate", Date.class));
        return df.convert(def, new IACConverter(number,parameter));
    }

    public PatientDataDefinition getClientStatus(){
        ClientCareStatusDataDefinition def = new ClientCareStatusDataDefinition();
        return df.convert(def,ObjectUtil.toMap("startDate=startDate,endDate=endDate"),new ClientCareStatusConverter());
    }
    @Override
    public String getVersion() {
        return "0.3.9";
    }
}



