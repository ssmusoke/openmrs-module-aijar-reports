package org.openmrs.module.ugandaemrreports.definition.dataset.predicates;

import com.google.common.base.Predicate;
import org.openmrs.module.ugandaemrreports.common.PatientARV;
import org.openmrs.module.ugandaemrreports.common.ViralLoad;

/**
 * Created by carapai on 11/05/2017.
 */
public class PatientARVFilter implements Predicate<PatientARV> {
    private Integer month;

    public PatientARVFilter(Integer month) {
        this.month = month;
    }

    @Override
    public boolean apply(PatientARV patientARV) {
        return (patientARV.getMonthsFromEnrollment() >= month) && (patientARV.getMonthsFromEnrollment() <= month + 2);
    }
}
