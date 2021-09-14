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
package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

/**
 */

import org.openmrs.Cohort;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.patient.PatientCalculationService;
import org.openmrs.calculation.result.CalculationResult;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.evaluator.PatientDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.definition.data.definition.CalculationDataDefinition;
import org.openmrs.module.ugandaemrreports.definition.data.definition.ClientCareStatusDataDefinition;
import org.openmrs.module.ugandaemrreports.library.CommonCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.CommonDimensionLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.ARTCohortLibrary;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.CommonCohortLibrary;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;

/**
 * Evaluates a {@link ClientCareStatusDataDefinition} to produce a PatientData
 */
@Handler(supports = ClientCareStatusDataDefinition.class, order = 50)
public class ClientCareStatusDataEvaluator implements PatientDataEvaluator {


    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private EvaluationService evaluationService;

    /**
     * @see PatientDataEvaluator#evaluate(PatientDataDefinition, EvaluationContext)
     */
    public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext context) throws EvaluationException {

        ClientCareStatusDataDefinition def = (ClientCareStatusDataDefinition) definition;
        EvaluatedPatientData c = new EvaluatedPatientData(def, context);

        if ((context.getBaseCohort() != null) && (context.getBaseCohort().isEmpty())) {
            return c;
        }
        Map<Integer, Date> m = new HashMap<Integer, Date>();

        String startDate = DateUtil.formatDate(def.getStartDate(), "yyyy-MM-dd");

        String deadpatientsquery = "select person_id from person where dead=1 and voided=0 and death_date >='" + startDate + "'";

        SqlQueryBuilder q = new SqlQueryBuilder(deadpatientsquery);

        List<Object[]> results = evaluationService.evaluateToList(q, context);

        CohortDefinition transferredOutPatients = hivCohortDefinitionLibrary.getPatientsTransferredOutBetweenStartAndEndDate();
        Cohort transferredOut= Context.getService(CohortDefinitionService.class).evaluate(transferredOutPatients, null);
        CohortDefinition activePatients = hivCohortDefinitionLibrary.getPatientsHavingRegimenDuringPeriod();
        Cohort active= Context.getService(CohortDefinitionService.class).evaluate(activePatients, null);

        List<Person> people = new ArrayList<>();
        List<Integer> deadpatientsIds = new ArrayList<>();
        for (Object[] row : results) {
            Person p = new Person();
            String patientId = String.valueOf(row[0]);
            p.setDead(true);
            c.addData(Integer.parseInt(patientId), p);
            deadpatientsIds.add((Integer) row[0]);
        }
        for(int patient_id:transferredOut.getMemberIds()){
            if(deadpatientsIds.contains(patient_id)){
                continue;
            }
            Person p = new Person();
            p.setDead(false);
            p.setPersonVoided(true); // will use this attribute as a check for transfer_out
            c.addData(patient_id, p);
        }
        for(int patient_id:active.getMemberIds()){
            if(transferredOut.getMemberIds().contains(patient_id)){
                continue;
            }
            if(deadpatientsIds.contains(patient_id)){
                continue;
            }
            Person p = new Person();
            p.setDead(false);
            p.setPersonVoided(false); // will use this attribute as a check for active in care
            c.addData(patient_id, p);
        }


        return c;
    }
}
