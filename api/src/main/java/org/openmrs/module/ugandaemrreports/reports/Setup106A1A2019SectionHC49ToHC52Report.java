package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.MedianBaselineCD4DatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.ARTCohortLibrary;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.CommonCohortLibrary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 */
@Component
public class Setup106A1A2019SectionHC49ToHC52Report extends UgandaEMRDataExportManager {
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    private CommonCohortLibrary commonCohortLibrary;

    @Autowired
    private DataFactory df;

    @Autowired
    private HIVMetadata hivMetadata;

    @Override
    public String getExcelDesignUuid() {
        return "d727f692-b133-42e3-aba9-a42671047c99";
    }

    @Override
    public String getUuid() {
        return "ac1b2145-54aa-4ed8-9590-db7c47fe51e4";
    }

    @Override
    public String getName() {
        return "HMIS 106A1A 2019 Section HC49 To HC52";
    }

    @Override
    public String getDescription() {
        return "This is the 2019 version of the HMIS 106A Section 1A Section HC49 To HC52 ";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(new Parameter("startDate", "Start date (Start of quarter)", Date.class));
        l.add(new Parameter("endDate", "End date (End of quarter)", Date.class));
        return l;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        return Arrays.asList(buildReportDesign(reportDefinition));
    }

    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "106A1A2019HC49ToHC52Report.xls");
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        rd.addDataSetDefinition("m",Mapped.mapStraightThrough(getMedianCD4AtARTInitiation()));
        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
        dsd.setParameters(getParameters());
        rd.addDataSetDefinition("x", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension finerAgeDisaggregations = commonDimensionLibrary.getFinerAgeDisaggregations();
        dsd.addDimension("age", Mapped.mapStraightThrough(finerAgeDisaggregations));
        CohortDefinition onTBRxDuringQuarter = hivCohortDefinitionLibrary.getPatientsOnTBRxDuringPeriod();

        CohortDefinition activePatientsInCareDuringPeriod = hivCohortDefinitionLibrary.getActivePatientsWithLostToFollowUpAsByDays("90");

        CohortDefinition inProgramFBIMDuringPeriod = commonCohortLibrary.getPatientsInProgramDuringPeriod(commonDimensionLibrary.getProgramByUuid("de5d54ae-c304-11e8-9ad0-529269fb1459"));
        CohortDefinition inProgramFTDRDuringPeriod = commonCohortLibrary.getPatientsInProgramDuringPeriod(commonDimensionLibrary.getProgramByUuid("de5d5896-c304-11e8-9ad0-529269fb1459"));
        CohortDefinition inProgramFBGDuringPeriod = commonCohortLibrary.getPatientsInProgramDuringPeriod(commonDimensionLibrary.getProgramByUuid("de5d5b34-c304-11e8-9ad0-529269fb1459"));
        CohortDefinition inProgramCDDPDuringPeriod = commonCohortLibrary.getPatientsInProgramDuringPeriod(commonDimensionLibrary.getProgramByUuid("de5d6034-c304-11e8-9ad0-529269fb1459"));
        CohortDefinition inProgramCCLADDuringPeriod = commonCohortLibrary.getPatientsInProgramDuringPeriod(commonDimensionLibrary.getProgramByUuid("de5d5da0-c304-11e8-9ad0-529269fb1459"));

         CohortDefinition patientsSuppressedByEndDate = df.getPatientsWithNumericObsByEndDate(hivMetadata.getViralLoadCopies(),hivMetadata.getARTEncounterPageEncounterType(),RangeComparator.LESS_THAN,1000.0, BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition newlyEnrolledInFBIMDuringPeriod = commonCohortLibrary.newlyEnrolledDuringPeriod(commonDimensionLibrary.getProgramByUuid("de5d54ae-c304-11e8-9ad0-529269fb1459"));
        CohortDefinition newlyEnrolledInFTDRDuringPeriod = commonCohortLibrary.newlyEnrolledDuringPeriod(commonDimensionLibrary.getProgramByUuid("de5d5896-c304-11e8-9ad0-529269fb1459"));
        CohortDefinition newlyEnrolledInFBGDuringPeriod = commonCohortLibrary.newlyEnrolledDuringPeriod(commonDimensionLibrary.getProgramByUuid("de5d5b34-c304-11e8-9ad0-529269fb1459"));
        CohortDefinition newlyEnrolledInCDDPDuringPeriod = commonCohortLibrary.newlyEnrolledDuringPeriod(commonDimensionLibrary.getProgramByUuid("de5d6034-c304-11e8-9ad0-529269fb1459"));
        CohortDefinition newlyEnrolledInCCLADDuringPeriod = commonCohortLibrary.newlyEnrolledDuringPeriod(commonDimensionLibrary.getProgramByUuid("de5d5da0-c304-11e8-9ad0-529269fb1459"));


        addAgeGender(dsd, "49a-", "activeOnART patients on DSD FBIM",df.getPatientsInAll(activePatientsInCareDuringPeriod,inProgramFBIMDuringPeriod));
        addAgeGender(dsd, "49b-", "activeOnART patients on DSD FBG",df.getPatientsInAll(activePatientsInCareDuringPeriod,inProgramFBGDuringPeriod));
        addAgeGender(dsd, "49c-", "activeOnART patients on DSD FTDR",df.getPatientsInAll(activePatientsInCareDuringPeriod,inProgramFTDRDuringPeriod));
        addAgeGender(dsd, "49d-", "activeOnART patients on DSD CDDP",df.getPatientsInAll(activePatientsInCareDuringPeriod,inProgramCDDPDuringPeriod));
        addAgeGender(dsd, "49e-", "activeOnART patients on DSD CCLAD",df.getPatientsInAll(activePatientsInCareDuringPeriod,inProgramCCLADDuringPeriod));

        addAgeGender(dsd, "50a-", "activeOnART achieving viral load patients on DSD FBIM",df.getPatientsInAll(patientsSuppressedByEndDate,inProgramFBIMDuringPeriod));
        addAgeGender(dsd, "50b-", "activeOnART achieving viral load patients on DSD FBG",df.getPatientsInAll(patientsSuppressedByEndDate,inProgramFBGDuringPeriod));
        addAgeGender(dsd, "50c-", "activeOnART achieving viral load patients on DSD FTDR",df.getPatientsInAll(patientsSuppressedByEndDate,inProgramFTDRDuringPeriod));
        addAgeGender(dsd, "50d-", "activeOnART achieving viral load patients on DSD CDDP",df.getPatientsInAll(patientsSuppressedByEndDate,inProgramCDDPDuringPeriod));
        addAgeGender(dsd, "50e-", "activeOnART achieving viral load patients on DSD CCLAD",df.getPatientsInAll(patientsSuppressedByEndDate,inProgramCCLADDuringPeriod));

        addAgeGender(dsd, "51a-", "patients newly enrolled in DSD FBIM",newlyEnrolledInFBIMDuringPeriod);
        addAgeGender(dsd, "51b-", "patients newly enrolled in DSD FBG",newlyEnrolledInFBGDuringPeriod);
        addAgeGender(dsd, "51c-", "patients newly enrolled in DSD FTDR",newlyEnrolledInFTDRDuringPeriod);
        addAgeGender(dsd, "51d-", "patients newly enrolled in DSD CDDP",newlyEnrolledInCDDPDuringPeriod);
        addAgeGender(dsd, "51e-", "patients newly enrolled in DSD CCLAD",newlyEnrolledInCCLADDuringPeriod);

        addAgeGender(dsd, "52a-", "patients receiving their TB drug refills in  DSD FBIM",df.getPatientsInAll(onTBRxDuringQuarter,inProgramFBIMDuringPeriod));
        addAgeGender(dsd, "52b-", "patients receiving their TB drug refills in DSD FBG",df.getPatientsInAll(onTBRxDuringQuarter,inProgramFBGDuringPeriod));
        addAgeGender(dsd, "52c-", "patients receiving their TB drug refills in DSD FTDR",df.getPatientsInAll(onTBRxDuringQuarter,inProgramFTDRDuringPeriod));
        addAgeGender(dsd, "52d-", "patients receiving their TB drug refills in DSD CDDP",df.getPatientsInAll(onTBRxDuringQuarter,inProgramCDDPDuringPeriod));
        addAgeGender(dsd, "52e-", "patients receiving their TB drug refills in DSD CCLAD",df.getPatientsInAll(onTBRxDuringQuarter,inProgramCCLADDuringPeriod));
//

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

    public void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition, String dimensionOptions) {
        CohortIndicator ci = new CohortIndicator();
        ci.addParameter(ReportingConstants.START_DATE_PARAMETER);
        ci.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ci.setType(CohortIndicator.IndicatorType.COUNT);
        ci.setCohortDefinition(Mapped.mapStraightThrough(cohortDefinition));
        dsd.addColumn(key, label, Mapped.mapStraightThrough(ci), dimensionOptions);
    }

    public CohortDefinition addParameters(CohortDefinition cohortDefinition){
        return   df.convert(cohortDefinition, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate"));
    }

    private DataSetDefinition getMedianCD4AtARTInitiation() {
        MedianBaselineCD4DatasetDefinition dsd = new MedianBaselineCD4DatasetDefinition();
       dsd.setParameters(getParameters());
        return dsd;
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }
}
