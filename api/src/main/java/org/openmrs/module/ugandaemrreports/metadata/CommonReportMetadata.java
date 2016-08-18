package org.openmrs.module.ugandaemrreports.metadata;

import org.openmrs.PatientIdentifierType;
import org.springframework.stereotype.Component;

/**
 * Created by ssmusoke on 31/03/2016.
 */
@Component("commonReportMetadata")
public class CommonReportMetadata extends Metadata {

	public PatientIdentifierType getARTClinicNumber() {
		return getPatientIdentifierType("e1731641-30ab-102d-86b0-7a5022ba4115");
	}
}
