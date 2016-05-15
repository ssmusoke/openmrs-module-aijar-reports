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
package org.openmrs.module.aijarreports.definition.data.obs.evaluator;

import org.openmrs.annotation.Handler;
import org.openmrs.module.aijarreports.definition.data.obs.definition.ObsDatetimeDataDefinition;
import org.openmrs.module.reporting.data.obs.evaluator.ObsPropertyDataEvaluator;

/**
 * Evaluates a ObsDatetimeDataDefinition to produce a ObsData
 */
@Handler(supports = ObsDatetimeDataDefinition.class, order = 50)
public class ObsDatetimeDataEvaluator extends ObsPropertyDataEvaluator {

	@Override
	public String getPropertyName() {
		return "obsDatetime";
	}
}
