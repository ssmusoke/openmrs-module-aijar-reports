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
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reporting.web.renderers.WebReportRenderer;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.springframework.web.bind.annotation.RequestParam;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
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
            String filename = renderingMode.getRenderer().getFilename(req).replace(" ", "_");
            String contentType = renderingMode.getRenderer().getRenderedContentType(req);
            byte[] data = reportService.loadRenderedOutput(req);

            if (data == null) {
                throw new IllegalStateException("Error retrieving the report");
            } else {

                return sendData(data);

            }

        }
    }

    private SimpleObject sendData(byte[] data) throws IOException {
        Map map = new HashMap();
        SimpleObject simpleObject=new SimpleObject();
        try {
            URL url = new URL("https://ugisl.mets.or.ug:5000/ehmis");
            String encoding = Base64.getEncoder().encodeToString(("mets.mkaye:METS4321!").getBytes("UTF-8"));
            disableSSLCertificates();

            HttpsURLConnection httpsCon = (HttpsURLConnection) url.openConnection();
            httpsCon.setDoOutput(true);
            httpsCon.setRequestMethod("POST");
            httpsCon.setRequestProperty("Authorization", "Basic " + encoding);
            httpsCon.setRequestProperty("content-type", "application/json");
            OutputStream os = httpsCon.getOutputStream();
            os.write(data);
            httpsCon.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(httpsCon.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            os.close();

            int responseCode = ((HttpsURLConnection) httpsCon).getResponseCode();
            //reading the response
            if ((responseCode == 200 || responseCode == 201)) {
                InputStream inputStreamReader = httpsCon.getInputStream();
                String output1 = httpsCon.getResponseMessage();
                map.put("responseCode", output1);
            } else {
                map.put("responseCode", responseCode);
            }


            simpleObject=SimpleObject.create("message",response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return simpleObject;
    }

    /**
     * This function disables the SSLCertificate - need to activate SSL at the endpoint
     *
     * @Jmpango
     */
    private void disableSSLCertificates() throws Exception {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new SecureRandom());
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String urlHostName, SSLSession sslSession) {
                if (!urlHostName.equalsIgnoreCase(sslSession.getPeerHost())) {
                    log.info("Warning: URL host '" + urlHostName + "' is different to SSLSession host '" + sslSession.getPeerHost() + "'.");
                }
                return true;
            }
        };

        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
    }


}
