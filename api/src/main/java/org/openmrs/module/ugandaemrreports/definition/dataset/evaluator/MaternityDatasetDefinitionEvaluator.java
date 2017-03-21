package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import java.util.List;

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
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.MaternityDatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

@Handler(supports = { MaternityDatasetDefinition.class })
public class MaternityDatasetDefinitionEvaluator implements DataSetEvaluator {
	
	
	@Autowired
	private EvaluationService evaluationService;
	
	@Override
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
		SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
		MaternityDatasetDefinition definition = (MaternityDatasetDefinition) dataSetDefinition;
		
		String date = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
		
		LocalDate workingDate = StubDate.dateOf(date);
		
		int beginningMonth = workingDate.getMonthOfYear();
		int beginningYear = workingDate.getYear();
		
		context = ObjectUtil.nvl(context, new EvaluationContext());
		
		String sql = "SELECT\n"
		        + "  A.value_datetime                                                                                      AS art_start,\n"
		        + "  A.patient_id                                                                                          AS unique_id,\n"
		        + "  o1.value_datetime                                                                                     AS ti_date,\n"
		        + "  pi.identifier,\n" + "  pn.family_name,\n" + "  pn.given_name,\n" + "  p.gender,\n"
		        + "  if((year(a.value_datetime) - year(p.birthdate) - (right(a.value_datetime, 5) < right(p.birthdate, 5))) <= 2,\n"
		        + "     TIMESTAMPDIFF(MONTH, p.birthdate, A.value_datetime),\n"
		        + "     year(a.value_datetime) - year(p.birthdate) - (right(a.value_datetime, 5) < right(p.birthdate, 5))) AS age,\n"
		        + "  pa.county_district                                                                                    AS district,\n"
		        + "  pa.address3                                                                                           AS sub_county,\n"
		        + "  pa.address4                                                                                           AS parish,\n"
		        + "  pa.address5                                                                                           AS village,\n"
		        + "  YEAR(\n"
		        + "      p.death_date)                                                                                     AS death_year,\n"
		        + "  MONTH(\n"
		        + "      p.death_date)                                                                                     AS death_month\n"
		        + "\n" + "FROM\n" + "  (SELECT\n" + "     e.patient_id,\n" + "     e.encounter_datetime,\n"
		        + "     o.value_datetime\n" + "   FROM encounter e INNER JOIN obs o\n"
		        + String.format(
		            "       ON (o.person_id = e.patient_id AND o.concept_id = 99161 AND YEAR(o.value_datetime) = %s AND\n",
		            beginningYear)
		        + String.format(
		            "           MONTH(o.value_datetime) = %s AND e.encounter_type = 14)) A INNER JOIN person p\n",
		            beginningMonth)
		        + "    ON (p.person_id = A.patient_id)\n" + "  LEFT JOIN person_name pn ON (p.person_id = pn.person_id)\n"
		        + "  LEFT JOIN person_address pa ON (p.person_id = pa.person_id)\n"
		        + "  LEFT JOIN patient_identifier pi ON (p.person_id = pi.patient_id AND pi.identifier_type = 4)\n"
		        + "  LEFT JOIN obs o1 ON (o1.person_id = p.person_id AND o1.concept_id = 99160)";
		
		SqlQueryBuilder q = new SqlQueryBuilder(sql);
		
		//        SqlQueryBuilder followupQuery = new SqlQueryBuilder(followupSql);
		
		List<Object[]> results = evaluationService.evaluateToList(q, context);
		
		//        List<Object[]> followupResults = evaluationService.evaluateToList(followupQuery, context);
		
		PatientDataHelper pdh = new PatientDataHelper();
		
		for (Object[] r : results) {
			DataSetRow row = new DataSetRow();
			
			String name = new StringBuilder().append(r[4] + "\n").append(r[5]).toString();
			
			String address = new StringBuilder().append(r[8] + "\n").append(r[9] + "\n").append(r[10] + "\n")
			        .append(r[11] + "\n").toString();
			
			pdh.addCol(row, "Date ART Started", r[0]);
			pdh.addCol(row, "Unique ID no", r[1]);
			pdh.addCol(row, "Patient Clinic ID", r[3]);
			pdh.addCol(row, "TI", r[2]);
			pdh.addCol(row, "Name", name);
			pdh.addCol(row, "Gender", r[6]);
			pdh.addCol(row, "Age", r[7]);
			pdh.addCol(row, "Address", address);
			
			dataSet.addRow(row);
		}
		return dataSet;
		
	}
	
}
