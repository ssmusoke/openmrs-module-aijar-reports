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
import org.openmrs.module.ugandaemrreports.metadata.SMCMetadata;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.openmrs.module.ugandaemrreports.library.CommonDatasetLibrary.getUgandaEMRVersion;
import static org.openmrs.module.ugandaemrreports.library.CommonDatasetLibrary.settings;

/**
 *  TX Current Report
 */
@Component
public class SetupWeeklySurgeReport extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    private SMCMetadata smcMetadata;

    @Autowired
    private HIVMetadata hivMetadata;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "8347cd85-6c14-4509-9e78-77d2f0cc664a";
    }

    public String getJSONDesignUuid() {
        return "98b4d8d6-17da-45f2-a825-87a8f6522e13";
    }

    @Override
    public String getUuid() {
        return "e7102e5c-b90d-4a4a-b763-20518eadbae5";
    }

    @Override
    public String getName() {
        return "SURGE REPORT";
    }

    @Override
    public String getDescription() {
        return "SURGE REPORT";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "SURGE.xls");
    }

    public ReportDesign buildJSONReportDesign(ReportDefinition reportDefinition) {
        return createJSONTemplateDesign(getJSONDesignUuid(), reportDefinition, "SURGE.json");
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
        rd.addDataSetDefinition("TX", Mapped.mapStraightThrough(dsd));
        rd.addDataSetDefinition("S", Mapped.mapStraightThrough(settings()));
        rd.addDataSetDefinition("aijar", Mapped.mapStraightThrough(getUgandaEMRVersion()));

        CohortDefinitionDimension ageDimension = commonDimensionLibrary.getSurgeWeeklyAgeDimension();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));


        CohortDefinition TX_New = df.getPatientsNotIn(hivCohortDefinitionLibrary.getArtStartDateBetweenPeriod(),hivCohortDefinitionLibrary.getTransferredInToCareDuringPeriod());
        CohortDefinition SMC =df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(smcMetadata.getCircumcisionDate(), null, BaseObsCohortDefinition.TimeModifier.ANY);

        CohortDefinition initiatedOnIPT =hivCohortDefinitionLibrary.getTPTStartDateBetweenPeriod();
        CohortDefinition patientsStartedOnTLDDuringPeriod = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getArtStartRegimen(),hivMetadata.getARTSummaryPageEncounterType(),Arrays.asList(Dictionary.getConcept("a58d12c5-abc2-4575-8fdb-f30960f348fc")), BaseObsCohortDefinition.TimeModifier.ANY);

        CohortDefinition emtctCareEntries = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCareEntryPoint(),hivMetadata.getARTSummaryPageEncounterType(),Arrays.asList(hivMetadata.getEMTCTCareEntryPoint()), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition TLDThroughEMTCT = df.getPatientsInAll(emtctCareEntries,patientsStartedOnTLDDuringPeriod);

        CohortDefinition TLDThroughART = df.getPatientsNotIn(patientsStartedOnTLDDuringPeriod,TLDThroughEMTCT);

        CohortDefinition dueForSecondVisitInReportingPeriod = df.getCohortDefinitionBySql("SELECT obs.person_id FROM obs INNER JOIN (SELECT person_id,value_datetime AS artStartDate FROM obs WHERE concept_id=99165 AND  voided=0)A\n" +
                "    ON A.person_id=obs.person_id WHERE concept_id=5096 AND voided=0 AND value_datetime >=:startDate AND value_datetime<=:endDate AND DATE(artStartDate)=DATE(obs.obs_datetime)");

        CohortDefinition onTLDBeforePeriod = df.getPatientsWithCodedObsByEndOfPreviousDate(hivMetadata.getCurrentRegimen(), null, Arrays.asList(Dictionary.getConcept("a58d12c5-abc2-4575-8fdb-f30960f348fc")),"1d", BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition onTLDDuringPeriod = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), null, Arrays.asList(Dictionary.getConcept("a58d12c5-abc2-4575-8fdb-f30960f348fc")), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition transitionToTLD = df.getPatientsNotIn(onTLDDuringPeriod,onTLDBeforePeriod);
        CohortDefinition pregnantOrBFeeding = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getPregnant(), hivMetadata.getARTEncounterPageEncounterType(), Arrays.asList(hivMetadata.getYesPregnant(),hivMetadata. getLactatingAtEnrollment()), BaseObsCohortDefinition.TimeModifier.ANY);

        CohortDefinition transitionedToTLDThroughEMTCT = df.getPatientsInAll(transitionToTLD,pregnantOrBFeeding);
        CohortDefinition transitionedToTLDThroughARTClinic = df.getPatientsNotIn(transitionToTLD,transitionedToTLDThroughEMTCT);

        CohortDefinition recentHIVInfectionDuringPeriod = hivCohortDefinitionLibrary.getPatientWithRecentHIVInfectionDuringPeriod();

        CohortDefinition onABC3TCDTGBeforePeriod = df.getPatientsWithCodedObsByEndOfPreviousDate(hivMetadata.getCurrentRegimen(), null, Arrays.asList(Dictionary.getConcept("6cc36637-596a-4426-92cf-170f76ea437d")),"1d", BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition onABC3TCDTGDuringPeriod = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), null, Arrays.asList(Dictionary.getConcept("6cc36637-596a-4426-92cf-170f76ea437d")), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition transitionToABC3TCDTG = df.getPatientsNotIn(onABC3TCDTGDuringPeriod,onABC3TCDTGBeforePeriod);

        CohortDefinition onABC3TCLPVRBeforePeriod = df.getPatientsWithCodedObsByEndOfPreviousDate(hivMetadata.getCurrentRegimen(), null, Arrays.asList(Dictionary.getConcept("14c56659-3d4e-4b88-b3ff-e2d43dbfb865")),"1d", BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition onABC3TCLPVRDuringPeriod = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), null, Arrays.asList(Dictionary.getConcept("14c56659-3d4e-4b88-b3ff-e2d43dbfb865")), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition transitionToABC3TCLPVR = df.getPatientsNotIn(onABC3TCLPVRDuringPeriod,onABC3TCLPVRBeforePeriod);


        addIndicator(dsd,"BABIES","TX_NEW 0-11 m", TX_New,"age=below1Year");
        addIndicator(dsd,"1a","TX_NEW 1-14 between1And14female", TX_New,"age=between1And14female");
        addIndicator(dsd,"1b","TX_NEW 1-14 between1And14male", TX_New,"age=between1And14male");
        addIndicator(dsd,"2a","TX_NEW 15+ above15female", TX_New,"age=above15female");
        addIndicator(dsd,"2b","TX_NEW 15+ above15male", TX_New,"age=above15male");

        addIndicator(dsd,"3b","VMMC_CIRC <15", SMC,"age=less15male");
        addIndicator(dsd,"4b","VMMC_CIRC 15-29", SMC,"age=between15And29male");
        addIndicator(dsd,"5b","VMMC_CIRC 30", SMC,"age=above30male");

        addIndicator(dsd,"6a","TB_IPT <5", initiatedOnIPT,"age=less5Years");
        addIndicator(dsd,"7a","TB_IPT 5-14", initiatedOnIPT,"age=between5And14Years");
        addIndicator(dsd,"8a","TB_IPT 15+ females", initiatedOnIPT,"age=above15female");
        addIndicator(dsd,"8b","TB_IPT 15+ males", initiatedOnIPT,"age=above15male");

        addIndicator(dsd,"9a","TLD <15", patientsStartedOnTLDDuringPeriod,"age=less15Years");
        addIndicator(dsd,"10a","TLD 15+ females", patientsStartedOnTLDDuringPeriod,"age=above15female");
        addIndicator(dsd,"10b","TLD 15+ males", patientsStartedOnTLDDuringPeriod,"age=above15male");

        addIndicator(dsd,"11a","TLD ART <15", TLDThroughART,"age=less15Years");
        addIndicator(dsd,"12a","TLD ART 15+ females", TLDThroughART,"age=above15female");
        addIndicator(dsd,"12b","TLD ART 15+ males", TLDThroughART,"age=above15male");

        addIndicator(dsd,"13a","TLD MBCP <15", TLDThroughEMTCT,"age=less15Years");
        addIndicator(dsd,"14a","TLD MBCP 15+ females", TLDThroughEMTCT,"age=above15female");
        addIndicator(dsd,"14b","TLD MBCP 15+ males", TLDThroughEMTCT,"age=above15male");

        addIndicator(dsd,"13a","TLD MBCP <15", TLDThroughEMTCT,"age=less15Years");
        addIndicator(dsd,"14a","TLD MBCP 15+ females", TLDThroughEMTCT,"age=above15female");
        addIndicator(dsd,"14b","TLD MBCP 15+ males", TLDThroughEMTCT,"age=above15male");

        addIndicator(dsd,"15a","TLD Transitioned <15", transitionToTLD,"age=less15Years");
        addIndicator(dsd,"16a","TLD Transitioned 15+ females", transitionToTLD,"age=above15female");
        addIndicator(dsd,"16b","TLD Transitioned 15+ males", transitionToTLD,"age=above15male");

        addIndicator(dsd,"17a","TLD Transitioned ART <15", transitionedToTLDThroughARTClinic,"age=less15Years");
        addIndicator(dsd,"18a","TLD Transitioned ART 15+ females", transitionedToTLDThroughARTClinic,"age=above15female");
        addIndicator(dsd,"18b","TLD Transitioned ART 15+ males", transitionedToTLDThroughARTClinic,"age=above15male");

        addIndicator(dsd,"19a","TLD Transitioned MBCP <15", transitionedToTLDThroughEMTCT,"age=less15Years");
        addIndicator(dsd,"20a","TLD Transitioned MBCP 15+ females", transitionedToTLDThroughEMTCT,"age=above15female");
        addIndicator(dsd,"20b","TLD Transitioned MBCP 15+ males", transitionedToTLDThroughEMTCT,"age=above15male");

        addIndicator(dsd,"21a","TX_SV(D) <15", dueForSecondVisitInReportingPeriod,"age=less15female");
        addIndicator(dsd,"21b","TX_SV(D) <15", dueForSecondVisitInReportingPeriod,"age=less15male");
        addIndicator(dsd,"22a","TX_SV(D) 15+ females", dueForSecondVisitInReportingPeriod,"age=above15female");
        addIndicator(dsd,"22b","TX_SV(D) 15+ males", dueForSecondVisitInReportingPeriod,"age=above15male");

        addIndicator(dsd,"21c","TX_SV(N) <15", dueForSecondVisitInReportingPeriod,"age=less15female");
        addIndicator(dsd,"21d","TX_SV(N) <15", dueForSecondVisitInReportingPeriod,"age=less15male");
        addIndicator(dsd,"22c","TX_SV(N) 15+ females", dueForSecondVisitInReportingPeriod,"age=above15female");
        addIndicator(dsd,"22d","TX_SV(N) 15+ males", dueForSecondVisitInReportingPeriod,"age=above15male");

        addIndicator(dsd,"23a","TX_ PRO ABC/3TC/DTG <3", transitionToABC3TCDTG,"age=less3Years");
        addIndicator(dsd,"23b","TX_ PRO ABC/3TC/DTG 3-9", transitionToABC3TCDTG,"age=between3And9Years");
        addIndicator(dsd,"23c","TX_ PRO ABC/3TC/DTG 10-14", transitionToABC3TCDTG,"age=between10And14years");
        addIndicator(dsd,"23d","TX_ PRO ABC/3TC/DTG 15-19", transitionToABC3TCDTG,"age=between15And19Years");

        addIndicator(dsd,"24a","TX_ PRO ABC/3TC/LPV/r <3", transitionToABC3TCLPVR,"age=less3Years");
        addIndicator(dsd,"24b","TX_ PRO ABC/3TC/LPV/r 3-9", transitionToABC3TCLPVR,"age=between3And9Years");
        addIndicator(dsd,"24c","TX_ PRO ABC/3TC/LPV/r 10-14", transitionToABC3TCLPVR,"age=between10And14years");
        addIndicator(dsd,"24d","TX_ PRO ABC/3TC/LPV/r 15-19", transitionToABC3TCLPVR,"age=between15And19Years");

        addIndicator(dsd,"25a","TX_ PRO TLD <3", transitionToTLD,"age=less3Years");
        addIndicator(dsd,"25b","TX_ PRO TLD 3-9", transitionToTLD,"age=between3And9Years");
        addIndicator(dsd,"25c","TX_ PRO TLD 10-14", transitionToTLD,"age=between10And14years");
        addIndicator(dsd,"25d","TX_ PRO TLD 15-19", transitionToTLD,"age=between15And19Years");

        addIndicator(dsd,"26a","HTS RECENT <15 FEMALE", recentHIVInfectionDuringPeriod,"age=less15female");
        addIndicator(dsd,"26b","HTS RECENT <15 MALE", recentHIVInfectionDuringPeriod,"age=less15male");
        addIndicator(dsd,"27a","HTS RECENT 15+ females", recentHIVInfectionDuringPeriod,"age=above15female");
        addIndicator(dsd,"27b","HTS RECENT 15+ males", recentHIVInfectionDuringPeriod,"age=above15male");



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
        return "0.0.7";
    }
}