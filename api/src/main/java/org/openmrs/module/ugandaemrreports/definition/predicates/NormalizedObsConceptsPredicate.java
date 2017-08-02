package org.openmrs.module.ugandaemrreports.definition.predicates;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import org.openmrs.module.ugandaemrreports.common.NormalizedObs;


public class NormalizedObsConceptsPredicate implements Predicate<NormalizedObs> {

    private String concepts;
    private String patient;

    public NormalizedObsConceptsPredicate(String concepts, String patient) {
        this.patient = patient;
        this.concepts = concepts;
    }

    @Override
    public boolean apply(NormalizedObs normalizedObs) {
        return normalizedObs.getPersonId().equals(Integer.valueOf(patient)) && Splitter.on(",").splitToList(concepts).contains(normalizedObs.getConcept());
    }
}