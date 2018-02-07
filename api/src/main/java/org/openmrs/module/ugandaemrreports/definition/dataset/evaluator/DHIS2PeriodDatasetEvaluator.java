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
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.ugandaemrreports.common.Enums;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.DHIS2PeriodDatasetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.GlobalPropertyParametersDatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.openmrs.module.ugandaemrreports.reports.Helper.getObsPeriod;

/**
 */
@Handler(supports = DHIS2PeriodDatasetDefinition.class)
public class DHIS2PeriodDatasetEvaluator implements DataSetEvaluator {

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {

        DHIS2PeriodDatasetDefinition dsd = (DHIS2PeriodDatasetDefinition) dataSetDefinition;
        Date period = dsd.getStartDate();
        Enums.Period periodType = dsd.getPeriodType();

        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);

        DataSetRow row = new DataSetRow();
        row.addColumnValue(new DataSetColumn("period", "period", String.class), getObsPeriod(period, periodType));
        dataSet.addRow(row);

        return dataSet;
    }


}
