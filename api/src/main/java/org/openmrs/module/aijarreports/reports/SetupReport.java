package org.openmrs.module.aijarreports.reports;

/**
 * Report Setup / teardown interface
 */
public interface SetupReport {
	public void setup() throws Exception;
	public void delete();
}
