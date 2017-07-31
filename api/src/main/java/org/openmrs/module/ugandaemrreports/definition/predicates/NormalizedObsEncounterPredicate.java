package org.openmrs.module.ugandaemrreports.definition.predicates;

import com.google.common.base.Predicate;
import org.openmrs.module.ugandaemrreports.common.NormalizedObs;


public class NormalizedObsEncounterPredicate implements Predicate<NormalizedObs> {

    private String encounter;

    public NormalizedObsEncounterPredicate(String encounter) {
        this.encounter = encounter;
    }

    @Override
    public boolean apply(NormalizedObs normalizedObs) {
        return normalizedObs.getEncounter().equals(encounter);

    }
}