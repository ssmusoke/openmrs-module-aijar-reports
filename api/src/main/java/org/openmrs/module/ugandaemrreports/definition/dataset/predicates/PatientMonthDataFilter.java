package org.openmrs.module.ugandaemrreports.definition.dataset.predicates;

import com.google.common.base.Predicate;
import org.openmrs.module.ugandaemrreports.common.PatientMonthData;

/**
 * Created by carapai on 11/05/2017.
 */
public class PatientMonthDataFilter implements Predicate<PatientMonthData> {
    private Integer month;
    private Integer dataType;

    public PatientMonthDataFilter(Integer month, Integer dataType) {
        this.month = month;
        this.dataType = dataType;
    }

    @Override
    public boolean apply(PatientMonthData patientMonthData) {
        return (patientMonthData.getDataType().equals(dataType)) && (patientMonthData.getMonth() >= month) && (patientMonthData.getMonth() <= month + 2);

    }
}