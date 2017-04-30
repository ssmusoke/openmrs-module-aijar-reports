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
public class IptCtxConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        //1,2,ND,C,CTX,CTX✔
        Concept one = Context.getConceptService().getConceptByUuid("0192ca59-b647-4f88-b07e-8fda991ba6d6");//1
        Concept two = Context.getConceptService().getConceptByUuid("f1d5afce-8dfe-4d2d-b24b-051815d61848");//2
        Concept nd = Context.getConceptService().getConceptByUuid("f29f43c6-076a-4f70-9ae8-4563ac3fda80");//ND
        Concept c = Context.getConceptService().getConceptByUuid("dca06bae-30ab-102d-86b0-7a5022ba4115");//C
        Concept ctx = Context.getConceptService().getConceptByUuid("fca28768-50dc-4d6b-a3d2-2aae3b376b27");//CTX
        Concept ctxp = Context.getConceptService().getConceptByUuid("b0439b8e-469e-43e6-9dda-9b6b49f2147b");//CTX✔

        //get the coded value for the results
        Concept value = ((Obs)obj).getValueCoded();

        if(value.equals(one)){
            return "1";
        }
        else if(value.equals(two)){
            return "2";
        }
        else if(value.equals(nd)){
            return "ND";
        }
        else if(value.equals(c)){
            return "C";
        }
        else if(value.equals(ctx)){
            return "CTX";
        }
        else if(value.equals(ctxp)){
            return "CTX✔";
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
