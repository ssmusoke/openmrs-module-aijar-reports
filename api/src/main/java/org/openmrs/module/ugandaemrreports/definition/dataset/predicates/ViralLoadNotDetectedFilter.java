package org.openmrs.module.ugandaemrreports.definition.dataset.predicates;

import com.google.common.base.Predicate;
import org.openmrs.module.ugandaemrreports.common.ViralLoad;

/**
 * Created by carapai on 11/05/2017.
 */
public class ViralLoadNotDetectedFilter implements Predicate<ViralLoad> {
    private Integer start;
    private Integer end;

    public ViralLoadNotDetectedFilter(Integer start, Integer end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean apply(ViralLoad viralLoad) {
        return ((viralLoad.getValueCoded() == 1306 || viralLoad.getValueNumeric() < 1000.0D) && (viralLoad.getMonthsBetweenReportDateAndArt() >= start && viralLoad.getMonthsBetweenReportDateAndArt() <= end));
    }
}
