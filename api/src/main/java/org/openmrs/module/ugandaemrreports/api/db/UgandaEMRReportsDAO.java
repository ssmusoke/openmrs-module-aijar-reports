package org.openmrs.module.ugandaemrreports.api.db;

import org.openmrs.*;
import org.openmrs.Concept;
import org.openmrs.logic.op.In;
import org.openmrs.module.ugandaemrreports.api.UgandaEMRReportsService;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.reporting.ReportObjectWrapper;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Set;

/**
 * Database methods for {@link UgandaEMRReportsService}.
 */
public interface UgandaEMRReportsDAO {
    void executeFlatteningScript();

    List<ReportObjectWrapper> getReportObjects(String type);
    PatientSearch getPatientSearchByUuid(String uuid);

    Cohort getPatientCurrentlyInPrograms(String uuid);

    List<Integer> getObsConceptsFromEncounters(EncounterType encounterType);
    Map<Integer, String> getPatientsConditionsStatus(org.openmrs.cohort.Cohort patients, Concept codedCondition);

    Set<Concept> getAllConditions();

    Map<Integer,Object> getLatestPatientAppointmentsScheduled(org.openmrs.cohort.Cohort patients, int limit);

    List<Object> getNonCodedOrderReasons(OrderType orderType);

    List<Concept> getCodedOrderReasons(OrderType orderType);

    Map<Integer, Map<String, Object>> getDrugOrderByIndication(org.openmrs.cohort.Cohort patients,String drugIndication,OrderType orderType);
}
