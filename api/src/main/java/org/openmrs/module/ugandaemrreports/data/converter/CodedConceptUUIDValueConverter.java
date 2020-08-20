package org.openmrs.module.ugandaemrreports.data.converter;

import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.data.converter.DataConverter;


public class CodedConceptUUIDValueConverter implements DataConverter {

    public CodedConceptUUIDValueConverter() {
          }
    public Object convert(Object original) {
        Obs o = (Obs)original;
        if (o == null) {
            return null;
        } else if (o.getValueBoolean() != null) {
            return o.getValueBoolean();
        } else if (o.getValueCoded() != null) {
            return ObjectUtil.format(o.getValueCoded().getUuid());

        } else {
            return o.getValueTime() != null ? o.getValueTime() : o.getValueAsString(Context.getLocale());
        }
    }

    @Override
    public Class<?> getInputDataType() {
        return null;
    }

    @Override
    public Class<?> getDataType() {
        return null;
    }
}
