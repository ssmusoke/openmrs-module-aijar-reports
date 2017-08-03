package org.openmrs.module.ugandaemrreports.definition.predicates;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.module.ugandaemrreports.common.NormalizedObs;
import org.openmrs.module.ugandaemrreports.common.SummarizedObs;


public class SummarizedObsPatientPredicate implements Predicate<SummarizedObs> {

    private String patient;


    public SummarizedObsPatientPredicate(String patient) {
        this.patient = patient;
    }

    @Override
    public boolean apply(SummarizedObs summarizedObs) {
            return Splitter.on(",").splitToList(summarizedObs.getPatients()).contains(patient);
    }
}