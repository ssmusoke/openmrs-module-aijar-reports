/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.ugandaemrreports.reporting.dataset.definition;

import org.openmrs.Concept;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.person.definition.ObsForPersonDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.ugandaemrreports.definition.data.definition.CalculationDataDefinition;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.AgeLimitCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.PersonAddressCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.WhoCd4VLCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.pnc.RtwRfwCalculation;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 */
@Component
public class SharedDataDefintion {

    public DataDefinition definition(String name, Concept concept) {
        ObsForPersonDataDefinition obsForPersonDataDefinition = new ObsForPersonDataDefinition();
        obsForPersonDataDefinition.setName(name);
        obsForPersonDataDefinition.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        obsForPersonDataDefinition.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        obsForPersonDataDefinition.setQuestion(concept);
        obsForPersonDataDefinition.setWhich(TimeQualifier.LAST);
        return obsForPersonDataDefinition;
    }
    public DataDefinition villageParish(){
        CalculationDataDefinition cdf =new CalculationDataDefinition("village+parish", new PersonAddressCalculation());
        cdf.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cdf;
    }
    public DataDefinition age(Integer lower, Integer upper) {
        CalculationDataDefinition cdf = new CalculationDataDefinition("Age-"+lower+"-"+upper+"yrs", new AgeLimitCalculation());
        cdf.addCalculationParameter("lowerLimit", lower);
        cdf.addCalculationParameter("upperLimit", upper);
        cdf.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cdf;
    }
    public DataDefinition whoCd4Vl(String q, String a){
        CalculationDataDefinition cd = new CalculationDataDefinition("", new WhoCd4VLCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        cd.addCalculationParameter("question", q);
        cd.addCalculationParameter("answer", a);
        return cd;
    }

    public DataDefinition referredToOrFrom(){
        CalculationDataDefinition cd = new CalculationDataDefinition("RTW/RFW", new RtwRfwCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }
}
