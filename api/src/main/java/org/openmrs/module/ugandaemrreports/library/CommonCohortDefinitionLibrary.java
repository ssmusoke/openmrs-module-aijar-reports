package org.openmrs.module.ugandaemrreports.library;

import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

/**
 * Library of common Cohort definitions
 */
@Component
public class CommonCohortDefinitionLibrary extends BaseDefinitionLibrary<CohortDefinition> {

    @Autowired
    private DataFactory df;

    @Override
    public Class<? super CohortDefinition> getDefinitionType() {
        return CohortDefinition.class;
    }

    @Override
    public String getKeyPrefix() {
        return "ugemr.cohort.common.";
    }

    /**
     * Patients who are female
     *
     * @return the cohort definition
     */
    public CohortDefinition females() {
        GenderCohortDefinition cd = new GenderCohortDefinition();
        cd.setName("Females");
        UUID uuid = UUID.randomUUID();
        cd.setUuid(String.valueOf(uuid));
        cd.setFemaleIncluded(true);
        return cd;
    }

    /**
     * Patients who are male
     *
     * @return the cohort definition
     */
    public CohortDefinition males() {
        GenderCohortDefinition cd = new GenderCohortDefinition();
        UUID uuid = UUID.randomUUID();
        cd.setUuid(String.valueOf(uuid));
        cd.setName("Males");
        cd.setMaleIncluded(true);
        return cd;
    }

    /**
     * Patients who at most maxAge years old on ${effectiveDate}
     *
     * @return the cohort definition
     */
    public CohortDefinition agedAtMost(int maxAge) {
        AgeCohortDefinition cd = new AgeCohortDefinition();
        cd.setName("aged at most " + maxAge);
        cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        cd.setMaxAge(maxAge);
        UUID uuid = UUID.randomUUID();
        cd.setUuid(String.valueOf(uuid));
        return df.convert(cd, ObjectUtil.toMap("effectiveDate=endDate"));
    }

    /**
     * Patients who are at least minAge years old on ${effectiveDate}
     *
     * @return the cohort definition
     */
    public CohortDefinition agedAtLeast(int minAge) {
        AgeCohortDefinition cd = new AgeCohortDefinition();
        cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        cd.setMinAge(minAge);
        UUID uuid = UUID.randomUUID();
        cd.setUuid(String.valueOf(uuid));
        return df.convert(cd, ObjectUtil.toMap("effectiveDate=endDate"));
    }

    /**
     * Patients who are at least minAge years old and are not more than maxAge on ${effectiveDate}
     *
     * @return the cohort definition
     */
    public CohortDefinition agedBetween(int minAge, int maxAge) {
        AgeCohortDefinition cd = new AgeCohortDefinition();
        cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        cd.setMinAge(minAge);
        cd.setMaxAge(maxAge);
        return df.convert(cd, ObjectUtil.toMap("effectiveDate=endDate"));
    }

    public CohortDefinition below6months() {
        AgeCohortDefinition cd = (AgeCohortDefinition) agedAtMost(6);
        cd.setMinAgeUnit(DurationUnit.MONTHS);
        UUID uuid = UUID.randomUUID();
        cd.setUuid(String.valueOf(uuid));
        return cd;
    }

    public CohortDefinition below2Years() {
        return agedAtMost(1);
    }
    public CohortDefinition below5Years() {
        return agedAtMost(4);
    }

    public CohortDefinition between6And59months() {
        AgeCohortDefinition cd = (AgeCohortDefinition) agedBetween(6, 59);
        cd.setMinAgeUnit(DurationUnit.MONTHS);
        cd.setMaxAgeUnit(DurationUnit.MONTHS);
        UUID uuid = UUID.randomUUID();
        cd.setUuid(String.valueOf(uuid));
        return cd;
    }

    public CohortDefinition between0And4years() {
        return agedAtMost(4);
    }

    public CohortDefinition between2And5Years() {
        return agedBetween(2, 4);
    }

    public CohortDefinition between5And14Years() {
        return agedBetween(5, 14);
    }

    public CohortDefinition between15And49Years() {
        return agedBetween(14, 49);
    }

    public CohortDefinition above15Years() {
        return agedAtLeast(15);
    }

    public CohortDefinition above50Years() {
        return agedAtLeast(50);
    }

    public CohortDefinition between0And10years() {
        return agedAtMost(10);
    }
    
    /**
     * MoH definition of children who is anybody 14 years and below
     * @return
     */
    public CohortDefinition MoHChildren(){
        return agedAtMost(14);
    }
    
    /**
     * MoH definition of adults who are 15 years and older
     * @return
     */
    public CohortDefinition MoHAdult(){
        return agedAtLeast(15);
    }

    public CohortDefinition above10years() {
        return agedAtLeast(11);
    }

}
