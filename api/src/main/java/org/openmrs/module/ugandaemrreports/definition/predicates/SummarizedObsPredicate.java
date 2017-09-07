package org.openmrs.module.ugandaemrreports.definition.predicates;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import org.openmrs.module.ugandaemrreports.common.SummarizedObs;


public class SummarizedObsPredicate implements Predicate<SummarizedObs> {

    private String concept;
    private String period;

    public SummarizedObsPredicate(String concept, String period) {
        this.concept = concept;
        this.period = period;
    }

    @Override
    public boolean apply(SummarizedObs summarizedObs) {
        boolean bool = true;
        if(concept != null){
            bool = summarizedObs.getConcept().equals(concept);
        }
        if(period != null){
            bool = bool && summarizedObs.getM().equals(period);
        }
        return bool;
    }
}