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

package org.openmrs.module.ugandaemrreports.page;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reporting.web.renderers.WebReportRenderer;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.springframework.web.bind.annotation.RequestParam;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;

/**
 *
 */
public class SendDhisRequestPageController {
    protected Log log = LogFactory.getLog(getClass());


    public Object get(@SpringBean ReportService reportService,
                                    @RequestParam("requestuuid") String requestUuid) {
        ReportRequest req = reportService.getReportRequestByUuid(requestUuid);
        if (req == null) {
            throw new IllegalArgumentException("ReportRequest not found");
        }

        RenderingMode renderingMode = req.getRenderingMode();
        String linkUrl = "/module/reporting/reports/reportHistoryOpen";

        if (renderingMode.getRenderer() instanceof WebReportRenderer) {

            throw new IllegalStateException("Web Renderers not yet implemented");
        }
        else {
            String filename = renderingMode.getRenderer().getFilename(req).replace(" ", "_");
            String contentType = renderingMode.getRenderer().getRenderedContentType(req);
            byte[] data = reportService.loadRenderedOutput(req);

            if (data == null) {
                throw new IllegalStateException("Error retrieving the report");
            } else {
                try {
                    sendData(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

        }
    }
    private void sendData( byte[] data) throws IOException {
        try{
            URL url=new URL("https://ugisl.mets.or.ug:5000/ehmis");
            String encoding = Base64.getEncoder().encodeToString(("mets.mkaye:METS4321!").getBytes("UTF-8"));
            disableSSLCertificates();

            HttpsURLConnection httpsCon = (HttpsURLConnection) url.openConnection();
            httpsCon.setDoOutput(true);
            httpsCon.setRequestMethod("POST");
            httpsCon.setRequestProperty("Authorization", "Basic " + encoding);
            httpsCon.setRequestProperty("content-type","application/json");
            OutputStream os = httpsCon.getOutputStream();
            os.write(data);
            httpsCon.connect();
            BufferedReader br = new BufferedReader(  new InputStreamReader(httpsCon.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
            String output1=httpsCon.getResponseMessage();
            System.out.println(output1);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * This function disables the SSLCertificate - need to activate SSL at the endpoint
     */
    private void disableSSLCertificates () throws Exception {
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
                if(!urlHostName.equalsIgnoreCase(sslSession.getPeerHost())){
                    log.info("Warning: URL host '" + urlHostName + "' is different to SSLSession host '" + sslSession.getPeerHost() + "'.");
                }
                return true;
            }
        };

        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
    }


}
