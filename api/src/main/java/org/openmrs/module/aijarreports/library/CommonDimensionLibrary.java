package org.openmrs.module.aijarreports.library;

import java.util.Date;

import org.openmrs.module.aijarreports.AijarReportUtil;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Common dimensions shared across multiple reports
 */
@Component
public class CommonDimensionLibrary extends BaseDefinitionLibrary<CohortDefinitionDimension> {

    @Autowired
    private CommonCohortDefinitionLibrary cohortDefinitionLibrary;

    @Override
    public Class<? super CohortDefinitionDimension> getDefinitionType() {
        return CohortDefinitionDimension.class;
    }

    @Override
    public String getKeyPrefix() {
        return "aijar.dim.common.";
    }

    /**
     * Gender dimension
     * @return the dimension
     */
    @DocumentedDefinition(value = "gender", name = "Gender")
    public CohortDefinitionDimension genders() {
        CohortDefinitionDimension dimGender = new CohortDefinitionDimension();
        dimGender.setName("Gender");
        dimGender.addCohortDefinition(cohortDefinitionLibrary.males().getName(), Mapped.mapStraightThrough(cohortDefinitionLibrary.males()));
        dimGender.addCohortDefinition(cohortDefinitionLibrary.females().getName(), Mapped.mapStraightThrough(cohortDefinitionLibrary.females()));

        return dimGender;
    }
    /**
     * Dimension of age using the standard age groups
     * @return the dimension
     */
    @DocumentedDefinition(value = "age.106a-1A", name = "Age - 106a 1A")
    public CohortDefinitionDimension get106aAgeGroup() {
        CohortDefinitionDimension dimAges = new CohortDefinitionDimension();
        dimAges.setName("Age - 106a 1A");
        dimAges.setDescription("Age desegregation for HMIS 106A Section 1A which are  - AgesGroup (< 2 years, 2 - 5 years, 5 - 14 years, 15+ years)");
        dimAges.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        dimAges.addCohortDefinition("< 2 years", AijarReportUtil.map(cohortDefinitionLibrary.below2Years(), "effectiveDate=${effectiveDate}"));
        dimAges.addCohortDefinition("2 - 5 years", AijarReportUtil.map(cohortDefinitionLibrary.between2And5Years(), "effectiveDate=${effectiveDate}"));
        dimAges.addCohortDefinition("5 - 14 years", AijarReportUtil.map(cohortDefinitionLibrary.between5And14Years(), "effectiveDate=${effectiveDate}"));
        dimAges.addCohortDefinition("15+ years", AijarReportUtil.map(cohortDefinitionLibrary.above15Years(), "effectiveDate=${effectiveDate}"));

        return dimAges;
    }


}
