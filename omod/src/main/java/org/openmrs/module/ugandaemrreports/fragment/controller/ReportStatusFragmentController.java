/*
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

package org.openmrs.module.ugandaemrreports.fragment.controller;

import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.report.Report;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.InteractiveReportRenderer;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

/**
 *
 */
public class ReportStatusFragmentController {

    public List<SimpleObject> getQueuedRequests(@RequestParam(value = "reportDefinition", required = false) String reportDefinitionUuid,
                                                @SpringBean ReportDefinitionService reportDefinitionService,
                                                @SpringBean ReportService reportService,
                                                UiUtils ui) {

        ReportDefinition reportDefinition = null;
        if (reportDefinitionUuid != null) {
            reportDefinition = reportDefinitionService.getDefinitionByUuid(reportDefinitionUuid);
        }
        List<ReportRequest> requests = reportService.getReportRequests(reportDefinition, null, null, ReportRequest.Status.REQUESTED, ReportRequest.Status.PROCESSING);

        final Map<ReportRequest, QueueStatus> queueStatus = new HashMap<ReportRequest, QueueStatus>();
        for (ReportRequest request : requests) {
            if (request.getStatus().equals(ReportRequest.Status.REQUESTED)) {
                Integer positionInQueue = reportService.getPositionInQueue(request);

                // due to an underlying bug in the reporting module, the status on a REQUESTED request isn't updated when it starts PROCESSING
                ReportRequest.Status statusOverride = null;
                List<String> reportLog = reportService.loadReportLog(request);
                if (reportLog != null) {
                    for (String s : reportLog) {
                        if (s.indexOf("Starting to process report") != -1) {
                            statusOverride = ReportRequest.Status.PROCESSING;
                        }
                    }
                }

                queueStatus.put(request, new QueueStatus(positionInQueue, statusOverride));
            }
        }

        Collections.sort(requests, new Comparator<ReportRequest>() {
            @Override
            public int compare(ReportRequest left, ReportRequest right) {
                return OpenmrsUtil.compareWithNullAsGreatest(queueStatus.get(left), queueStatus.get(right));
            }
        });

        return simplify(ui, requests, queueStatus, null);

    }

    public List<SimpleObject> getCompletedRequests(@RequestParam(value = "reportDefinition", required = false) String reportDefinitionUuid,
                                                   @SpringBean ReportDefinitionService reportDefinitionService,
                                                   @SpringBean ReportService reportService,
                                                   UiUtils ui) {
        ReportDefinition reportDefinition = null;
        if (reportDefinitionUuid != null) {
            reportDefinition = reportDefinitionService.getDefinitionByUuid(reportDefinitionUuid);
        }

        List<ReportRequest> requests = reportService.getReportRequests(reportDefinition, null, null, 10, ReportRequest.Status.FAILED, ReportRequest.Status.COMPLETED, ReportRequest.Status.SAVED);

        Map<ReportRequest, String> errorMessages = new HashMap<ReportRequest, String>();
        for (ReportRequest request : requests) {
            if (ReportRequest.Status.FAILED.equals(request.getStatus())) {
                errorMessages.put(request, reportService.loadReportError(request));
            }
        }

        Collections.sort(requests, new Comparator<ReportRequest>() {
            @Override
            public int compare(ReportRequest left, ReportRequest right) {
                return OpenmrsUtil.compareWithNullAsGreatest(right.getEvaluateStartDatetime(), left.getEvaluateStartDatetime());
            }
        });

        return simplify(ui, requests, null, errorMessages);
    }

    public FragmentActionResult cancelRequest(@RequestParam("reportRequest") String reportRequestUuid,
                              @SpringBean ReportService reportService,
                              UiUtils ui) {
        ReportRequest request = reportService.getReportRequestByUuid(reportRequestUuid);
        if (ReportRequest.Status.REQUESTED.equals(request.getStatus())) {
            reportService.purgeReportRequest(request);
            return new SuccessResult(ui.message("reportingui.reportRequest.cancel.successMessage"));
        }
        return new FailureResult(ui.message("reportingui.reportRequest.cancel.errorMessage"));
    }

