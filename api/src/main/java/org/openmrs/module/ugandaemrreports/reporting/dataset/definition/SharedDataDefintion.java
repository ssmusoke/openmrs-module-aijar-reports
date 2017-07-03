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
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by Nicholas Ingosi on 5/10/17.
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
}
