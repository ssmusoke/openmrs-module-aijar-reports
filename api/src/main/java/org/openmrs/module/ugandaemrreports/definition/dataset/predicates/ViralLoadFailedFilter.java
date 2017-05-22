package org.openmrs.module.ugandaemrreports.definition.dataset.predicates;

import com.google.common.base.Predicate;
import org.openmrs.module.ugandaemrreports.common.ViralLoad;

/**
 * Created by carapai on 11/05/2017.
 */
public class ViralLoadFailedFilter implements Predicate<ViralLoad> {
    private Integer month;

    public ViralLoadFailedFilter(Integer month) {
        this.month = month;
    }

    @Override
    public boolean apply(ViralLoad viralLoad) {
        return (viralLoad.getMonthsBetweenArtAndViralLoad() >= month) && (viralLoad.getMonthsBetweenArtAndViralLoad() <= month + 2) && (viralLoad.getValueNumeric() > 1000.0D);
    }
}
