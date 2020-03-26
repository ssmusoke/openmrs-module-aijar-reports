package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import com.google.common.base.Joiner;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.*;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.common.PersonDemographics;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.DispensingDatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.openmrs.module.ugandaemrreports.reports.Helper.getPatientDemographics;
import static org.openmrs.module.ugandaemrreports.reports.Helper.sqlConnection;
import static org.openmrs.module.ugandaemrreports.reports.Helper.processString;


/**
 */
@Handler(supports = {DispensingDatasetDefinition.class})
public class DispensingDatasetDefinitionEvaluator implements DataSetEvaluator {

    @Autowired
    EvaluationService evaluationService;


    Map<Integer,String> drugNames = new HashMap<>();
    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {

        DispensingDatasetDefinition definition = (DispensingDatasetDefinition) dataSetDefinition;


        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);

        String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");

        startDate = startDate+" 00:00:00";
        endDate = endDate+" 23:59:59";
        context = ObjectUtil.nvl(context, new EvaluationContext());

        String dataQuery = String.format("select p.obs_group_id,p.person_id,p.concept_id,p.value_numeric,p.value_coded,p.obs_datetime from obs p where p.obs_group_id in (select obs_id from obs o join encounter e on o.encounter_id = e.encounter_id join encounter_type t on e.encounter_type = t.encounter_type_id\n" +
                "where o.concept_id=163711 and o.obs_datetime between '%s' and '%s'  and t.uuid= '22902411-19c1-4a02-b19a-bf1a9c24fd51');",startDate,endDate);

        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(dataQuery);
        List<Object[]> results = evaluationService.evaluateToList(q, context);
        String drugs = "";
        if(results.size()>0 && !results.isEmpty()) {
            Set<String> drugConcepts = new HashSet<>();
            for (Object[] object : results) {
                if (String.valueOf(object[4]) != null) {
                    drugConcepts.add(String.valueOf(object[4]));
                }
            }
            drugs = Joiner.on(",").join(drugConcepts);

            String conceptNamesQuery = String.format("select concept_id, name from concept_name where concept_name_type='FULLY_SPECIFIED'and voided=0 and locale='en' and concept_id IN (%s);", drugs);
            SqlQueryBuilder p = new SqlQueryBuilder();
            p.append(conceptNamesQuery);
            List<Object[]> conceptNames = evaluationService.evaluateToList(p, context);


            for (Object[] drugName : conceptNames) {
                drugNames.put((int) drugName[0], String.valueOf(drugName[1]));
            }


            Connection connection = null;
            try {
                connection = sqlConnection();
                String allPatients = Joiner.on(",").join(results.stream().map(Object -> Object[1]).collect(Collectors.toList()));
                String drugOrderGroupIds = Joiner.on(",").join(results.stream().map(Object -> Object[0]).collect(Collectors.toList()));

                Set<Integer> orderIds = Stream.of(drugOrderGroupIds.split(","))
                        .map(Integer::parseInt)
                        .collect(Collectors.toSet());


                Map<Integer, List<PersonDemographics>> demographics = getPatientDemographics(connection, allPatients);

                PatientDataHelper pdh = new PatientDataHelper();
                for (int i : orderIds) {
                    DataSetRow row = new DataSetRow();

                    List<Object[]> drugOrder = results.stream().filter(Object -> Object[0].equals(i)).collect(Collectors.toList());
                    for (Object[] o : drugOrder) {
                        List<PersonDemographics> personDemographics = demographics.get(o[1]);
                        PersonDemographics personDemos = personDemographics != null && personDemographics.size() > 0 ? personDemographics.get(0) : new PersonDemographics();

                        int question = (int) o[2];

                        if (question == 1282) {
                            pdh.addCol(row, "drug", drugNames.get((int) o[4]));
                        }
                        if (question == 1443) {
                            pdh.addCol(row, "quantity", o[3]);
                        }
                        pdh.addCol(row, "date", DateUtil.formatDate(DateUtil.parseYmd(String.valueOf(o[5])), "yyyy-MM-dd"));
                        pdh.addCol(row, "Patient_ID", processString(personDemos.getIdentifiers()).get("e1731641-30ab-102d-86b0-7a5022ba4115"));

                    }
                    dataSet.addRow(row);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return dataSet;
    }

}