    public FragmentActionResult saveRequest(@RequestParam("reportRequest") String reportRequestUuid,
                                            @RequestParam(value="description", required=false) String description,
                                            @SpringBean ReportService reportService,
                                            UiUtils ui) {
        ReportRequest request = reportService.getReportRequestByUuid(reportRequestUuid);
        if (ReportRequest.Status.COMPLETED.equals(request.getStatus())) {
            Report report = reportService.loadReport(request);
            reportService.saveReport(report, description);
            return new SuccessResult(ui.message("reportingui.reportRequest.save.successMessage"));
        }
        return new FailureResult(ui.message("reportingui.reportRequest.save.errorMessage"));
    }


    public List<SimpleObject> simplify(UiUtils ui, List<ReportRequest> requests, Map<ReportRequest, QueueStatus> queueStatus, Map<ReportRequest, String> errorMessages) {
        List<SimpleObject> ret = new ArrayList<SimpleObject>();
        for (ReportRequest request : requests) {
            ret.add(simplify(ui, request, queueStatus == null ? null : queueStatus.get(request), errorMessages == null ? null : errorMessages.get(request)));
        }

        return ret;
    }

    private SimpleObject simplify(UiUtils ui, ReportRequest request, QueueStatus queueStatus, String error) {
        SimpleObject simple = SimpleObject.fromObject(request, ui,
                "uuid", "renderingMode.label", "priority", "schedule",
                "requestedBy", "requestDate", "status",
                "evaluateStartDatetime", "evaluateCompleteDatetime", "renderCompleteDatetime");

        if (queueStatus != null) {
            simple.put("positionInQueue", queueStatus.getPositionInQueue());
            if (queueStatus.getStatusOverride() != null) {
                simple.put("status", queueStatus.getStatusOverride());
            }
        }
        ((SimpleObject) simple.get("renderingMode")).put("interactive", request.getRenderingMode().getRenderer() instanceof InteractiveReportRenderer);
        simple.put("reportDefinition", simplify(ui, request.getReportDefinition()));
        simple.put("baseCohort", simplify(ui, request.getBaseCohort()));
        simple.put("errorMessage", error);

        return simple;
    }

    public class QueueStatus implements Comparable<QueueStatus> {
        private Integer positionInQueue;
        private ReportRequest.Status statusOverride;

        public QueueStatus(Integer positionInQueue, ReportRequest.Status statusOverride) {
            this.positionInQueue = positionInQueue;
            this.statusOverride = statusOverride;
        }

        public Integer getPositionInQueue() {
            return positionInQueue;
        }

        public ReportRequest.Status getStatusOverride() {
            return statusOverride;
        }

        @Override
        public int compareTo(QueueStatus other) {
            if (ReportRequest.Status.PROCESSING.equals(statusOverride)) {
                return -1;
            } else if (ReportRequest.Status.PROCESSING.equals(other.statusOverride)) {
                return 1;
            }
            return OpenmrsUtil.compareWithNullAsGreatest(positionInQueue, other.positionInQueue);
        }
    }

    public class ParamValue {
        private String name;
        private String value;

        public ParamValue(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

    private SimpleObject simplify(UiUtils ui, Mapped<? extends Parameterizable> mapped) {
        if (mapped == null) {
            return null;
        }

        List<ParamValue> parameterMappings = new ArrayList<ParamValue>();
        for (Map.Entry<String, Object> entry : mapped.getParameterMappings().entrySet()) {
            parameterMappings.add(new ParamValue(entry.getKey(), ui.format(entry.getValue())));
        }

        SimpleObject simple = new SimpleObject();
        simple.put("mappings", parameterMappings);
        if (mapped.getParameterizable() != null) {
            simple.put("name", mapped.getParameterizable().getName());
            simple.put("description", mapped.getParameterizable().getDescription());
        }
        return simple;
    }

}
