package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import org.joda.time.LocalDate;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.Observation;
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.common.PatientEncounterObs;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.HCTDatasetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.TBDatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.openmrs.module.ugandaemrreports.reports.Helper.getEncounterObs;
import static org.openmrs.module.ugandaemrreports.reports.Helper.processString2;
import static org.openmrs.module.ugandaemrreports.reports.Helper.sqlConnection;

@Handler(supports = {HCTDatasetDefinition.class})
public class HCTDatasetEvaluator implements DataSetEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        HCTDatasetDefinition definition = (HCTDatasetDefinition) dataSetDefinition;

        Date startDate = definition.getStartDate();
        Date endDate = definition.getEndDate();

        PatientDataHelper pdh = new PatientDataHelper();


        try {
            List<PatientEncounterObs> patientEncounterObs = getEncounterObs(sqlConnection(), "334bf97e-28e2-4a27-8727-a5ce31c7cd66", null, startDate, endDate);

            for (PatientEncounterObs data : patientEncounterObs) {
                DataSetRow row = new DataSetRow();
                List<Observation> observations = data.getObs();
                List<String> addresses = processString2(data.getAddresses());
                pdh.addCol(row, "data", data.getEncounterDate());
                pdh.addCol(row, "client name", data.getNames());
                pdh.addCol(row, "reg no", "");

                Integer age = data.getAge();

                if (age < 5) {
                    pdh.addCol(row, "<5", age);
                } else {
                    pdh.addCol(row, "<5", "");
                }

                if (age < 10 && age >= 5) {
                    pdh.addCol(row, "<10", age);
                } else {
                    pdh.addCol(row, "<10", "");
                }

                if (age < 15 && age >= 10) {
                    pdh.addCol(row, "<15", age);
                } else {
                    pdh.addCol(row, "<15", "");
                }

                if (age < 19 && age >= 15) {
                    pdh.addCol(row, "<19", age);
                } else {
                    pdh.addCol(row, "<19", "");
                }

                if (age < 49 && age >= 19) {
                    pdh.addCol(row, "<49", age);
                } else {
                    pdh.addCol(row, "<49", "");
                }

                if (age >= 49) {
                    pdh.addCol(row, ">49", age);
                } else {
                    pdh.addCol(row, ">49", "");
                }

                pdh.addCol(row, "sex", data.getGender());
                pdh.addCol(row, "marital status", "");

                if (addresses.size() == 6) {
                    pdh.addCol(row, "District", addresses.get(1));
                    pdh.addCol(row, "Subcounty/Parish", addresses.get(3) + " " + addresses.get(4));
                    pdh.addCol(row, "Village/Cell", addresses.get(5));

                } else {
                    pdh.addCol(row, "District", "");
                    pdh.addCol(row, "Subcounty/Parish", "");
                    pdh.addCol(row, "Village/Cell", "");
                }


                dataSet.addRow(row);

            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return dataSet;

    }

}
