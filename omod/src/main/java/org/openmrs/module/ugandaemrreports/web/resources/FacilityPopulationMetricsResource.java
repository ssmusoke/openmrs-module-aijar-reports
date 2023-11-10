package org.openmrs.module.ugandaemrreports.web.resources;

import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + FacilityPopulationMetricsResource.UGANDAEMRREPORTS + FacilityPopulationMetricsResource.DEFINITION)

public class FacilityPopulationMetricsResource {
    public static final String UGANDAEMRREPORTS = "/ugandaemrreports";
    public static final String DEFINITION = "/genderMetrics";

    @ExceptionHandler(APIAuthenticationException.class)
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object evaluate() {

        AdministrationService administrationService = Context.getAdministrationService();

        String gender_query = "select  gender,COUNT(gender) AS patient_count  from person p  inner join patient pt  on p.person_id = pt.patient_id and pt.voided=0 GROUP BY p.gender;";

        String nationality_query = "SELECT cn.name, COUNT(patient_id)\n" +
                "FROM patient p\n" +
                "         LEFT JOIN person_attribute pa ON p.patient_id = pa.person_id\n" +
                "         INNER JOIN person_attribute_type pat ON pa.person_attribute_type_id = pat.person_attribute_type_id AND\n" +
                "                                                 pat.uuid = 'dec484be-1c43-416a-9ad0-18bd9ef28929'\n" +
                "         INNER JOIN concept_name cn ON cn.concept_id = pa.value\n" +
                "WHERE p.voided = 0\n" +
                "  AND cn.locale = 'en'\n" +
                "  AND cn.concept_name_type = 'FULLY_SPECIFIED'\n" +
                "GROUP BY value";

        List<List<Object>> total_patients_by_gender = administrationService.executeSQL(gender_query, true);
        List<List<Object>> total_patients_by_nationality = administrationService.executeSQL(nationality_query, true);



        HashMap<String,Object> list = new HashMap<>();

        SimpleObject genderList =getDisaggregations(total_patients_by_gender);
        SimpleObject nationalList =getDisaggregations(total_patients_by_nationality);
        list.put("gender",genderList);
        list.put("nationality",nationalList);

        return new ResponseEntity<>(list, HttpStatus.OK);

    }

    private SimpleObject getDisaggregations(List<List<Object>> results){

        SimpleObject object = new SimpleObject();
        List<Object> gender_list = new ArrayList();
        int totalPatients = 0;
        if (results.size() != 0) {
            for (List<Object> item : results) {
                int count =Integer.parseInt(item.get(1).toString());
                String gender = item.get(0).toString();
                totalPatients += count;
                SimpleObject objectNode = new SimpleObject();
                objectNode.put( gender,count);
                gender_list.add(objectNode);

            }
            object.add("disaggregation",gender_list);
            object.add("totalPatients",totalPatients);

        }
        return object;
    }

}
