package org.openmrs.module.ugandaemrreports.definition.dataset.predicates;

import com.google.common.base.Predicate;
import org.openmrs.module.ugandaemrreports.common.PatientNonSuppressingData;

/**
 * Created by carapai on 11/05/2017.
 */
public class NonSuppressedDataFilter implements Predicate<PatientNonSuppressingData> {
    private Integer concept;

    public NonSuppressedDataFilter(Integer concept) {
        this.concept = concept;
    }

    @Override
    public boolean apply(PatientNonSuppressingData patientNonSuppressingData) {
        return patientNonSuppressingData.getConcept().equals(concept);
    }
}