package org.openmrs.module.ugandaemrreports.definition.dataset.predicates;

import com.google.common.base.Predicate;
import org.openmrs.module.ugandaemrreports.common.ViralLoad;

/**
 * Created by carapai on 11/05/2017.
 */
public class ViralLoadTestedFilter implements Predicate<ViralLoad> {
    private Integer start;
    private Integer end;

    public ViralLoadTestedFilter(Integer start, Integer end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean apply(ViralLoad viralLoad) {
        return (viralLoad.getMonthsSinceArt() >= start) && (viralLoad.getMonthsSinceArt() <= end + 2);

    }
}
