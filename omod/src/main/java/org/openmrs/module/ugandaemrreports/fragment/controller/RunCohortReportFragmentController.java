/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ugandaemrreports.fragment.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.*;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.ReportingAuditToolDataSetDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


/**
 *  * Controller for a fragment that sends a report 
 */
public class RunCohortReportFragmentController {


    Cohort c = new Cohort("Patients with encounters", "Cohort 1", new ArrayList<>());
    Cohort c1 = new Cohort("Patients on appointment", "Cohort 2", new ArrayList<>());

    Cohort[] cohortArray = {c, c1};
    List<Cohort> cohorts = new ArrayList<>(Arrays.asList(cohortArray));


    protected Log log = LogFactory.getLog(getClass());

    AdministrationService administrationService = Context.getAdministrationService();

    public void get(@SpringBean ReportDefinitionService reportDefinitionService,
                           @SpringBean ReportService reportService,
                           @RequestParam("reportDefinition") String reportDefinitionUuid,
                           @RequestParam(value = "breadcrumb", required = false) String breadcrumb,
                           PageModel model) {

        ReportDefinition reportDefinition = reportDefinitionService.getDefinitionByUuid(reportDefinitionUuid);
        if (reportDefinition == null) {
            throw new IllegalArgumentException("No reportDefinition with the given uuid");
        }
        model.addAttribute("reportDefinition", reportDefinition);
        model.addAttribute("renderingModes", reportService.getRenderingModes(reportDefinition));
        model.addAttribute("breadcrumb", breadcrumb);

        model.addAttribute("cohorts", cohorts);

    }

    public String post(@SpringBean ReportService reportService,
                     @RequestParam(value = "breadcrumb", required = false) String breadcrumb,
                     UiUtils ui,
                     HttpServletRequest request,
                     @RequestParam("reportDefinition") ReportDefinition reportDefinition,
                     @RequestParam("renderingMode") String renderingModeDescriptor,
                     PageModel model) throws EvaluationException {

        RenderingMode renderingMode = null;
        for (RenderingMode candidate : reportService.getRenderingModes(reportDefinition)) {
            if (candidate.getDescriptor().equals(renderingModeDescriptor)) {
                renderingMode = candidate;
                break;
            }
        }

        EvaluationContext context = new EvaluationContext();

        Collection<Parameter> missingParameters = new ArrayList<Parameter>();
        Map<String, Object> parameterValues = new HashMap<String, Object>();
        for (Parameter parameter : reportDefinition.getParameters()) {
            String submitted = request.getParameter("parameterValues[" + parameter.getName() + "]");
            if (parameter.getCollectionType() != null) {
                throw new IllegalStateException("Collection parameters not yet implemented");
            }
            Object converted;
            if (StringUtils.isEmpty(submitted)) {
                converted = parameter.getDefaultValue();
            } else {
                converted = ui.convert(submitted, parameter.getType());
            }
            if (converted == null) {
                missingParameters.add(parameter);
            }
            parameterValues.put(parameter.getName(), converted);
        }

        context.setParameterValues(parameterValues);

        String cohortSelected = request.getParameter("cohort");

        parameterValues.put("cohortList",cohortSelected );


        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setReportDefinition(new Mapped<ReportDefinition>(reportDefinition, parameterValues));
        reportRequest.setRenderingMode(renderingMode);

        reportRequest = reportService.queueReport(reportRequest);
        reportService.processNextQueuedReports();


        model.addAttribute("reportDefinition", reportDefinition);
        model.addAttribute("renderingModes", reportService.getRenderingModes(reportDefinition));
        model.addAttribute("breadcrumb", breadcrumb);

        model.addAttribute("cohorts", cohorts);
        return "redirect:" + ui.pageLink("ugandaemrreports", "runCohortReport", SimpleObject.create("reportDefinition", reportDefinition.getUuid()));


    }

}
