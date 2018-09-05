package org.openmrs.module.ugandaemrreports.library;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.ugandaemrreports.UgandaEMRReportUtil;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

/**
 * Common dimensions shared across multiple reports
 */
@Component
public class CommonDimensionLibrary extends BaseDefinitionLibrary<CohortDefinitionDimension> {

    @Autowired
    private CommonCohortDefinitionLibrary cohortDefinitionLibrary;
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private DataFactory df;
    @Autowired
    private HIVMetadata hivMetadata;


    @Override
    public Class<? super CohortDefinitionDimension> getDefinitionType() {
        return CohortDefinitionDimension.class;
    }

    @Override
    public String getKeyPrefix() {
        return "ugemr.dim.common.";
    }

    /**
     * Gender dimension
     *
     * @return the dimension
     */
    public CohortDefinitionDimension genders() {
        CohortDefinitionDimension dimGender = new CohortDefinitionDimension();
        dimGender.setName("Gender");
        dimGender.addCohortDefinition(cohortDefinitionLibrary.males().getName(), Mapped.mapStraightThrough(cohortDefinitionLibrary.males()));
        dimGender.addCohortDefinition(cohortDefinitionLibrary.females().getName(), Mapped.mapStraightThrough(cohortDefinitionLibrary.females()));

        return dimGender;
    }

    /**
     * Dimension of age using the standard age groups
     *
     * @return the dimension
     */
    public CohortDefinitionDimension get106aAgeGroup() {
        CohortDefinitionDimension dimAges = new CohortDefinitionDimension();
        dimAges.setName("Age - 106a 1A");
        dimAges.setDescription("Age desegregation for HMIS 106A Section 1A which are  - AgesGroup (< 2 years, 2 - 5 years, 5 - 14 years, 15+ years)");
        dimAges.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        dimAges.addCohortDefinition("< 2 years", UgandaEMRReportUtil
                .map(cohortDefinitionLibrary.below2Years(), "effectiveDate=${effectiveDate}"));
        dimAges.addCohortDefinition("2 - 5 years", UgandaEMRReportUtil
                .map(cohortDefinitionLibrary.between2And5Years(), "effectiveDate=${effectiveDate}"));
        dimAges.addCohortDefinition("5 - 14 years", UgandaEMRReportUtil
                .map(cohortDefinitionLibrary.between5And14Years(), "effectiveDate=${effectiveDate}"));
        dimAges.addCohortDefinition("15+ years", UgandaEMRReportUtil
                .map(cohortDefinitionLibrary.above15Years(), "effectiveDate=${effectiveDate}"));

        return dimAges;
    }


    /**
     * Dimension of age using the standard age and gender groups
     *
     * @return the dimension
     */
    public CohortDefinitionDimension get106aAgeGenderGroup() {
        CohortDefinitionDimension ageGenderDimension = new CohortDefinitionDimension();

        CohortDefinition below2Years = cohortDefinitionLibrary.below2Years();
        CohortDefinition between2And4Years = cohortDefinitionLibrary.between2And5Years();
        CohortDefinition between5And14Years = cohortDefinitionLibrary.between5And14Years();
        CohortDefinition above15Years = cohortDefinitionLibrary.above15Years();

        CohortDefinition males = cohortDefinitionLibrary.males();
        CohortDefinition females = cohortDefinitionLibrary.females();


        CohortDefinition a = df.getPatientsInAll(below2Years, males);
        CohortDefinition b = df.getPatientsInAll(below2Years, females);
        CohortDefinition c = df.getPatientsInAll(between2And4Years, males);
        CohortDefinition d = df.getPatientsInAll(between2And4Years, females);

        CohortDefinition e = df.getPatientsInAll(between5And14Years, males);
        CohortDefinition f = df.getPatientsInAll(between5And14Years, females);
        CohortDefinition g = df.getPatientsInAll(above15Years, males);
        CohortDefinition h = df.getPatientsInAll(above15Years, females);

        ageGenderDimension.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ageGenderDimension.addCohortDefinition("below2male", Mapped.mapStraightThrough(a));
        ageGenderDimension.addCohortDefinition("below2female", Mapped.mapStraightThrough(b));
        ageGenderDimension.addCohortDefinition("between2and5male", Mapped.mapStraightThrough(c));
        ageGenderDimension.addCohortDefinition("between2and5female", Mapped.mapStraightThrough(d));
        ageGenderDimension.addCohortDefinition("between5and14male", Mapped.mapStraightThrough(e));
        ageGenderDimension.addCohortDefinition("between5and14female", Mapped.mapStraightThrough(f));
        ageGenderDimension.addCohortDefinition("above15male", Mapped.mapStraightThrough(g));
        ageGenderDimension.addCohortDefinition("above15female", Mapped.mapStraightThrough(h));
        ageGenderDimension.addCohortDefinition("child", Mapped.mapStraightThrough(cohortDefinitionLibrary.agedBetween(0, 14)));
        ageGenderDimension.addCohortDefinition("adult", Mapped.mapStraightThrough(cohortDefinitionLibrary.agedAtLeast(15)));
        return ageGenderDimension;
    }

