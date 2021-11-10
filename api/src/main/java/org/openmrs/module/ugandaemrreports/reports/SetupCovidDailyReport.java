package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.*;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.CovidMetadata;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.metadata.TBMetadata;
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary.getConcept;

/**
 *  Covid Daily Report
 */
@Component
public class SetupCovidDailyReport extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    SetupMERTxNew2019Report setupTxNewReport;

    @Autowired
    TBCohortDefinitionLibrary tbCohortDefinitionLibrary;

    @Autowired
    HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    private SharedDataDefintion sdd;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private CovidMetadata covidMetadata;

    @Autowired
    private CommonCohortDefinitionLibrary cohortDefinitionLibrary;



    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "36dc4990-72e7-4a03-a58a-de09bd50d485";
    }

    @Override
    public String getUuid() {
        return "439027c4-ca99-467b-9a45-e0884081c20d";
    }

    @Override
    public String getName() {
        return "Covid Daily Report";
    }

    @Override
    public String getDescription() {
        return "Covid Daily Report";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "CovidReport.xls");
    }



    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();

        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
        CohortIndicatorDataSetDefinition dsd1 = new CohortIndicatorDataSetDefinition();
        CohortIndicatorDataSetDefinition dsd2 = new CohortIndicatorDataSetDefinition();
        CohortIndicatorDataSetDefinition dsd3 = new CohortIndicatorDataSetDefinition();
        CohortIndicatorDataSetDefinition dsd4 = new CohortIndicatorDataSetDefinition();
        CohortIndicatorDataSetDefinition dsd5 = new CohortIndicatorDataSetDefinition();
        CohortIndicatorDataSetDefinition dsd6 = new CohortIndicatorDataSetDefinition();
        CohortIndicatorDataSetDefinition dsd7 = new CohortIndicatorDataSetDefinition();
        CohortIndicatorDataSetDefinition dsd8 = new CohortIndicatorDataSetDefinition();
        CohortIndicatorDataSetDefinition dsd9 = new CohortIndicatorDataSetDefinition();
        CohortIndicatorDataSetDefinition dsd10 = new CohortIndicatorDataSetDefinition();
        dsd.setParameters(getParameters());
        dsd1.setParameters(getParameters());
        dsd2.setParameters(getParameters());
        dsd3.setParameters(getParameters());
        dsd4.setParameters(getParameters());
        dsd5.setParameters(getParameters());
        dsd6.setParameters(getParameters());
        dsd7.setParameters(getParameters());
        dsd8.setParameters(getParameters());
        dsd9.setParameters(getParameters());
        dsd10.setParameters(getParameters());

        rd.addDataSetDefinition("A1",Mapped.mapStraightThrough(dsd));
        rd.addDataSetDefinition("A2",Mapped.mapStraightThrough(dsd1));
        rd.addDataSetDefinition("B2",Mapped.mapStraightThrough(dsd2));
        rd.addDataSetDefinition("HC", Mapped.mapStraightThrough(sdd.healthFacilityName()));

        CohortDefinitionDimension ageDimension = commonDimensionLibrary.getCovidAgeGenderGroup();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));
        dsd1.addDimension("age", Mapped.mapStraightThrough(ageDimension));
        dsd2.addDimension("age", Mapped.mapStraightThrough(ageDimension));

        CohortDefinition admissions = getAnyEncounterOfTypesBetweenDates(hivMetadata.getCovidInitiationEncounterType());
        CohortDefinition allAdmissionsByToday = df.getAnyEncounterOfTypesByEndOfDate(hivMetadata.getCovidInitiationEncounterType());
        CohortDefinition dischargedByToday = df.getAnyEncounterOfTypesByEndOfDate(hivMetadata.getCovidDischargeEncounterType());
        CohortDefinition confirmedCasesOnAdmissionsByToday = df.getPatientsNotIn(allAdmissionsByToday,dischargedByToday);

        Concept national = getConcept("dc47dd51-c509-44fd-ab2f-a2494f0d4726");
        Concept foreigner = getConcept("d7193894-2922-432a-9175-dde877090594");
        Concept refugee = getConcept("165127AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        CohortDefinition ugandans = df.getClientOfNationality(Arrays.asList(national));
        CohortDefinition foreigners = df.getClientOfNationality(Arrays.asList(foreigner));
        CohortDefinition refugees = df.getClientOfNationality(Arrays.asList(refugee));
        CohortDefinition withPhysicalAddressInUganda = df.getPatientsWithCodedObsByEndDate(getConcept("03b90a31-ed26-4375-bbb7-d26b3afd5afd"),hivMetadata.getCovidInitiationEncounterType(),Arrays.asList(getConcept("922be2a9-4f2b-4f42-804f-037f84576fba")), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition foreignResidents = df.getPatientsInAll(foreigners,withPhysicalAddressInUganda);
        CohortDefinition foreignNonResidents = df.getPatientsNotIn(foreigners,withPhysicalAddressInUganda);

        CohortDefinition HIVcomorbidities = df.getPatientsWithCodedObsDuringPeriod(covidMetadata.getHIVCommobiityQuestion(),hivMetadata.getCovidInitiationEncounterType(), Arrays.asList(covidMetadata.getHIVAnswer()), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition TBcomorbidities = df.getPatientsNotIn(df.getPatientsWithCodedObsDuringPeriod(covidMetadata.getCommobiityQuestion(),hivMetadata.getCovidInitiationEncounterType(), Arrays.asList(covidMetadata.getTBComorbidty()), BaseObsCohortDefinition.TimeModifier.ANY),HIVcomorbidities);
        CohortDefinition Diabetescomorbidities = df.getPatientsNotIn(df.getPatientsWithCodedObsDuringPeriod(covidMetadata.getCommobiityQuestion(),hivMetadata.getCovidInitiationEncounterType(), Arrays.asList(covidMetadata.getDiabetesComorbidty()), BaseObsCohortDefinition.TimeModifier.ANY),TBcomorbidities);
        CohortDefinition HyperTensioncomorbidities = df.getPatientsNotIn(df.getPatientsWithCodedObsDuringPeriod(covidMetadata.getCommobiityQuestion(),hivMetadata.getCovidInitiationEncounterType(), Arrays.asList(covidMetadata.getHyperTensionComorbidty() ), BaseObsCohortDefinition.TimeModifier.ANY),Diabetescomorbidities);

        CohortDefinition allComorbidities = df.getPatientsWithCodedObsDuringPeriod(covidMetadata.getCommobiityQuestion(),hivMetadata.getCovidInitiationEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition otherComorbidities = df.getPatientsNotIn(allComorbidities,df.getPatientsInAny(HIVcomorbidities,TBcomorbidities,Diabetescomorbidities,HyperTensioncomorbidities));

        CohortDefinition HIVRelatedcomorbidities = df.getPatientsWithCodedObsDuringPeriod(covidMetadata.getHIVCommobiityQuestion(),hivMetadata.getCovidInitiationEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition OtherHIVRelatedcomorbidities = df.getPatientsNotIn(HIVRelatedcomorbidities,HIVcomorbidities);

        CohortDefinition noComorbidities = df.getPatientsNotIn(confirmedCasesOnAdmissionsByToday,df.getPatientsInAny(allComorbidities,HIVRelatedcomorbidities));

        addGender(dsd,"1","Ugandans  Admitted today",df.getPatientsInAll(ugandans,admissions));
        addGender(dsd,"2","Refugees  Admitted today",df.getPatientsInAll(refugees,admissions));
        addGender(dsd,"3","Foreign residents  Admitted today",df.getPatientsInAll(foreignResidents,admissions));
        addGender(dsd,"4","non resident foreigners  Admitted today",df.getPatientsInAll(foreignNonResidents,admissions));

        addGender(dsd1,"1","Ugandans  on admission as of today",df.getPatientsInAll(ugandans,confirmedCasesOnAdmissionsByToday));
        addGender(dsd1,"2","Refugees  on admission as of today",df.getPatientsInAll(refugees,confirmedCasesOnAdmissionsByToday));
        addGender(dsd1,"3","Foreign residents  on admission as of today",df.getPatientsInAll(foreignResidents,confirmedCasesOnAdmissionsByToday));
        addGender(dsd1,"4","non resident foreigners  on admission as of today",df.getPatientsInAll(foreignNonResidents,confirmedCasesOnAdmissionsByToday));

        addGender(dsd2,"1","No comorbidities",noComorbidities);
        addGender(dsd2,"2","HIV comorbidities",df.getPatientsInAll(confirmedCasesOnAdmissionsByToday,HIVcomorbidities));
        addGender(dsd2,"3","TB comorbidities",df.getPatientsInAll(confirmedCasesOnAdmissionsByToday,TBcomorbidities));
        addGender(dsd2,"5","Diabetes comorbidities",df.getPatientsInAll(confirmedCasesOnAdmissionsByToday,Diabetescomorbidities));
        addGender(dsd2,"6","Hypertension comorbidities",df.getPatientsInAll(confirmedCasesOnAdmissionsByToday,HyperTensioncomorbidities));
        addGender(dsd2,"9","Other comorbidities",df.getPatientsInAll(confirmedCasesOnAdmissionsByToday,df.getPatientsInAny(otherComorbidities,OtherHIVRelatedcomorbidities)));





        return rd;
    }

    public void addGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {

        Helper.addIndicator(dsd,key+"a", label, cohortDefinition, "age=below5male");
        Helper.addIndicator(dsd,key+"b", label, cohortDefinition, "age=below5female");
        Helper.addIndicator(dsd,key+"c", label, cohortDefinition, "age=btn5and19male");
        Helper.addIndicator(dsd,key+"d", label, cohortDefinition, "age=btn5and19female");
        Helper.addIndicator(dsd,key+"e", label, cohortDefinition, "age=btn20and49male");
        Helper.addIndicator(dsd,key+"f", label, cohortDefinition, "age=btn20and49female");
        Helper.addIndicator(dsd,key+"g", label, cohortDefinition, "age=above50male");
        Helper.addIndicator(dsd,key+"h", label, cohortDefinition, "age=above50female");

    }

    public CohortDefinition getAnyEncounterOfTypesBetweenDates(List<EncounterType> types) {
        EncounterCohortDefinition cd = new EncounterCohortDefinition();
        cd.setEncounterTypeList(types);
        cd.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return df.convert(cd, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=startDate"));
    }


    @Override
    public String getVersion() {
        return "2.0.3";
    }
}
