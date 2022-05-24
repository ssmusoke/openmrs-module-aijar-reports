package org.openmrs.module.ugandaemrreports.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.PatientService;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;

public class PatientsFragmentController {

    protected final Log log = LogFactory.getLog(getClass());

    public void controller(FragmentModel model, @SpringBean("patientService") PatientService service) {
        model.addAttribute("patients", service.getAllPatients());
        log.error("Jacob- In PatientsFragmentController PatientsFragmentController");
    }
}
