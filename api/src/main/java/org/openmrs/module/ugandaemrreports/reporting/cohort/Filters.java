/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.ugandaemrreports.reporting.cohort;

import org.openmrs.Program;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.module.ugandaemrreports.reporting.calculation.Calculations;
import org.openmrs.module.ugandaemrreports.reporting.utils.CalculationUtils;

import java.util.Collection;
import java.util.Set;

/**
 * Utility class of filters which take a cohort and return another cohort
 */
public class Filters {

	/**
	 * Patients who are alive
	 * @param cohort the patient ids
	 * @param context the calculation context
	 * @return the filtered cohort
	 */
	public static Set<Integer> alive(Collection<Integer> cohort, PatientCalculationContext context) {
		return CalculationUtils.patientsThatPass(Calculations.alive(cohort, context));
	}

	/**
	 * Patients who are female
	 * @param cohort the patient ids
	 * @param context the calculation context
	 * @return the filtered cohort
	 */
	public static Set<Integer> female(Collection<Integer> cohort, PatientCalculationContext context) {
		return CalculationUtils.patientsThatPass(Calculations.genders(cohort, context), "F");
	}

	/**
	 * Patients who are in the specified program
	 * @param program the program
	 * @param cohort the patient ids
	 * @param context the calculation context
	 * @return the filtered cohort
	 */
	public static Set<Integer> inProgram(Program program, Collection<Integer> cohort, PatientCalculationContext context) {
		return CalculationUtils.patientsThatPass(Calculations.activeEnrollment(program, cohort, context));
	}
	
	/**
	 * Patients who are male
	 * @param cohort the patient ids
	 * @param context the calculation context
	 * @return the filtered cohort
	 */
	public static Set<Integer> male(Collection<Integer> cohort, PatientCalculationContext context) {
		return CalculationUtils.patientsThatPass(Calculations.genders(cohort, context), "M");
	}
}