package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import org.joda.time.Months;
import org.openmrs.*;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.PreferredIdentifierDataDefinition;
import org.openmrs.module.reporting.data.patient.service.PatientDataService;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.data.definition.EncounterObsDataDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.EIDDatasetDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.openmrs.module.ugandaemrreports.reports.Helper.convert;

/**
 * Created by carapai on 27/09/2017.
 */
@Handler(supports = {EIDDatasetDefinition.class})

public class EIDDatasetEvaluator implements DataSetEvaluator {
    @Autowired
    private HIVMetadata hivMetadata;

    PatientDataHelper pdh = new PatientDataHelper();

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evaluationContext) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, evaluationContext);
        EncounterObsDataDefinition eidSummaryObsDefinition = new EncounterObsDataDefinition();
        eidSummaryObsDefinition.setEncounterType(hivMetadata.getEIDSummaryPageEncounterType().get(0));

        EncounterObsDataDefinition eidEncounterObsDefinition = new EncounterObsDataDefinition();
        eidEncounterObsDefinition.setEncounterType(hivMetadata.getEIDEncounterPageEncounterType().get(0));

        PreferredIdentifierDataDefinition preferredIdentifierDataDefinition = new PreferredIdentifierDataDefinition();
        preferredIdentifierDataDefinition.setIdentifierType(MetadataUtils.existing(PatientIdentifierType.class, "2c5b695d-4bf3-452f-8a7c-fe3ee3432ffe"));

        EvaluatedPatientData eidEvaluatedSummaryObs = Context.getService(PatientDataService.class).evaluate(eidSummaryObsDefinition, evaluationContext);
        EvaluatedPatientData eidEvaluatedEncounterObs = Context.getService(PatientDataService.class).evaluate(eidEncounterObsDefinition, evaluationContext);

        EvaluatedPatientData patientIdentifierEvaluator = Context.getService(PatientDataService.class).evaluate(preferredIdentifierDataDefinition, evaluationContext);


        Map<Integer, Object> eidEncounters = eidEvaluatedEncounterObs.getData();
        Map<Integer, Object> eidSummaries = eidEvaluatedSummaryObs.getData();
        Map<Integer, Object> patientIdentifiers = patientIdentifierEvaluator.getData();


        for (Map.Entry<Integer, Object> data : eidSummaries.entrySet()) {
            List<Obs> summaryObs = (List<Obs>) data.getValue();
            List<Obs> encounterObs = (List<Obs>) (eidEncounters.get(data.getKey()));

            DataSetRow row = new DataSetRow();
            Obs firstObs = summaryObs.get(0);
            Person p = firstObs.getPerson();

            PatientIdentifier identifier = (PatientIdentifier) patientIdentifiers.get(data.getKey());

            this.pdh.addCol(row, "EIDNo", identifier != null ? identifier.getIdentifier() : "");
            this.pdh.addCol(row, "registrationDate", DateUtil.formatDate(firstObs.getEncounter().getEncounterDatetime(), "yyyy-MM-dd"));
            this.pdh.addCol(row, "surname", p.getFamilyName());
            this.pdh.addCol(row, "firstName", p.getGivenName());
            this.pdh.addCol(row, "sex", p.getGender());
            this.pdh.addCol(row, "dob", DateUtil.formatDate(p.getBirthdate(),"dd/MM/yyyy"));
            this.pdh.addCol(row, "age", getMonthsBetweenDates(p.getBirthdate(), firstObs.getEncounter().getEncounterDatetime()));

            Obs entryPoint = searchObs(summaryObs, 90200);
            Obs nvp = searchObs(summaryObs, 99771);
            Obs cotrim = searchObs(summaryObs, 99773);
            Obs motherFirstName = searchObs(summaryObs, 99776);
            Obs motherLastName = searchObs(summaryObs, 99775);
            Obs motherANCNo = searchObs(summaryObs, 99777);
            Obs motherArtNo = searchObs(summaryObs, 162874);
            Obs motherARV4ANC = searchObs(summaryObs, 99783);
            Obs motherARV4Delivery = searchObs(summaryObs, 99784);
            Obs motherARV4PNC = searchObs(summaryObs, 99785);
            Obs infantARV4PMTCT = searchObs(summaryObs, 99787);

            // First PCR
            Obs firstPCRDate = searchObs(summaryObs, 99606);
            Obs fsAtFirstPCR = searchObs(summaryObs, 99434);
            Obs firstPCRResult = searchObs(summaryObs, 99435);
            Obs dateFirstPCRGiven2CareGiver = searchObs(summaryObs, 99438);

            // Second PCR
            Obs secondPCRDate = searchObs(summaryObs, 99436);
            Obs fsAtSecondPCR = searchObs(summaryObs, 99794);
            Obs secondPCRResult = searchObs(summaryObs, 99440);
            Obs dateSecondPCRGiven2CareGiver = searchObs(summaryObs, 99442);

            // Rapid Test
            Obs rapidTestDate = searchObs(summaryObs, 162879);
            Obs rapidTestResult = searchObs(summaryObs, 162880);

            // Final outcomes

            Obs finalOutcome = searchObs(summaryObs, 99428);
            Obs enrolled = searchObs(summaryObs, 163004);
            Obs preArtNo = searchObs(summaryObs, 99751);


            this.pdh.addCol(row, "entry", entryPoint != null ? convert(String.valueOf(entryPoint.getValueCoded().getConceptId())) : "");
            this.pdh.addCol(row, "nvp", nvp != null ? DateUtil.formatDate(nvp.getValueDatetime(),"dd/MM/yyyy") : "");
            this.pdh.addCol(row, "nvpAge", getMonthsBetweenDates(p.getBirthdate(), nvp != null ? nvp.getValueDatetime() : null));
            this.pdh.addCol(row, "cotrim", cotrim != null ? DateUtil.formatDate(cotrim.getValueDatetime(), "dd/MM/yyyy") : "");
            this.pdh.addCol(row, "cotrimAge", getMonthsBetweenDates(p.getBirthdate(), cotrim != null ? cotrim.getValueDatetime() : null));
            this.pdh.addCol(row, "motherFirstName", motherFirstName != null ? motherFirstName.getValueText() : "");
            this.pdh.addCol(row, "motherLastName", motherLastName != null ? motherLastName.getValueText() : "");
            this.pdh.addCol(row, "motherANCNo", motherANCNo != null ? motherANCNo.getValueText() : "");
            this.pdh.addCol(row, "motherArtNo", motherArtNo != null ? motherArtNo.getValueText() : "");
            this.pdh.addCol(row, "motherNewlyTested", "");
            this.pdh.addCol(row, "motherNewlyEnrolled", "");
            this.pdh.addCol(row, "motherARV4ANC", motherARV4ANC != null ? motherARV4ANC.getValueCoded().getName().getName() : "");
            this.pdh.addCol(row, "motherARV4Delivery", motherARV4Delivery != null ?
                    motherARV4Delivery.getValueCoded().getName().getName() : "");
            this.pdh.addCol(row, "motherARV4PNC", motherARV4PNC != null ? motherARV4PNC.getValueCoded().getName().getName() : "");
            this.pdh.addCol(row, "infantARV4PMTCT", infantARV4PMTCT != null ? convert(String.valueOf(infantARV4PMTCT.getValueCoded().getConceptId())) : "");

            // First PCR
            this.pdh.addCol(row, "firstPCR", firstPCRDate != null ? "✓" : "");
            this.pdh.addCol(row, "firstPCRDateCollected", firstPCRDate != null ? DateUtil.formatDate(firstPCRDate.getValueDatetime(), "yyyy-MM-dd") : "");
            this.pdh.addCol(row, "firstPCRDateDispatched", "");
            this.pdh.addCol(row, "ageAtFirstPCR", getMonthsBetweenDates(p.getBirthdate(), firstPCRDate != null ? firstPCRDate.getValueDatetime() : null));
            this.pdh.addCol(row, "fsAtFirstPCR", fsAtFirstPCR != null ? convert(String.valueOf(fsAtFirstPCR.getValueCoded().getConceptId())) : "");
            this.pdh.addCol(row, "firstPCRResult", firstPCRResult != null ? firstPCRResult.getValueCoded().getName().getName() : "");
            this.pdh.addCol(row, "firstPCRResultDateReceived", "");
            this.pdh.addCol(row, "dateFirstPCRGiven2CareGiver", dateFirstPCRGiven2CareGiver != null ?
                    DateUtil.formatDate(dateFirstPCRGiven2CareGiver.getValueDatetime(), "yyyy-MM-dd") : "");

            this.pdh.addCol(row, "firstPCRRepeat", "");
            this.pdh.addCol(row, "firstPCRRepeatDateCollected", "");
            this.pdh.addCol(row, "firstPCRRepeatDateDispatched", "");
            this.pdh.addCol(row, "ageAtFirstPCRRepeat", "");
            this.pdh.addCol(row, "fsAtFirstPCRRepeat", "");
            this.pdh.addCol(row, "firstPCRRepeatResult", "");
            this.pdh.addCol(row, "firstPCRRepeatResultDateReceived", "");
            this.pdh.addCol(row, "dateFirstPCRRepeatGiven2CareGiver", "");

            //Second PCR
            this.pdh.addCol(row, "secondPCR", secondPCRDate != null ? "✓" : "");
            this.pdh.addCol(row, "secondPCRDateCollected", secondPCRDate != null ? DateUtil.formatDate(secondPCRDate.getValueDatetime(), "yyyy-MM-dd") : "");
            this.pdh.addCol(row, "secondPCRDateDispatched", "");
            this.pdh.addCol(row, "ageAtSecondPCR", getMonthsBetweenDates(p.getBirthdate(), secondPCRDate != null ? secondPCRDate.getValueDatetime() : null));
            this.pdh.addCol(row, "fsAtSecondPCR", fsAtSecondPCR != null ? convert(String.valueOf(fsAtSecondPCR.getValueCoded().getConceptId())) : "");
            this.pdh.addCol(row, "secondPCRResult", secondPCRResult != null ? secondPCRResult.getValueCoded().getName().getName() : "");
            this.pdh.addCol(row, "secondPCRResultDateReceived", "");
            this.pdh.addCol(row, "dateSecondPCRGiven2CareGiver", dateSecondPCRGiven2CareGiver != null ?
                    DateUtil.formatDate(dateSecondPCRGiven2CareGiver.getValueDatetime(), "yyyy-MM-dd") : "");

            this.pdh.addCol(row, "secondPCRRepeat", "");
            this.pdh.addCol(row, "secondPCRRepeatDateCollected", "");
            this.pdh.addCol(row, "secondPCRRepeatDateDispatched", "");
            this.pdh.addCol(row, "ageAtSecondPCRRepeat", "");
            this.pdh.addCol(row, "fsAtSecondPCRRepeat", "");
            this.pdh.addCol(row, "secondPCRRepeatResult", "");
            this.pdh.addCol(row, "secondPCRRepeatResultDateReceived", "");
            this.pdh.addCol(row, "dateSecondPCRRepeatGiven2CareGiver", "");

            // Rapid Test

            this.pdh.addCol(row, "rapidTestDate", rapidTestDate != null ? DateUtil.formatDate(rapidTestDate.getValueDatetime(),"yyyy-MM-dd") : "");
            this.pdh.addCol(row, "ageAtRapidTest", getMonthsBetweenDates(p.getBirthdate(), rapidTestDate != null ? rapidTestDate.getValueDatetime() : null));
            this.pdh.addCol(row, "rapidTestResult", rapidTestResult != null ? rapidTestResult.getValueCoded().getName().getName() : "");

            // Followup
            addColumns(filterMap(encounterObs, 162993), row, "1");
            addColumns(filterMap(encounterObs, 162994), row, "2");
            addColumns(filterMap(encounterObs, 162995), row, "3");
            addColumns(filterMap(encounterObs, 162996), row, "4");
            addColumns(filterMap(encounterObs, 162997), row, "5");
            addColumns(filterMap(encounterObs, 162998), row, "6");
            addColumns(filterMap(encounterObs, 162999), row, "7");
            addColumns(filterMap(encounterObs, 163000), row, "8");
            addColumns(filterMap(encounterObs, 163001), row, "9");
            addColumns(filterMap(encounterObs, 163002), row, "10");
            addColumns(filterMap(encounterObs, 163018), row, "11");
            addColumns(filterMap(encounterObs, 163019), row, "12");
            addColumns(filterMap(encounterObs, 163020), row, "13");
            addColumns(filterMap(encounterObs, 163021), row, "14");
            addColumns(filterMap(encounterObs, 163022), row, "15");

            // Final outcomes

            this.pdh.addCol(row, "dischargedNegative", finalOutcome != null && finalOutcome.getValueCoded().getConceptId().equals(99427) ? "✓" : "");
            this.pdh.addCol(row, "lost", finalOutcome != null && finalOutcome.getValueCoded().getConceptId().equals(5240) ? "✓" : "");
            this.pdh.addCol(row, "referred", finalOutcome != null && finalOutcome.getValueCoded().getConceptId().equals(99430) ? "✓" : "");
            this.pdh.addCol(row, "transferred", finalOutcome != null && finalOutcome.getValueCoded().getConceptId().equals(90306) ? "✓" : "");
            this.pdh.addCol(row, "died", finalOutcome != null && finalOutcome.getValueCoded().getConceptId().equals(99112) ? "✓" : "");

            this.pdh.addCol(row, "enrolled", enrolled != null ? "✓" : "");
            this.pdh.addCol(row, "preArtNo", preArtNo != null ? preArtNo.getValueText() : "");
            this.pdh.addCol(row, "clinic1", "");
            this.pdh.addCol(row, "clinic2", "");
            dataSet.addRow(row);
        }

        return dataSet;
    }

    private Obs searchObs(List<Obs> obs, Integer conceptId) {
        if (obs != null) {
            return obs.stream()
                    .filter(x -> x.getConcept().getConceptId().equals(conceptId))
                    .findAny()
                    .orElse(null);
        }
        return null;
    }

    private Obs searchObs(List<Obs> obs, Integer conceptId, Integer valueCodedId) {
        if (obs != null) {
            return obs.stream()
                    .filter(x -> Objects.equals(x.getConcept().getConceptId(), conceptId) && Objects.equals(x.getValueCoded().getConceptId(), valueCodedId))
                    .findAny()
                    .orElse(null);
        }
        return null;
    }

    private String getMonthsBetweenDates(Date date1, Date date2) {
        if (date1 != null && date2 != null) {
            return String.valueOf(Months.monthsBetween(StubDate.dateOf(date1), StubDate.dateOf(date2)).getMonths());
        }

        return "-";
    }

    private List<Obs> filterMap(List<Obs> data, Integer concept) {
        Obs visit = searchObs(data, 162992, concept);

        List<Obs> results = new ArrayList<>();
        if (visit == null) {
            return results;
        }

        for (Obs o : data) {
            if (Objects.equals(visit.getEncounter().getEncounterId(), o.getEncounter().getEncounterId())) {
                results.add(o);
            }
        }
        return results;
    }

    private void addColumns(List<Obs> data, DataSetRow row, String visit) {
        if (data.size() > 0) {
            Obs firstObs = data.get(0);
            Obs appointmentDate = searchObs(data, 99443);
            Obs age = searchObs(data, 99449);
            Obs ctx = searchObs(data, 99798);
            Obs nvp = searchObs(data, 99799);
            Obs infantFeeding = searchObs(data, 99451);
            Obs zScores = searchObs(data, 99800);
            Obs muac = searchObs(data, 99801);
            Obs motherARVs = searchObs(data, 162854);

            String ctxString = ctx != null && Objects.equals(ctx.getValueCoded().getConceptId(), 90003) ? "Y" : "N";
            String nvpString = nvp != null && Objects.equals(nvp.getValueCoded().getConceptId(), 90003) ? "Y" : "N";
            String zScoresString = zScores != null ? zScores.getValueCoded().getName().getName() : "";
            String muacString = muac != null ? muac.getValueCoded().getName().getName() : "";

            this.pdh.addCol(row, "appointmentDate" + visit, appointmentDate != null ? DateUtil.formatDate(appointmentDate.getValueDatetime(), "yyyy-MM-dd") : "");
            this.pdh.addCol(row, "visitDate" + visit, DateUtil.formatDate(firstObs.getEncounter().getEncounterDatetime(), "yyyy-MM-dd"));
            this.pdh.addCol(row, "age" + visit, age != null ? age.getValueNumeric() : "");
            this.pdh.addCol(row, "ctx/nvp" + visit, ctxString + "/" + nvpString);
            this.pdh.addCol(row, "infantFeeding" + visit, infantFeeding != null ? convert(String.valueOf(infantFeeding.getValueCoded().getConceptId())) : "");
            this.pdh.addCol(row, "zScores/muac" + visit, zScoresString + "/" + muacString);
            this.pdh.addCol(row, "motherARVs" + visit, motherARVs != null && Objects.equals(motherARVs.getValueCoded().getConceptId(), 90003) ? "Y" : "N");
        } else {
            this.pdh.addCol(row, "appointmentDate" + visit, "");
            this.pdh.addCol(row, "visitDate" + visit, "");
            this.pdh.addCol(row, "age" + visit, "");
            this.pdh.addCol(row, "ctx/nvp" + visit, "");
            this.pdh.addCol(row, "infantFeeding" + visit, "");
            this.pdh.addCol(row, "zScores/muac" + visit, "");
            this.pdh.addCol(row, "motherARVs" + visit, "");
        }
    }
}
