/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.ugandaemrreports.reporting.library.dimension;

import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.CommonCohortLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

import static org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils.map;


/**
 * Library of common dimension definitions
 */
@Component
public class CommonReportDimensionLibrary {

    @Autowired
    private CommonCohortLibrary commonCohortLibrary;

    /**
     * Gender dimension
     * @return the dimension
     */
    public CohortDefinitionDimension gender() {
        CohortDefinitionDimension dim = new CohortDefinitionDimension();
        dim.setName("gender");
        dim.addCohortDefinition("M", map(commonCohortLibrary.males()));
        dim.addCohortDefinition("F", map(commonCohortLibrary.females()));
        return dim;
    }

    /**
     * Dimension of age using the 3 standard age groups
     * @return the dimension
     */
    public CohortDefinitionDimension get106AgeGroups() {
        CohortDefinitionDimension dim = new CohortDefinitionDimension();
        dim.setName("age groups (<2, 2-4, 5-14,15+)");
        dim.addParameter(new Parameter("onDate", "Date", Date.class));
        dim.addCohortDefinition("<2", map(commonCohortLibrary.agedAtMost(1), "effectiveDate=${onDate}"));
        dim.addCohortDefinition("2-<5", map(commonCohortLibrary.agedAtLeastAgedAtMost(2, 4), "effectiveDate=${onDate}"));
        dim.addCohortDefinition("5-14", map(commonCohortLibrary.agedAtLeastAgedAtMost(5,14), "effectiveDate=${onDate}"));
        dim.addCohortDefinition("15+", map(commonCohortLibrary.agedAtLeast(15), "effectiveDate=${onDate}"));
        return dim;
    }
}