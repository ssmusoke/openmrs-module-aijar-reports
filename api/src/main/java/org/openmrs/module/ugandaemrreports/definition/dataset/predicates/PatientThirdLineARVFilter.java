package org.openmrs.module.ugandaemrreports.definition.dataset.predicates;

import com.google.common.base.Predicate;
import org.openmrs.module.ugandaemrreports.common.PatientARV;

import java.util.Collection;

/**
 * Created by carapai on 11/05/2017.
 */
public class PatientThirdLineARVFilter implements Predicate<PatientARV> {
    private Integer month;
    private Collection<Integer> thirdLineDrugs;

    public PatientThirdLineARVFilter(Integer month, Collection<Integer> thirdLineDrugs) {
        this.month = month;
        this.thirdLineDrugs = thirdLineDrugs;
    }

    @Override
    public boolean apply(PatientARV patientARV) {
        return (patientARV.getMonthsFromEnrollment() >= month) && (patientARV.getMonthsFromEnrollment() <= month + 2) && (thirdLineDrugs.contains(patientARV.getValueCoded()));

    }
}