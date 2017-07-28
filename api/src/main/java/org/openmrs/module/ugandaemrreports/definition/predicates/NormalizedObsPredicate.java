package org.openmrs.module.ugandaemrreports.definition.predicates;

import com.google.common.base.Predicate;
import org.openmrs.module.ugandaemrreports.common.NormalizedObs;


public class NormalizedObsPredicate implements Predicate<NormalizedObs> {

    private String periodType;
    private String period;

    public NormalizedObsPredicate(String periodType, String period) {
        this.periodType = periodType;
        this.period = period;
    }

    @Override
    public boolean apply(NormalizedObs normalizedObs) {
        if (normalizedObs != null) {
            switch (periodType) {
                case "value_datetime":
                    return normalizedObs.getValueDatetime() != null && (String.valueOf(normalizedObs.getValueDatetimeYear()).equals(period) || String.valueOf(normalizedObs.getValueDatetimeMonth()).equals(period) || normalizedObs.getValueDatetimeQuarter().equals(period));
                case "encounter_datetime":
                    return normalizedObs.getEncounterDatetime() != null && (String.valueOf(normalizedObs.getEncounterYear()).equals(period) || String.valueOf(normalizedObs.getEncounterMonth()).equals(period) || normalizedObs.getEncounterQuarter().equals(period));
                case "obs_datetime":
                    return normalizedObs.getObsDatetime() != null && (String.valueOf(normalizedObs.getObsDatetimeYear()).equals(period) || String.valueOf(normalizedObs.getObsDatetimeMonth()).equals(period) || normalizedObs.getObsDatetimeQuarter().equals(period));

            }
        }
        return false;
    }
}