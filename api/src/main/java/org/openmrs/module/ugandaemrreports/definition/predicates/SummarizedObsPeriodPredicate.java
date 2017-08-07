package org.openmrs.module.ugandaemrreports.definition.predicates;

import com.google.common.base.Predicate;
import org.openmrs.module.reportingcompatibility.service.ReportService;


public class SummarizedObsPeriodPredicate implements Predicate<String> {

    private String period;
    private ReportService.Modifier modifier;

    public SummarizedObsPeriodPredicate(String period, ReportService.Modifier modifier) {
        this.period = period;
        this.modifier = modifier;
    }

    @Override
    public boolean apply(String summarizedObs) {

        if (modifier.equals(ReportService.Modifier.GREATER_THAN)) {
            return Integer.valueOf(summarizedObs) > Integer.valueOf(period);

        }
        if (modifier.equals(ReportService.Modifier.LESS_THAN)) {
            return Integer.valueOf(summarizedObs) < Integer.valueOf(period);

        }
        if (modifier.equals(ReportService.Modifier.GREATER_EQUAL)) {
            return Integer.valueOf(summarizedObs) >= Integer.valueOf(period);

        }
        if (modifier.equals(ReportService.Modifier.LESS_EQUAL)) {
            return Integer.valueOf(summarizedObs) <= Integer.valueOf(period);

        }
        return summarizedObs.equals(period);
    }
}