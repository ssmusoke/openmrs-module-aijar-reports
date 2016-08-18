package org.openmrs.module.ugandaemrreports.reports;

/**
 * Report Setup / teardown interface
 */
public interface SetupReport {

	public void setup() throws Exception;

	public void delete();
}
