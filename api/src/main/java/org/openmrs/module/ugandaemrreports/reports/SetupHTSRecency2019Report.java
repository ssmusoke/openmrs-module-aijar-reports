package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
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
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  MER 2.0 HTS RECENCY Report
 */
@Component
public class SetupHTSRecency2019Report extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;


    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    private CommonCohortDefinitionLibrary cohortDefinitionLibrary;

    @Autowired
    private HIVMetadata hivMetadata;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "819191e8-f762-4d49-b125-f67f1051c361";
    }

    public String getJSONDesignUuid() {
        return "b36697ff-54e6-4742-bdad-c4ccd9db9e9c";
    }

    @Override
    public String getUuid() {
        return "43636326-36c5-4104-b358-b912a038ee9d";
    }

    @Override
    public String getName() {
        return "HTS_RECENT Report";
    }

    @Override
    public String getDescription() {
        return "MER Indicator Report for HTS_RECENT";
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
        List<ReportDesign> l = new ArrayList<>();
        l.add(buildReportDesign(reportDefinition));
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "HTS_Recency_Report.xls");
    }

    public ReportDesign buildJSONReportDesign(ReportDefinition reportDefinition) {
        return createJSONTemplateDesign(getJSONDesignUuid(), reportDefinition, "HTS_Recency_Report.json");
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
        rd.addDataSetDefinition("HTS_RECENCY", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension ageDimension = commonDimensionLibrary.getTxNewAgeGenderGroup();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));


        CohortDefinition recentHIVInfectionDuringPeriod = hivCohortDefinitionLibrary.getPatientWithRecentHIVInfectionDuringPeriod();
        CohortDefinition testedForRecencyDuringPeriod = hivCohortDefinitionLibrary.getPatientWhoTestedForRecencyHIVInfectionDuringPeriod();

        CohortDefinition pregnantPatientDuringPeriod = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("927563c5-cb91-4536-b23c-563a72d3f829"),hivMetadata.getHCTEncounterType(),
                Arrays.asList(Dictionary.getConcept("dcda5179-30ab-102d-86b0-7a5022ba4115")), BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition PWIDS = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("927563c5-cb91-4536-b23c-563a72d3f829"),hivMetadata.getHCTEncounterType(),
                Arrays.asList(Dictionary.getConcept("160666AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition PIPS = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("927563c5-cb91-4536-b23c-563a72d3f829"),hivMetadata.getHCTEncounterType(),
                Arrays.asList(Dictionary.getConcept("162277AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition pregnantAndTestedWithRecentInfection = df.getPatientsInAll(pregnantPatientDuringPeriod,recentHIVInfectionDuringPeriod);
        CohortDefinition pregnantAndTestedForRecency = df.getPatientsInAll(pregnantPatientDuringPeriod,testedForRecencyDuringPeriod);

        CohortDefinition males = cohortDefinitionLibrary.males();
        CohortDefinition females = cohortDefinitionLibrary.females();

        addGender(dsd, "a", "Patients with recent infection Females", recentHIVInfectionDuringPeriod);
        addGender(dsd, "b", "Patients with recent infection Males", recentHIVInfectionDuringPeriod);
        addIndicator(dsd, "9a", "Pregnant and with recent infection", pregnantAndTestedWithRecentInfection, "");
        addIndicator(dsd, "10a", "PWIDS and with recent infection females", df.getPatientsInAll(PWIDS,recentHIVInfectionDuringPeriod,females), "");
        addIndicator(dsd, "10b", "PWIDS and with recent infection males", df.getPatientsInAll(PWIDS,recentHIVInfectionDuringPeriod,males), "");
        addIndicator(dsd, "11a", "PIPS and with recent infection", df.getPatientsInAll(PIPS,recentHIVInfectionDuringPeriod,females), "");
        addIndicator(dsd, "11b", "PIPS and with recent infection", df.getPatientsInAll(PIPS,recentHIVInfectionDuringPeriod,males), "");
        addGender(dsd, "c", "Patients tested for recency Females", testedForRecencyDuringPeriod);
        addGender(dsd, "d", "Patients tested for recency Males", testedForRecencyDuringPeriod);
        addIndicator(dsd, "12a", "Pregnant and tested for recency", pregnantAndTestedForRecency, "");
        addIndicator(dsd, "13a", "PWIDS and tested for recency females", df.getPatientsInAll(PWIDS,females,testedForRecencyDuringPeriod), "");
        addIndicator(dsd, "13b", "PWIDS and tested for recency males", df.getPatientsInAll(PWIDS,males,testedForRecencyDuringPeriod), "");
        addIndicator(dsd, "14a", "PIPS and tested for recency females", df.getPatientsInAll(PIPS,females,testedForRecencyDuringPeriod), "");
        addIndicator(dsd, "14b", "PIPS and tested for recency males", df.getPatientsInAll(PIPS,males,testedForRecencyDuringPeriod), "");

        return rd;
    }

    public void addGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        if (key == "a" || key == "c") {
            addIndicator(dsd, "1" + key, label, cohortDefinition, "age=between15and19female");
            addIndicator(dsd, "2" + key, label, cohortDefinition, "age=between20and24female");
            addIndicator(dsd, "3" + key, label, cohortDefinition, "age=between25and29female");
            addIndicator(dsd, "4" + key, label, cohortDefinition, "age=between30and34female");
            addIndicator(dsd, "5" + key, label, cohortDefinition, "age=between35and39female");
            addIndicator(dsd, "6" + key, label, cohortDefinition, "age=between40and44female");
            addIndicator(dsd, "7" + key, label, cohortDefinition, "age=between45and49female");
            addIndicator(dsd, "8" + key, label, cohortDefinition, "age=above50female");
        } else if (key == "b" || key == "d") {
            addIndicator(dsd, "1" + key, label, cohortDefinition, "age=between15and19male");
            addIndicator(dsd, "2" + key, label, cohortDefinition, "age=between20and24male");
            addIndicator(dsd, "3" + key, label, cohortDefinition, "age=between25and29male");
            addIndicator(dsd, "4" + key, label, cohortDefinition, "age=between30and34male");
            addIndicator(dsd, "5" + key, label, cohortDefinition, "age=between35and39male");
            addIndicator(dsd, "6" + key, label, cohortDefinition, "age=between40and44male");
            addIndicator(dsd, "7" + key, label, cohortDefinition, "age=between45and49male");
            addIndicator(dsd, "8" + key, label, cohortDefinition, "age=above50male");
        }
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
        return "3.0.3";
    }
}