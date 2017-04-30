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
package org.openmrs.module.ugandaemrreports.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Created by Nicholas Ingosi on 4/29/17.
 */
public class EmctCodesConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        Concept value = ((Obs)obj).getValueCoded();

        Concept trr = Context.getConceptService().getConceptByUuid("25c448ff-5fe4-4a3a-8c0a-b5aaea9d5465");//TRR
        Concept tr = Context.getConceptService().getConceptByUuid("86e394fd-8d85-4cb3-86d7-d4b9bfc3e43a");//TR
        Concept c = Context.getConceptService().getConceptByUuid("6da9b915-8668-4642-8ed4-7d2a346881cb");//C
        Concept t = Context.getConceptService().getConceptByUuid("05f16fc5-1d82-4ce8-9b44-a3125fbbf2d7");//T
        Concept trP = Context.getConceptService().getConceptByUuid("12d878f9-899c-4b3c-bf57-c6226c307a53");//TR✔
        Concept trrP = Context.getConceptService().getConceptByUuid("60155e4d-1d49-4e97-9689-758315967e0f");//TRR✔
        Concept tkr = Context.getConceptService().getConceptByUuid("81bd3e58-9389-41e7-be1a-c6723f899e56");//TRK
        Concept trrk = Context.getConceptService().getConceptByUuid("1f177240-85f6-4f10-964a-cfc7722408b3");//TRRK
        Concept trRetest = Context.getConceptService().getConceptByUuid("a08d9331-b437-485c-8eff-1923f3d43630");//TR+
        Concept trrRetest = Context.getConceptService().getConceptByUuid("8dcaefaa-aa91-4c24-aaeb-122cff549ab3");//TRR+

        if(value.equals(trr)) {
          return "TRR";
        }

        else if(value.equals(c)) {
            return "C";
        }
        else if(value.equals(t)) {
            return "T";
        }
        else if(value.equals(trP)) {
            return "TR✔";
        }
        else if(value.equals(trrP)) {
            return "TRR✔";
        }
        else if(value.equals(tkr)) {
            return "TRK";
        }
        else if(value.equals(trrk)) {
            return "TRRK";
        }
        else if(value.equals(trRetest)) {
            return "TR+";
        }
        else if(value.equals(trrRetest)) {
            return "TRR+";
        }
        else if(value.equals(tr)) {
            return "TR";
        }
        return null;
    }

    @Override
    public Class<?> getInputDataType() {
        return Obs.class;
    }

    @Override
    public Class<?> getDataType() {
        return String.class;
    }
}
