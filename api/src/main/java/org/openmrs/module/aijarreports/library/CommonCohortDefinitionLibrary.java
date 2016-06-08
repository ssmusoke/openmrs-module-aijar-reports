package org.openmrs.module.aijarreports.library;

import java.util.Date;

import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.stereotype.Component;

/**
 * Library of common Cohort definitions
 */
@Component
public class CommonCohortDefinitionLibrary extends BaseDefinitionLibrary<CohortDefinition> {

	@Override
	public Class<? super CohortDefinition> getDefinitionType() {
		return CohortDefinition.class;
	}

	@Override
	public String getKeyPrefix() {
		return "aijar.cohort.common.";
	}

	/**
	 * Patients who are female
	 *
	 * @return the cohort definition
	 */
	@DocumentedDefinition(value = "gender.females", name = "Females")
	public CohortDefinition females() {
		GenderCohortDefinition cd = new GenderCohortDefinition();
		cd.setName("Females");
		cd.setFemaleIncluded(true);
		return cd;
	}

	/**
	 * Patients who are male
	 *
	 * @return the cohort definition
	 */
	@DocumentedDefinition(value = "gender.males", name = "Males")
	public CohortDefinition males() {
		GenderCohortDefinition cd = new GenderCohortDefinition();
		cd.setName("Males");
		cd.setMaleIncluded(true);
		return cd;
	}

	/**
	 * Patients who at most maxAge years old on ${effectiveDate}
	 *
	 * @return the cohort definition
	 */
	public CohortDefinition agedAtMost(int maxAge) {
		AgeCohortDefinition cd = new AgeCohortDefinition();
		cd.setName("aged at most " + maxAge);
		cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		cd.setMaxAge(maxAge);
		return cd;
	}

	/**
	 * Patients who are at least minAge years old on ${effectiveDate}
	 *
	 * @return the cohort definition
	 */
	public CohortDefinition agedAtLeast(int minAge) {
		AgeCohortDefinition cd = new AgeCohortDefinition();
		cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		cd.setMinAge(minAge);
		return cd;
	}

	/**
	 * Patients who are at least minAge years old and are not more than maxAge on ${effectiveDate}
	 *
	 * @return the cohort definition
	 */
	public CohortDefinition agedBetween(int minAge, int maxAge) {
		AgeCohortDefinition cd = new AgeCohortDefinition();
		cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		cd.setMinAge(minAge);
		cd.setMaxAge(maxAge);
		return cd;
	}

	@DocumentedDefinition(value = "age.below6months", name = "< 6 months")
	public CohortDefinition below6months() {
		AgeCohortDefinition cd = (AgeCohortDefinition) agedAtMost(6);
		cd.setMinAgeUnit(DurationUnit.MONTHS);
		return cd;
	}

	@DocumentedDefinition(value = "age.below2yrs", name = "< 2 years")
	public CohortDefinition below2Years() {
		return agedAtMost(1);
	}

	@DocumentedDefinition(value = "age.6to59mths", name = "6 - 59 months")
	public CohortDefinition between6And59months() {
		AgeCohortDefinition cd = (AgeCohortDefinition) agedBetween(6, 59);
		cd.setMinAgeUnit(DurationUnit.MONTHS);
		cd.setMaxAgeUnit(DurationUnit.MONTHS);
		return cd;
	}

	@DocumentedDefinition(value = "age.0to4yrs", name = "0 - 4 years")
	public CohortDefinition between0And4years() {
		return agedAtMost(4);
	}

	@DocumentedDefinition(value = "age.btn2and5yrs", name = "2 - <5 years")
	public CohortDefinition between2And5Years() {
		return agedBetween(2, 4);
	}

	@DocumentedDefinition(value = "age.btn5and14yrs", name = "5 - 14 years")
	public CohortDefinition between5And14Years() {
		return agedBetween(5, 14);
	}

	@DocumentedDefinition(value = "age.btn15and49yrs", name = "15 - 49 years")
	public CohortDefinition between15And49Years() {
		return agedBetween(14, 49);
	}

	@DocumentedDefinition(value = "age.over15yrs", name = "Over 15 years")
	public CohortDefinition above15Years() {
		return agedAtLeast(15);
	}

	@DocumentedDefinition(value = "age.over50yrs", name = "Over 50 years")
	public CohortDefinition above50Years() {
		return agedAtLeast(50);
	}
	
}
