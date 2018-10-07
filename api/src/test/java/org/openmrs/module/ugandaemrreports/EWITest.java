package org.openmrs.module.ugandaemrreports;

import com.google.common.base.Joiner;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.ugandaemrreports.common.EWIPatientData;
import org.openmrs.module.ugandaemrreports.common.EWIPatientEncounter;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static org.openmrs.module.ugandaemrreports.definition.dataset.queries.Queries.*;
import static org.openmrs.module.ugandaemrreports.reports.Helper.*;


public class EWITest {
   /* @Test
    public void testData() throws SQLException, ClassNotFoundException {
        String startDate = "2017-01-01";
        String endDate = "2017-12-31";

        String cohortQuertString = ewiQuery(startDate, endDate);


        List<Integer> patients = getEWICohort(testSqlConnection(), cohortQuertString);

        Assert.assertNotEquals(patients.size(), 0);

        String cohortString = Joiner.on(',').join(patients);


        String encounterQuery = ewiEncounterQuery(startDate, cohortString);

        List<EWIPatientEncounter> encounters = getEWIPatientEncounters(testSqlConnection(), encounterQuery);
        Assert.assertNotEquals(encounters.size(), 0);


        Map<Integer, List<EWIPatientEncounter>> groupedPatients = encounters.stream().collect(groupingBy(EWIPatientEncounter::getPersonId));

        Assert.assertNotEquals(groupedPatients.size(), 0);

        String ewiDataQuery = ewiPatientDataQuery(cohortString);

        List<EWIPatientData> ewiPatientData = getEWIPatients(testSqlConnection(), ewiDataQuery);

        Map<Integer, List<EWIPatientData>> groupedPatientData = ewiPatientData.stream().collect(groupingBy(EWIPatientData::getPersonId));

        Assert.assertNotEquals(groupedPatientData.size(), 0);

    }
    */
}
