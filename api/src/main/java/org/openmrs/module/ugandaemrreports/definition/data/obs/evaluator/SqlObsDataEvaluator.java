/*
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

package org.openmrs.module.ugandaemrreports.definition.data.obs.evaluator;

import java.util.Map;

import org.openmrs.annotation.Handler;
import org.openmrs.module.ugandaemrreports.definition.data.obs.definition.SqlObsDataDefinition;
import org.openmrs.module.reporting.data.obs.EvaluatedObsData;
import org.openmrs.module.reporting.data.obs.ObsDataUtil;
import org.openmrs.module.reporting.data.obs.definition.ObsDataDefinition;
import org.openmrs.module.reporting.data.obs.evaluator.ObsDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.reporting.query.obs.ObsIdSet;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Expects that the SQL query returns two columns:
 * the first should be an Integer returning the obsId
 * the second should be the data you wish to retrieve for each Obs
 * Expects that you use "obsIds" within your query to limit by the base id set in the evaluator context:
 * eg. "select obs_datetime from obs where obs_id in (:obsIds)"
 */
@Handler(supports = SqlObsDataDefinition.class)
public class SqlObsDataEvaluator implements ObsDataEvaluator {

	@Autowired
	EvaluationService evaluationService;

	@Override
	public EvaluatedObsData evaluate(ObsDataDefinition def, EvaluationContext context) throws EvaluationException {
		SqlObsDataDefinition definition = (SqlObsDataDefinition) def;
		EvaluatedObsData data = new EvaluatedObsData(definition, context);

		ObsIdSet obsIds = new ObsIdSet(ObsDataUtil.getObsIdsForContext(context, false));
		if (obsIds.getSize() == 0) {
			return data;
		}

		SqlQueryBuilder q = new SqlQueryBuilder();
		q.append(definition.getSql());
		for (Parameter p : definition.getParameters()) {
			q.addParameter(p.getName(), context.getParameterValue(p.getName()));
		}
		q.addParameter("obsIds", obsIds);

		Map<Integer, Object> results = evaluationService.evaluateToMap(q, Integer.class, Object.class, context);
		data.setData(results);

		return data;
	}

}
