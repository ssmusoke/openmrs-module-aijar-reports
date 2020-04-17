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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reporting.web.renderers.WebReportRenderer;
import org.openmrs.module.ugandaemrsync.tasks.SendDHIS2DataToCentralServerTask;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class SendReportRequestFragmentController {

    protected Log log = LogFactory.getLog(getClass());

    public SimpleObject post(@SpringBean ReportService reportService,
                             @RequestParam("request") String requestUuid) throws IOException {
        ReportRequest req = reportService.getReportRequestByUuid(requestUuid);
        if (req == null) {
            throw new IllegalArgumentException("ReportRequest not found");
        }

        RenderingMode renderingMode = req.getRenderingMode();
        String linkUrl = "/module/reporting/reports/reportHistoryOpen";

        if (renderingMode.getRenderer() instanceof WebReportRenderer) {

            throw new IllegalStateException("Web Renderers not yet implemented");
        } else {
            String filename = renderingMode.getRenderer().getFilename(req).replaceAll("-", "");
            String contentType = renderingMode.getRenderer().getRenderedContentType(req);
            byte[] data = reportService.loadRenderedOutput(req);

            if (data == null) {
                throw new IllegalStateException("Error retrieving the report");
            } else {

                return sendData(data);

            }

        }
    }

    public SimpleObject previewReport(@SpringBean ReportService reportService,
                                      @RequestParam("request") String requestUuid) throws IOException {
        ReportRequest req = reportService.getReportRequestByUuid(requestUuid);
        if (req == null) {
            throw new IllegalArgumentException("ReportRequest not found");
        }
        byte[] data=null;
        RenderingMode renderingMode = req.getRenderingMode();
        String linkUrl = "/module/reporting/reports/reportHistoryOpen";

        if (renderingMode.getRenderer() instanceof WebReportRenderer) {

            throw new IllegalStateException("Web Renderers not yet implemented");
        } else {
            String filename = renderingMode.getRenderer().getFilename(req).replace("-", "");
            String contentType = renderingMode.getRenderer().getRenderedContentType(req);
            data = reportService.loadRenderedOutput(req);
        }
        String jsonData=new String(data).replaceAll("-","");

        return SimpleObject.create("data",jsonData);
    }



    private SimpleObject sendData(byte[] data) throws IOException {
        Map map = new HashMap();
        SimpleObject simpleObject=new SimpleObject();
        try {
           SendDHIS2DataToCentralServerTask task = new SendDHIS2DataToCentralServerTask(data,simpleObject);
           task.execute();
            simpleObject=task.getServerResponseObject();
           System.out.println(simpleObject.toJson());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return simpleObject;
    }
}
