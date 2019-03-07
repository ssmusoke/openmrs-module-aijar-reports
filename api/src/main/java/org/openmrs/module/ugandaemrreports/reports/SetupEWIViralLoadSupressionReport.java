package org.openmrs.module.ugandaemrreports.reports;

import org.joda.time.LocalDate;
import org.openmrs.Concept;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.AgeConverter;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.data.person.definition.*;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.data.converter.*;
import org.openmrs.module.ugandaemrreports.definition.data.converter.BirthDateConverter;
import org.openmrs.module.ugandaemrreports.definition.data.definition.CalculationDataDefinition;
import org.openmrs.module.ugandaemrreports.definition.data.definition.DeathDateDataDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.NameOfHealthUnitDatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.*;
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 */
@Component
public class SetupEWIViralLoadSupressionReport extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    SharedDataDefintion sdd;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    @Autowired
    private BuiltInPatientDataLibrary builtInPatientData;

    @Autowired
    private HIVPatientDataLibrary hivPatientData;

    @Autowired
    private BasePatientDataLibrary basePatientData;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private CommonCohortDefinitionLibrary commonCohortDefinitionLibrary;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "99c052c6-38e1-4e55-8558-dc95b3a4c6eb";
    }

    @Override
    public String getUuid() {
        return "01f91ef5-9e52-4bf1-af5c-16438c7794f0";
    }

    @Override
    public String getName() {
        return "Early Warning Indicators - Viral Load Supression";
    }

    @Override
    public String getDescription() {
        return "Viral Load Supression";
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "EWIViralLoadSupression.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:16,dataset:V_L_S");
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
        rd.addDataSetDefinition("HC", Mapped.mapStraightThrough(healthFacilityName()));
        String params = "startDate=${startDate},endDate=${endDate}";

        CohortIndicatorDataSetDefinition cd = new CohortIndicatorDataSetDefinition();
        rd.addDataSetDefinition("CD", Mapped.mapStraightThrough(cd));
        PatientDataSetDefinition dsd = new PatientDataSetDefinition();
        dsd.setParameters(getParameters());

        rd.addDataSetDefinition("V_L_S", Mapped.mapStraightThrough(dsd));

        CohortDefinition startedART12MonthsAgo = hivCohortDefinitionLibrary.getArtStartDateBetweenPeriod("12m");
        CohortDefinition haveViralLoadDuringPeriod = hivCohortDefinitionLibrary.getPatientsWithViralLoadDuringPeriod();
        CohortDefinition haveViralLoadDuringPeriod11Months  = hivCohortDefinitionLibrary.getPatientsWithViralLoadDuringPeriod("1m");
        CohortDefinition haveViralLoadDuringPeriod10Months  = hivCohortDefinitionLibrary.getPatientsWithViralLoadDuringPeriod("2m");
        CohortDefinition haveViralLoadDuringPeriod9Months  = hivCohortDefinitionLibrary.getPatientsWithViralLoadDuringPeriod("3m");

        CohortDefinition haveViralLoadDuringPeriod13Months  = hivCohortDefinitionLibrary.getPatientsWithViralLoadDuringPeriodPlusMnths("1m");
        CohortDefinition haveViralLoadDuringPeriod14Months  = hivCohortDefinitionLibrary.getPatientsWithViralLoadDuringPeriodPlusMnths("2m");
        CohortDefinition haveViralLoadDuringPeriod15Months  = hivCohortDefinitionLibrary.getPatientsWithViralLoadDuringPeriodPlusMnths("3m");
        CohortDefinition viralloadtaken = df.getPatientsInAny(haveViralLoadDuringPeriod,haveViralLoadDuringPeriod9Months,
                haveViralLoadDuringPeriod10Months,haveViralLoadDuringPeriod11Months,haveViralLoadDuringPeriod13Months,haveViralLoadDuringPeriod14Months,haveViralLoadDuringPeriod15Months);
        CohortDefinition cohort = df.getPatientsInAll(startedART12MonthsAgo,viralloadtaken);

        rd.setBaseCohortDefinition(Mapped.mapStraightThrough(cohort));

        dsd.setName(getName());
        dsd.setParameters(getParameters());
        dsd.addRowFilter(Mapped.mapStraightThrough(cohort));

        addIndicator(cd,"noOfPatients","",cohort,"");

        addColumn(dsd,"PatientID", builtInPatientData.getPatientId());
        addColumn( dsd,"Sex", builtInPatientData.getGender());
        dsd.addColumn("DOB", new BirthdateDataDefinition(), "", new BirthdateConverter("dd/MM/yyyy"));
        dsd.addColumn("Age", new AgeDataDefinition(), "", new AgeConverter("{y}"));
        addColumn(dsd,"intiationDate",hivPatientData.getARTStartDate());
        addColumn(dsd, "viralLoadDate", hivPatientData.getLastViralLoadDateByEndDatePlusMonths("3m"));
        addColumn(dsd, "viralLoad", hivPatientData.getViralLoadByEndDatePlusMonths("3m"));
        if(hivPatientData.getTODateDuringPeriod() !=null) {
            addColumn(dsd, "transferOutorDeathDate", hivPatientData.getTransferredOutDateByEndDate());
        }else {
            addColumn(dsd,"transferOutorDeathDate", hivPatientData.getDeathDateByEndDate());
        }



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

    private DataSetDefinition healthFacilityName() {
        NameOfHealthUnitDatasetDefinition dsd = new NameOfHealthUnitDatasetDefinition();
        dsd.setFacilityName("aijar.healthCenterName");
        return dsd;
    }

    @Override
    public String getVersion() {
        return "1.7.0";
    }
}
