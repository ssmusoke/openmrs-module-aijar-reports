package org.openmrs.module.ugandaemrreports.definition.predicates;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.module.ugandaemrreports.common.NormalizedObs;
import org.openmrs.module.ugandaemrreports.common.SummarizedObs;


public class ReportTypePredicate implements Predicate<SummarizedObs> {

    private String periodGroupedBy;
    private String patient;

    public ReportTypePredicate(String periodGroupedBy) {
        this.periodGroupedBy = periodGroupedBy;
    }

    public ReportTypePredicate(String periodGroupedBy, String patient) {
        this.periodGroupedBy = periodGroupedBy;
        this.patient = patient;
    }

    @Override
    public boolean apply(SummarizedObs summarizedObs) {
        if (patient != null && StringUtils.isNotBlank(patient)) {
            return summarizedObs.getPeriodGroupedBy().equals(periodGroupedBy) && Splitter.on(",").splitToList(summarizedObs.getPatients()).contains(patient);
        }
        return summarizedObs.getPeriodGroupedBy().equals(periodGroupedBy);
    }
}