    public CohortDefinitionDimension getCBSAdultReportAgeGenderGroup() {
        CohortDefinitionDimension ageGenderDimension = new CohortDefinitionDimension();

        CohortDefinition below5Years = cohortDefinitionLibrary.below5Years();
        CohortDefinition between5And14Years = cohortDefinitionLibrary.between5And14Years();
        CohortDefinition above15Years = cohortDefinitionLibrary.above15Years();

        CohortDefinition males = cohortDefinitionLibrary.males();
        CohortDefinition females = cohortDefinitionLibrary.females();

        CohortDefinition a = df.getPatientsInAll(below5Years, males);
        CohortDefinition b = df.getPatientsInAll(below5Years, females);

        CohortDefinition e = df.getPatientsInAll(between5And14Years, males);
        CohortDefinition f = df.getPatientsInAll(between5And14Years, females);
        CohortDefinition g = df.getPatientsInAll(above15Years, males);
        CohortDefinition h = df.getPatientsInAll(above15Years, females);

        ageGenderDimension.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ageGenderDimension.addCohortDefinition("below5male", Mapped.mapStraightThrough(a));
        ageGenderDimension.addCohortDefinition("below5female", Mapped.mapStraightThrough(b));
        ageGenderDimension.addCohortDefinition("between5and14male", Mapped.mapStraightThrough(e));
        ageGenderDimension.addCohortDefinition("between5and14female", Mapped.mapStraightThrough(f));
        ageGenderDimension.addCohortDefinition("above15male", Mapped.mapStraightThrough(g));
        ageGenderDimension.addCohortDefinition("above15female", Mapped.mapStraightThrough(h));
        return ageGenderDimension;
    }


    public CohortDefinitionDimension getAdherenceGroup() {
        CohortDefinitionDimension adherenceDimension = new CohortDefinitionDimension();

        CohortDefinition good = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getAdherence(), Arrays.asList(hivMetadata.getARTEncounterEncounterType()), Arrays.asList(hivMetadata.getGoodAdherence()), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition fair = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getAdherence(), Arrays.asList(hivMetadata.getARTEncounterEncounterType()), Arrays.asList(hivMetadata.getFairAdherence()), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition poor = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getAdherence(), Arrays.asList(hivMetadata.getARTEncounterEncounterType()), Arrays.asList(hivMetadata.getPoorAdherence()), BaseObsCohortDefinition.TimeModifier.ANY);

        adherenceDimension.addParameter(ReportingConstants.END_DATE_PARAMETER);
        adherenceDimension.addCohortDefinition("good", Mapped.mapStraightThrough(good));
        adherenceDimension.addCohortDefinition("fair", Mapped.mapStraightThrough(fair));
        adherenceDimension.addCohortDefinition("poor", Mapped.mapStraightThrough(poor));
        return adherenceDimension;
    }


    public CohortDefinitionDimension get106bEMTCTGroup() {
        CohortDefinitionDimension eMTCTDimension = new CohortDefinitionDimension();

        CohortDefinition pregnant = hivCohortDefinitionLibrary.getPregnantPatientsAtArtStart();
        CohortDefinition lactating = hivCohortDefinitionLibrary.getLactatingPatientsAtArtStart();
        CohortDefinition pregnantOrLactating = df.getPatientsInAny(pregnant, lactating);

        eMTCTDimension.addCohortDefinition("pregnant", Mapped.mapStraightThrough(pregnantOrLactating));

        return eMTCTDimension;
    }


}
