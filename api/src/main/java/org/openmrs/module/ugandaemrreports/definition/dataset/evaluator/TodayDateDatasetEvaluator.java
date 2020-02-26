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
package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.TodayDateDatasetDefinition;

import java.util.Date;

@Handler(supports = TodayDateDatasetDefinition.class)
public class TodayDateDatasetEvaluator implements DataSetEvaluator{

	private final Log log = LogFactory.getLog(getClass());
    
    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {

        TodayDateDatasetDefinition dsd = (TodayDateDatasetDefinition) dataSetDefinition;

        Date date = new Date();

        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);

                context.addContextValue("today", date);
                DataSetRow row = new DataSetRow();
                row.addColumnValue(new DataSetColumn("today", "today", String.class),date);
                dataSet.addRow(row);

        return dataSet;
    }


}
