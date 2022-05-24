package org.openmrs.module.ugandaemrreports.dashboard.evaluator;

import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.dashboard.definition.HivenrollmentDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

//The evaluator needs a Handler definition to map to
@Handler(supports = {HivenrollmentDefinition.class})
public class HIVENROLLMENTEvaluator implements DataSetEvaluator {

    @Autowired
    EvaluationService evaluationService;

    private DataSet getEnrollmentsInPeriod(DataSetDefinition dataSetDefinition, EvaluationContext context) {

        //Define the Definition object to access the parameters/methods
        HivenrollmentDefinition Hdefinition = (HivenrollmentDefinition) dataSetDefinition;

        //Query returning the number of client whose enrollment date fall within the same month => Month:Number Enrolled
        HqlQueryBuilder q = new HqlQueryBuilder();
        q.select("month(value_datetime)", "count(month(value_datetime))");
        q.from(Obs.class);
        q.where(String.format("concept_id=%s and value_datetime between %s and %s", "165312", Hdefinition.getStartDate(), Hdefinition.getEndDate()));
        q.groupBy("month(value_datetime)");

        //The evaluationService already contain definition and a connection to the data model including the database
        List<Object[]> results = evaluationService.evaluateToList(q, context);

        //No need to procede if results count is 0.
        if (results.size() == 0)
            return null;

        System.out.println(results.size() + " En Record count");

        //Constructing a dataset to return data to UI for further processing.
        SimpleDataSet dataSet = new SimpleDataSet(Hdefinition, context);

        //Defining columns to be returned in the query q above.
        DataSetColumn monthColumn = new DataSetColumn("Month", "Month", String.class);
        DataSetColumn enrollmentsColumn = new DataSetColumn("Enrollments", "Enrollments", Integer.class);

        DataSetRow r;

        //loop to convert  elements and add them to a dataset which later will be returned
        for (Object[] data : results) {
            r = new DataSetRow();
            r.addColumnValue(monthColumn, (String) data[0]);
            r.addColumnValue(enrollmentsColumn, (Integer) data[1]);

            System.out.println(data[0] + " En month");
            System.out.println(data[1] + " En Count for En in a month ");

            dataSet.addRow(r);
        }

        System.out.println(dataSet.getRows().size() + " En RecordSet Row count");
        return dataSet;
    }

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evaluationContext) throws EvaluationException {

        return getEnrollmentsInPeriod(dataSetDefinition, evaluationContext);
    }
}
