package org.openmrs.module.ugandaemrreports.definition.cohort.definition;

import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.common.Localized;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;

@Caching(strategy= ConfigurationPropertyCachingStrategy.class)
@Localized("reporting.CurrentlyInProgramCohortDefinition")
public class CurrentlyInProgramCohortDefinition extends InProgramCohortDefinition {

}
