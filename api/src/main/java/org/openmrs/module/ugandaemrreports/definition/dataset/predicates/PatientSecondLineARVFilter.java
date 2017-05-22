package org.openmrs.module.ugandaemrreports.definition.dataset.predicates;

import com.google.common.base.Predicate;
import org.openmrs.module.ugandaemrreports.common.PatientARV;

import java.util.Collection;

/**
 * Created by carapai on 11/05/2017.
 */
public class PatientSecondLineARVFilter implements Predicate<PatientARV> {
    private Integer month;
    private Collection<Integer> secondLineDrugsChildren;
    private Collection<Integer> secondLineDrugsAdults;

    public PatientSecondLineARVFilter(Integer month, Collection<Integer> secondLineDrugsChildren, Collection<Integer> secondLineDrugsAdults) {
        this.month = month;
        this.secondLineDrugsChildren = secondLineDrugsChildren;
        this.secondLineDrugsAdults = secondLineDrugsAdults;
    }

    @Override
    public boolean apply(PatientARV patientARV) {
        return (patientARV.getMonthsFromEnrollment() >= month) && (patientARV.getMonthsFromEnrollment() <= month + 2) && (((secondLineDrugsChildren.contains(patientARV.getValueCoded())) && (patientARV.getAgeAtEncounter() <= 10)) || ((secondLineDrugsAdults.contains(patientARV.getValueCoded())) && (patientARV.getAgeAtEncounter() > 10)));

    }
}
