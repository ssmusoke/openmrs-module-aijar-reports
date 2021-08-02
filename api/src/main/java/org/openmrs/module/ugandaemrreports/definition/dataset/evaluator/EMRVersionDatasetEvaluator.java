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

import liquibase.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.EMRVersionDatasetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.NameOfHealthUnitDatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Handler(supports = EMRVersionDatasetDefinition.class)
public class EMRVersionDatasetEvaluator implements DataSetEvaluator{

	private final Log log = LogFactory.getLog(getClass());
    
    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {

        EMRVersionDatasetDefinition dsd = (EMRVersionDatasetDefinition) dataSetDefinition;

        String ugandaemr_version = Context.getMessageSourceService().getMessage("ugandaemr.build.info");
System.out.println(ugandaemr_version);
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);

                context.addContextValue("version", ugandaemr_version.substring(4,ugandaemr_version.length()));
                DataSetRow row = new DataSetRow();
                row.addColumnValue(new DataSetColumn("version", "version", String.class),ugandaemr_version.substring(4,ugandaemr_version.length()));
                dataSet.addRow(row);

        return dataSet;
    }


}
