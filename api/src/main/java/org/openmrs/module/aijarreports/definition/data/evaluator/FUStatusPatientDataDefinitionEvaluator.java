package org.openmrs.module.aijarreports.definition.data.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.openmrs.annotation.Handler;
import org.openmrs.module.aijarreports.common.PatientData;
import org.openmrs.module.aijarreports.common.Period;
import org.openmrs.module.aijarreports.common.Periods;
import org.openmrs.module.aijarreports.common.StubDate;
import org.openmrs.module.aijarreports.definition.data.definition.FUStatusPatientDataDefinition;
import org.openmrs.module.aijarreports.library.HIVPatientDataLibrary;
import org.openmrs.module.aijarreports.library.PatientDatasets;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.evaluator.PatientDataEvaluator;
import org.openmrs.module.reporting.data.patient.evaluator.SqlPatientDataEvaluator;
import org.openmrs.module.reporting.data.patient.service.PatientDataService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by carapai on 05/07/2016.
 */
@Handler(supports = FUStatusPatientDataDefinition.class, order = 50)
public class FUStatusPatientDataDefinitionEvaluator implements PatientDataEvaluator {
    protected static final Log log = LogFactory.getLog(FUStatusPatientDataDefinition.class);

    @Autowired
    private HIVPatientDataLibrary hivLibrary;

    @Autowired
    private PatientDataService patientDataService;

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private SqlPatientDataEvaluator sqlPatientDataEvaluator;

    @Override
    public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext context) throws EvaluationException {
        FUStatusPatientDataDefinition def = (FUStatusPatientDataDefinition) definition;

        EvaluatedPatientData c = new EvaluatedPatientData(def, context);

        if (context.getBaseCohort() != null && context.getBaseCohort().isEmpty()) {
            return c;
        }

        Period period = def.getPeriod();

        Date anotherDate = def.getOnDate();

        LocalDate workingDate = StubDate.dateOf(DateUtil.formatDate(anotherDate, "yyyy-MM-dd"));


        LocalDate localStartDate = null;
        LocalDate localEndDate = null;


        if (def.getPeriodToAdd() > 0) {
            if (period == Period.MONTHLY) {
                List<LocalDate> dates = Periods.addMonths(workingDate, def.getPeriodToAdd());
                localStartDate = dates.get(0);
                localEndDate = dates.get(1);
            } else if (period == Period.QUARTERLY) {
                List<LocalDate> dates = Periods.addQuarters(workingDate, def.getPeriodToAdd());
                localStartDate = dates.get(0);
                localEndDate = dates.get(1);
            }
        } else {
            if (period == Period.MONTHLY) {
                localStartDate = Periods.monthStartFor(workingDate);
                localEndDate = Periods.monthEndFor(workingDate);
            } else if (period == Period.QUARTERLY) {
                localStartDate = Periods.quarterStartFor(workingDate);
                localEndDate = Periods.quarterEndFor(workingDate);
            }
        }


        SqlPatientDataDefinition sqlPatientDataDefinition = PatientDatasets.getFUStatus(localStartDate.toDate(), localEndDate.toDate());
        EvaluatedPatientData data = sqlPatientDataEvaluator.evaluate(sqlPatientDataDefinition, context);

        Map<Integer, Object> evaluatedData = data.getData();

        for (Integer pId : evaluatedData.keySet()) {
            Object o = evaluatedData.get(pId);
            if (o != null) {
                String dt = (String) o;

                if (dt != null) {
                    String[] splitString = dt.split(",");
                    String s0 = splitString[0];
                    String s1 = splitString[1];
                    String s2 = splitString[2];
                    String s3 = splitString[3];
                    String s4 = splitString[4];
                    String s5 = splitString[5];

                    PatientData patientData = new PatientData();
                    patientData.setPeriod(period);
                    patientData.setPeriodDate(localEndDate.toDate());

                    if (!s0.equalsIgnoreCase("-")) {
                        Date encounterDate = DateUtil.parseDate(s0, "yyyy-MM-dd");
                        patientData.setEncounterDate(encounterDate);
                    }

                    if (!s1.equalsIgnoreCase("-")) {
                        Integer numberOfSinceLastVisit = Integer.valueOf(s1);
                        patientData.setNumberOfSinceLastVisit(numberOfSinceLastVisit);
                    }

                    if (!s2.equalsIgnoreCase("-")) {
                        Date deathDate = DateUtil.parseDate(s2, "yyyy-MM-dd");
                        patientData.setDeathDate(deathDate);
                    }

                    if (!s3.equalsIgnoreCase("-")) {
                        boolean transferredOut = true;
                        patientData.setTransferredOut(transferredOut);
                    }

                    if (!s4.equalsIgnoreCase("-")) {
                        Date nextVisitDate = DateUtil.parseDate(s4, "yyyy-MM-dd");
                        patientData.setNextVisitDate(nextVisitDate);
                    }

                    if (!s5.equalsIgnoreCase("-")) {
                        Date artStartDate = DateUtil.parseDate(s5, "yyyy-MM-dd");
                        patientData.setArtStartDate(artStartDate);
                    }

                    c.addData(pId, patientData);
                } else {
                    c.addData(pId, new PatientData());
                }
            }
        }
        return c;
    }

    protected Map<Integer, Date> getPatientMinimumArtDateMap(HqlQueryBuilder query, EvaluationContext context) {
        Map<Integer, Date> m = new HashMap<Integer, Date>();
        List<Object[]> queryResults = evaluationService.evaluateToList(query, context);
        for (Object[] row : queryResults) {
            Date a = (Date) row[1];
            Date b = (Date) row[2];
            Date minimum = a == null ? b : (b == null ? a : (a.before(b) ? a : b));
            m.put((Integer) row[0], minimum);
        }
        return m;
    }
}
