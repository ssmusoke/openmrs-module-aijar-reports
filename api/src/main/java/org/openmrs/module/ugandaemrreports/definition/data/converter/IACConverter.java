package org.openmrs.module.ugandaemrreports.definition.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 */
public class IACConverter implements DataConverter {

    private Integer number;

    private String parameter;

    public IACConverter() {
    }

    public IACConverter(Integer number,String parameter) {
        this.parameter = parameter;
        this.number = number;
    }

    @Override
    public Object convert(Object original) {
        List<Obs> o = (List) original;
        if (o != null) {
            Collections.sort(o, Comparator.comparing(Obs::getObsDatetime));

            if (o.size() > this.number) {
                if(o.get(this.number).getValueCoded()!=null && parameter.equals("outcome")) {
                    return o.get(this.number).getValueCoded().getName().getName();
                }else if(o.get(this.number).getValueDatetime()!=null){
                    return formatDate(o.get(this.number).getValueDatetime());
                }
            }
        }
            return null;
    }

    /**
     * @see DataConverter#getDataType()
     */@Override
    public Class<?> getDataType() {
        return String.class;
    }

    /**
     * @see DataConverter#getInputDataType()
     */@Override
    public Class<?> getInputDataType() {
        return Concept.class;
    }

    private String formatDate(Date date) {
        DateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy");
        return dateFormatter.format(date);
    }
}
