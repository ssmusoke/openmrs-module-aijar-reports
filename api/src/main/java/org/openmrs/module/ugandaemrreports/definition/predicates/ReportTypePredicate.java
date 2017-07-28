package org.openmrs.module.ugandaemrreports.definition.predicates;

import com.google.common.base.Predicate;
import org.openmrs.module.ugandaemrreports.common.NormalizedObs;
import org.openmrs.module.ugandaemrreports.common.SummarizedObs;


public class ReportTypePredicate implements Predicate<SummarizedObs> {

    private String periodGroupedBy;

    public ReportTypePredicate(String periodGroupedBy) {
        this.periodGroupedBy = periodGroupedBy;
    }

    @Override
    public boolean apply(SummarizedObs summarizedObs) {
        return summarizedObs.getPeriodGroupedBy().equals(periodGroupedBy);
    }
}