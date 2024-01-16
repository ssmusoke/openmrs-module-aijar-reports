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

package org.openmrs.module.ugandaemrreports.reports;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.metadata.CommonReportMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base implementation of ReportManager that provides some common method implementations
 */
public abstract class AggregateReportDataExportManager extends UgandaEMRDataExportManager {



	/**
	 * @return the json file for the report design for use in data set evaluator
	 */
	public  File getJsonReportDesign(){
		return 	getReportDesignFile(this.getUuid());
	}

	public File getReportDesignFile(String report_uuid) {

		File folder = FileUtils.toFile(AggregateReportDataExportManager.class.getClassLoader().getResource("report_designs"));
		if (folder.isDirectory()) {


			File[] files = folder.listFiles();
			File myFile = null;
			if (files != null) {
				for (File file : files) {
					if (file.isFile() && file.getName().endsWith(".json")) {
						ObjectMapper objectMapper = new ObjectMapper();
						objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

						try {
							JsonNode fileObject = objectMapper.readTree(file);
							JsonNode encounterNode = fileObject.path("report_uuid");
							if (encounterNode.asText().equals(report_uuid)) {
								myFile = file;
								break;
							}
						} catch (IOException e) {
							System.err.println("Error reading JSON file: " + file.getName());
							e.printStackTrace();
						}
					}
				}
			}

			return myFile;
		} else {
			return null;
		}
	}

}
