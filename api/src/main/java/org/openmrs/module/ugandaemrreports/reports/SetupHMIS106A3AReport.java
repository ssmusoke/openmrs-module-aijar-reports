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
import org.openmrs.module.ugandaemrreports.metadata.TBMetadata;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary.getConcept;

/**
 *  TX Current Report
 */
@Component
public class SetupHMIS106A3AReport extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    @Autowired
    private TBCohortDefinitionLibrary tbCohortDefinitionLibrary;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    private TBMetadata tbMetadata;

    @Autowired
    private CommonCohortDefinitionLibrary cohortDefinitionLibrary;



    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "220e7492-e56d-4f99-a424-40eda06668f2";
    }

    @Override
    public String getUuid() {
        return "dc69d1a2-2f2e-4512-896e-44c6bf7d1c42";
    }

    @Override
    public String getName() {
        return "HMIS 1061a 3A Report";
    }

    @Override
    public String getDescription() {
        return "HMIS 1061a 3A Report ";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "106A3_1.xls");
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
        rd.addDataSetDefinition("A1", Mapped.mapStraightThrough(dsd));
        rd.addDataSetDefinition("A2", Mapped.mapStraightThrough(dsd));
        rd.addDataSetDefinition("B", Mapped.mapStraightThrough(dsd));
        rd.addDataSetDefinition("C", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension finerAgeDisaggregations = commonDimensionLibrary.getFinerAgeDisaggregations();
        dsd.addDimension("age", Mapped.mapStraightThrough(finerAgeDisaggregations));

        CohortDefinitionDimension patientTypeDimensions = getPatientTypeDimension();
        dsd.addDimension("type", Mapped.mapStraightThrough(patientTypeDimensions));


        CohortDefinition males = cohortDefinitionLibrary.males();
        CohortDefinition females = cohortDefinitionLibrary.females();


        CohortDefinition above15Years = cohortDefinitionLibrary.above15Years();
        CohortDefinition below15Years = cohortDefinitionLibrary.MoHChildren();

        CohortDefinition registered = tbCohortDefinitionLibrary.getNewPatientsDuringPeriod();
        CohortDefinition startedOnTBTreatmentDuringPeriod = tbCohortDefinitionLibrary.getPatientsStartedOnTreatmentDuringperiod();

        CohortDefinition bacteriologicallyConfirmed = df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getPatientType(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(tbMetadata.getBacteriologicallyConfirmed()), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition clinicallyConfirmed = df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getPatientType(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(tbMetadata.getClinicallyDiagnosed()), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition EPTBConfirmed = df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getPatientType(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(tbMetadata.getEPTB()), BaseObsCohortDefinition.TimeModifier.ANY);

        CohortDefinition bacteriologicallyConfirmedAndRegistered = df.getPatientsInAll(registered,bacteriologicallyConfirmed);
        CohortDefinition bacteriologicallyConfirmedAndStartedOnTratment = df.getPatientsInAll(startedOnTBTreatmentDuringPeriod,bacteriologicallyConfirmed);

        CohortDefinition clinicallyConfirmedAndRegistered = df.getPatientsInAll(registered,clinicallyConfirmed);
        CohortDefinition clinicallyConfirmedAndStartedOnTratment = df.getPatientsInAll(startedOnTBTreatmentDuringPeriod,clinicallyConfirmed);

        CohortDefinition EPTBConfirmedAndRegistered = df.getPatientsInAll(registered,EPTBConfirmed);
        CohortDefinition EPTBConfirmedAndStartedOnTratment = df.getPatientsInAll(startedOnTBTreatmentDuringPeriod,EPTBConfirmed);




        CohortDefinition patientsWhoAreHealthWorkers = df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getRiskGroup(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(getConcept("5619AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition patientsWhoAreTBContacts = df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getRiskGroup(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(getConcept("b5171d08-77bf-40a8-a864-51caa6cd2480")), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition patientsWhoAreRefugees = df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getRiskGroup(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(getConcept("165127AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition patientsWhoArePrisoners = df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getRiskGroup(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(getConcept("162277AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition patientsWhoAreUniformedPeople = df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getRiskGroup(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(getConcept("165125AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition patientsWhoAreFisherMen = df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getRiskGroup(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(getConcept("159674AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition patientsWhoAreDiabetic = df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getRiskGroup(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(getConcept("119481AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition patientsWhoAreMiners = df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getRiskGroup(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(getConcept("952c6973-e163-4c0d-b6c8-a7071bd05e2a")), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition patientsWhoAreSmokers = df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getRiskGroup(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(getConcept("1455AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition patientsWhoAreMentallyIll = df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getRiskGroup(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(getConcept("134337AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.ANY);


//        addAgeGender(dsd,"A","all registered",males);
            add1AIndicators(dsd,"a","bacteria and registered ",bacteriologicallyConfirmedAndRegistered);
            add1AIndicators(dsd,"b","bacteria and on treatment",bacteriologicallyConfirmedAndStartedOnTratment);
            add1AIndicators(dsd,"c","clinicallyConfirmedAndRegistered",clinicallyConfirmedAndRegistered);
            add1AIndicators(dsd,"d","clinicallyConfirmedAndStartedOnTratment",clinicallyConfirmedAndStartedOnTratment);
            add1AIndicators(dsd,"e","EPTBConfirmedAndRegistered",EPTBConfirmedAndRegistered);
            add1AIndicators(dsd,"f","EPTBConfirmedAndStartedOnTratment",EPTBConfirmedAndStartedOnTratment);

//            add1AIndicators(dsd,"7","treatementHistoryUnknown",null);



        return rd;
    }

    private void addAgeGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        addIndicator(dsd, key + "a", label + " (Below 1 Males)", cohortDefinition, "age=below1male");
        addIndicator(dsd, key + "b", label + " (Below 1 Females)", cohortDefinition, "age=below1female");
        addIndicator(dsd, key + "c", label + " (Between 1 and 1 Males)", cohortDefinition, "age=between1and4male");
        addIndicator(dsd, key + "d", label + " (Between 1 and 4 Females)", cohortDefinition, "age=between1and4female");
        addIndicator(dsd, key + "e", label + " (Between 5 and 9 Males)", cohortDefinition, "age=between5and9male");
        addIndicator(dsd, key + "f", label + " (Between 5 and 9 Females)", cohortDefinition, "age=between5and9female");
        addIndicator(dsd, key + "g", label + " (Between 10 and 14 Males)", cohortDefinition, "age=between10and14male");
        addIndicator(dsd, key + "h", label + " (Between 10 and 14 Females)", cohortDefinition, "age=between10and14female");
        addIndicator(dsd, key + "i", label + " (Between 15 and 19 Males)", cohortDefinition, "age=between15and19male");
        addIndicator(dsd, key + "j", label + " (Between 15 and 19 Females)", cohortDefinition, "age=between15and19female");
        addIndicator(dsd, key + "k", label + " (Between 20 and 24 Males)", cohortDefinition, "age=between20and24male");
        addIndicator(dsd, key + "l", label + " (Between 20 and 24 Females)", cohortDefinition, "age=between20and24female");
        addIndicator(dsd, key + "m", label + " (Between 25 and 29 Males)", cohortDefinition, "age=between25and29male");
        addIndicator(dsd, key + "n", label + " (Between 25 and 29 Females)", cohortDefinition, "age=between25and29female");
        addIndicator(dsd, key + "o", label + " (Between 30 and 34 Males)", cohortDefinition, "age=between30and34male");
        addIndicator(dsd, key + "p", label + " (Between 30 and 34 Females)", cohortDefinition, "age=between30and34female");
        addIndicator(dsd, key + "q", label + " (Between 35 and 39 Males)", cohortDefinition, "age=between35and39male");
        addIndicator(dsd, key + "r", label + " (Between 35 and 39 Females)", cohortDefinition, "age=between35and39female");
        addIndicator(dsd, key + "s", label + " (Between 40 and 44 Males)", cohortDefinition, "age=between40and44male");
        addIndicator(dsd, key + "t", label + " (Between 40 and 44 Females)", cohortDefinition, "age=between40and44female");
        addIndicator(dsd, key + "u", label + " (Between 45 and 49 Males)", cohortDefinition, "age=between45and49male");
        addIndicator(dsd, key + "v", label + " (Between 45 and 49 Females)", cohortDefinition, "age=between45and49female");
        addIndicator(dsd, key + "w", label + " (Above 50 Males)", cohortDefinition, "age=above50male");
        addIndicator(dsd, key + "x", label + " (Above 50 Females)", cohortDefinition, "age=above50female");
        addIndicator(dsd, key + "y", label + " Total", cohortDefinition, "");
    }

    public void add1AIndicators(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        addIndicator(dsd, "1"+key , label + " new  ", cohortDefinition, "type=newPatients");
        addIndicator(dsd, "2"+key , label + " relapsed ", cohortDefinition, "type=relapsedPatients");
        addIndicator(dsd, "3"+key , label + " treatedAfterLTFP", cohortDefinition, "type=treatedAfterLTFP");
        addIndicator(dsd, "4"+key , label + " treatedAfterFailure ", cohortDefinition, "type=treatedAfterFailure");
        addIndicator(dsd, "5"+key , label + " treatementHistoryUnknown", cohortDefinition, "type=treatementHistoryUnknown");
    }

    public void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition, String dimensionOptions) {
        CohortIndicator ci = new CohortIndicator();
        ci.addParameter(ReportingConstants.START_DATE_PARAMETER);
        ci.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ci.setType(CohortIndicator.IndicatorType.COUNT);
        ci.setCohortDefinition(Mapped.mapStraightThrough(cohortDefinition));
        dsd.addColumn(key, label, Mapped.mapStraightThrough(ci), dimensionOptions);
    }

    public CohortDefinitionDimension getPatientTypeDimension(){
        CohortDefinition newPatients = tbCohortDefinitionLibrary.getNewPatientsDuringPeriod();
        CohortDefinition relapsedPatients = tbCohortDefinitionLibrary.getRelapsedPatientsDuringPeriod();
        CohortDefinition treatedAfterLTFP = tbCohortDefinitionLibrary.getTreatedAfterLTFPPatientsDuringPeriod();
        CohortDefinition treatedAfterFailure = tbCohortDefinitionLibrary.getTreatedAfterFailurePatientsDuringPeriod();
        CohortDefinition treatementHistoryUnknown = tbCohortDefinitionLibrary.getTreatmentHistoryUnknownPatientsDuringPeriod();

        CohortDefinitionDimension patientTypeDimension= new CohortDefinitionDimension();
        patientTypeDimension.addParameter(ReportingConstants.START_DATE_PARAMETER);
        patientTypeDimension.addParameter(ReportingConstants.END_DATE_PARAMETER);

        patientTypeDimension.addCohortDefinition("newPatients", Mapped.mapStraightThrough(newPatients));
        patientTypeDimension.addCohortDefinition("relapsedPatients", Mapped.mapStraightThrough(relapsedPatients));
        patientTypeDimension.addCohortDefinition("treatedAfterLTFP", Mapped.mapStraightThrough(treatedAfterLTFP));
        patientTypeDimension.addCohortDefinition("treatedAfterFailure", Mapped.mapStraightThrough(treatedAfterFailure));
        patientTypeDimension.addCohortDefinition("treatementHistoryUnknown", Mapped.mapStraightThrough(treatementHistoryUnknown));
        patientTypeDimension.addCohortDefinition("treatementHistoryUnknown", Mapped.mapStraightThrough(treatementHistoryUnknown));

        return patientTypeDimension;
    }




    @Override
    public String getVersion() {
        return "1.0.0";
    }
}
