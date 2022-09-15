package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.*;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.BooleanOperator;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.ActivesInCareCohortDefinition;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;


/**
 */
@Component
@Handler(supports = {ActivesInCareCohortDefinition.class})
public class ActiveInCareCohortDefinitionEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    HIVMetadata hivMetadata;

    @Autowired
    DataFactory df;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
        ActivesInCareCohortDefinition cd = (ActivesInCareCohortDefinition) cohortDefinition;


        context = ObjectUtil.nvl(context, new EvaluationContext());
        String startDate = "";
        String endDate = "";
        String days ="";

        startDate = DateUtil.formatDate(cd.getStartDate(), "yyyy-MM-dd");
        endDate = DateUtil.formatDate(cd.getEndDate(), "yyyy-MM-dd");
        days = cd.getLostToFollowupDays();



        //dead clients
        BirthAndDeathCohortDefinition dead = new BirthAndDeathCohortDefinition();
        dead.setDiedOnOrBefore(cd.getEndDate());

        //calculating lost cohorts
        SqlCohortDefinition lostClientsWithInPeriod = new SqlCohortDefinition("select t.patient_id from (select patient_id, max(value_datetime) return_visit_date,datediff('"+ endDate+"',max(value_datetime)) ltfp_days from encounter e inner  join obs o on e.encounter_id = o.encounter_id inner join encounter_type t on  t.encounter_type_id =e.encounter_type where encounter_datetime <='"+ endDate+"' " +
                "and t.uuid = '8d5b2be0-c2cc-11de-8d13-0010c6dffd0f' and  o.concept_id=5096 and o.value_datetime >= '"+startDate+"' and e.voided=0 and o.voided=0 group by patient_id) as t  where ltfp_days >=" + days +" ;");

        //has ART encounter during period
        EncounterCohortDefinition types = new EncounterCohortDefinition();
        types.setEncounterTypeList(hivMetadata.getArtEncounterTypes());
        types.setOnOrAfter(cd.getStartDate());
        types.setOnOrBefore(cd.getEndDate());

        //lost from previous quarter
        SqlCohortDefinition lost2 = new SqlCohortDefinition("select t.patient_id from (select patient_id, max(value_datetime) return_visit_date,datediff('"+startDate+"',max(value_datetime)) ltfp_days from encounter e\n" +
                "    inner  join obs o on e.encounter_id = o.encounter_id inner join encounter_type t on  t.encounter_type_id =e.encounter_type where encounter_datetime <='"+startDate+"' and encounter_datetime>= DATE_SUB('"+startDate+"', INTERVAL 3 month )\n" +
                "                and t.uuid = '8d5b2be0-c2cc-11de-8d13-0010c6dffd0f' and  o.concept_id=5096 and e.voided=0 and o.voided=0 group by patient_id) as t  where return_visit_date < '"+startDate+"' and  ltfp_days <" + days +" ;");

        CompositionCohortDefinition lostFromPreviousQuarter = df.getPatientsNotIn(lost2,types);

        CompositionCohortDefinition tx_Curr_lost_to_followup = df.getPatientsInAny(lostClientsWithInPeriod,lostFromPreviousQuarter);


        DateObsCohortDefinition transferredOut = new DateObsCohortDefinition();
        transferredOut.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        transferredOut.setQuestion(hivMetadata.getTransferredOutDate());
        transferredOut.setEncounterTypeList(null);
        transferredOut.setOperator1(RangeComparator.LESS_EQUAL);
        transferredOut.setValue1(cd.getEndDate());

        CompositionCohortDefinition excluded1 = df.getPatientsInAny(dead,transferredOut);
        CompositionCohortDefinition excludedPatients = df.getPatientsInAny(excluded1,tx_Curr_lost_to_followup);

        //transferred in
        DateObsCohortDefinition transferredInToCareDuringPeriod = new DateObsCohortDefinition();
        transferredInToCareDuringPeriod.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        transferredInToCareDuringPeriod.setQuestion(hivMetadata.getArtRegimenTransferInDate());
        transferredInToCareDuringPeriod.setEncounterTypeList(null);
        transferredInToCareDuringPeriod.setOperator1(RangeComparator.GREATER_EQUAL);
        transferredInToCareDuringPeriod.setValue1(cd.getStartDate());
        transferredInToCareDuringPeriod.setOperator2(RangeComparator.LESS_EQUAL);
        transferredInToCareDuringPeriod.setValue2(cd.getEndDate());

        //having base regimen during period
        CodedObsCohortDefinition havingBaseRegimenDuringQuarter = new CodedObsCohortDefinition();
        havingBaseRegimenDuringQuarter.setTimeModifier( BaseObsCohortDefinition.TimeModifier.ANY);
        havingBaseRegimenDuringQuarter.setQuestion(hivMetadata.getArtStartRegimen());
        havingBaseRegimenDuringQuarter.setEncounterTypeList(hivMetadata.getARTSummaryPageEncounterType());
        havingBaseRegimenDuringQuarter.setOnOrAfter(cd.getStartDate());
        havingBaseRegimenDuringQuarter.setOnOrBefore(cd.getEndDate());

        //having ARt start date within the period
        DateObsCohortDefinition havingArtStartDateDuringQuarter = new DateObsCohortDefinition();
        havingArtStartDateDuringQuarter.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        havingArtStartDateDuringQuarter.setQuestion(hivMetadata.getArtStartDate());
        havingArtStartDateDuringQuarter.setEncounterTypeList(hivMetadata.getARTSummaryPageEncounterType());
        havingArtStartDateDuringQuarter.setOperator1(RangeComparator.GREATER_EQUAL);
        havingArtStartDateDuringQuarter.setValue1(cd.getStartDate());
        havingArtStartDateDuringQuarter.setOperator2(RangeComparator.LESS_EQUAL);
        havingArtStartDateDuringQuarter.setValue2(cd.getEndDate());

        //received drugs during period
        CodedObsCohortDefinition onArtDuringQuarter = new CodedObsCohortDefinition();
        onArtDuringQuarter.setTimeModifier( BaseObsCohortDefinition.TimeModifier.ANY);
        onArtDuringQuarter.setQuestion(hivMetadata.getCurrentRegimen());
        onArtDuringQuarter.setEncounterTypeList( hivMetadata.getARTEncounterPageEncounterType());
        onArtDuringQuarter.setOnOrAfter(cd.getStartDate());
        onArtDuringQuarter.setOnOrBefore(cd.getEndDate());

        //have an active return visit date
        SqlCohortDefinition patientsWithActiveReturnVisitDate = new SqlCohortDefinition("select distinct patient_id from encounter e inner  join obs o on e.encounter_id = o.encounter_id  inner join encounter_type t on  t.encounter_type_id =e.encounter_type where encounter_datetime <= '"+ endDate+"' and t.uuid = '8d5b2be0-c2cc-11de-8d13-0010c6dffd0f' and  o.concept_id=5096 and o.value_datetime >= '"+startDate+"' and e.voided=0 and o.voided=0;");

        CompositionCohortDefinition allActivePatients = df.getPatientsInAny(patientsWithActiveReturnVisitDate,transferredInToCareDuringPeriod,onArtDuringQuarter,havingArtStartDateDuringQuarter,havingBaseRegimenDuringQuarter);

        CompositionCohortDefinition activeExcludingDeadLostAndTransfferedOut = df.getPatientsNotIn(allActivePatients,excludedPatients);

        Cohort cohort =Context.getService(CohortDefinitionService.class).evaluate(activeExcludingDeadLostAndTransfferedOut, context);

        return new EvaluatedCohort(new Cohort(cohort.getMemberIds()), cd, context);
    }

}
