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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.NameOfHealthUnitDatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Handler(supports = NameOfHealthUnitDatasetDefinition.class)
public class NameOfHealthUnitDatasetEvaluator implements DataSetEvaluator{
	
	private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private SessionFactory sessionFactory;
    
    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
    
    	NameOfHealthUnitDatasetDefinition dsd = (NameOfHealthUnitDatasetDefinition) dataSetDefinition;
        String name = ObjectUtil.nvl(dsd.getFacilityName(),null);

        StringBuilder sqlQuery = new StringBuilder("select gp.property_value");
        sqlQuery.append(" from global_property gp ");
        sqlQuery.append(" where gp.property = '").append(name).append("'");

        SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sqlQuery.toString());
        List<Object[]> list = query.list();

        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        if(list.size() > 0) {
                context.addContextValue("facilityName", list.get(0));
                DataSetRow row = new DataSetRow();
                row.addColumnValue(new DataSetColumn("facilityName", "facilityName", String.class), list.get(0));
                dataSet.addRow(row);
        }

        return dataSet;
    }


}
