package org.openmrs.module.ugandaemrreports.reports;


        import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
        import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
        import org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition;
        import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
        import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
        import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
        import org.openmrs.module.reporting.evaluation.parameter.Mapped;
        import org.openmrs.module.reporting.evaluation.parameter.Parameter;
        import org.openmrs.module.reporting.report.ReportDesign;
        import org.openmrs.module.reporting.report.definition.ReportDefinition;
        import org.openmrs.module.ugandaemrreports.definition.data.converter.BirthDateConverter;
        import org.openmrs.module.ugandaemrreports.library.ARTClinicCohortDefinitionLibrary;
        import org.openmrs.module.ugandaemrreports.library.BasePatientDataLibrary;
        import org.openmrs.module.ugandaemrreports.library.DataFactory;
        import org.openmrs.module.ugandaemrreports.library.HIVPatientDataLibrary;
        import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.stereotype.Component;

        import java.util.ArrayList;
        import java.util.List;
        import java.util.Properties;

/**
 * Daily Appointments List report
 */
        @Component
        public class SetUpLostToFollowUp extends UgandaEMRDataExportManager {

        @Autowired
        private DataFactory df;

        @Autowired
        ARTClinicCohortDefinitionLibrary hivCohorts;

        @Autowired
        private BuiltInPatientDataLibrary builtInPatientData;

        @Autowired
        private HIVPatientDataLibrary hivPatientData;

        @Autowired
        private BasePatientDataLibrary basePatientData;

        @Autowired
        private HIVMetadata hivMetadata;

        /**
         * @return the uuid for the report design for exporting to Excel
         */
        @Override
        public String getExcelDesignUuid() {
        return "c6db4c87-2c2a-4cef-8b48-3e344a412d99";
        }

        @Override
        public String getUuid() {
        return "a549c20a-ff8e-4b92-afbe-aeeaa4e47ff7";
        }

        @Override
        public String getName() {
        return "Lost to Follow up";
        }

        @Override
        public String getDescription() {
        return "Lost to Follow up";
        }

        @Override
        public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(df.getStartDateParameter());
        l.add(df.getEndDateParameter());
        return l;
        }

        @Override
        public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        List<ReportDesign> l = new ArrayList<ReportDesign>();
        l.add(buildReportDesign(reportDefinition));
        return l;
        }

        /**
         * Build the report design for the specified report, this allows a user to override the report design by adding
         * properties and other metadata to the report design
         *
         * @param reportDefinition
         * @return The report design
         */
        @Override

        public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "LostToFollowUp.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:7,dataset:LOST_TO_FOLLOW_UP");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
        }

        @Override
        public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();

        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        PatientDataSetDefinition dsd = new PatientDataSetDefinition();

        CohortDefinition clientsLostToFollowUp = df.getLostToFollowUp();

        dsd.setName(getName());
        dsd.setParameters(getParameters());
        dsd.addRowFilter(Mapped.mapStraightThrough(clientsLostToFollowUp));
        dsd.addColumn("Patient Name", new PreferredNameDataDefinition(), (String) null);
        dsd.addColumn("Sex", new GenderDataDefinition(), (String) null);
        dsd.addColumn("Birth Date", new BirthdateDataDefinition(), (String) null);
                addColumn(dsd, "Age", builtInPatientData.getAgeAtStart());
                addColumn(dsd, "Telephone", basePatientData.getTelephone());
                addColumn(dsd, "HIV Enrolled Date", hivPatientData.getEnrollmentDate());
                addColumn(dsd, "ART Start Date", hivPatientData.getArtStartDate());
                addColumn(dsd, "Current Regimen",hivPatientData.getCurrentRegimen());
                addColumn(dsd, "Last Visit Date",hivPatientData.getLastVisitDate());
                addColumn(dsd, "Last Appointment",hivPatientData.getExpectedReturnDate());
                addColumn(dsd, "Date Seen", hivPatientData.getLastARTEncounter());

        rd.addDataSetDefinition("LOST_TO_FOLLOW_UP", Mapped.mapStraightThrough(dsd));
        rd.setBaseCohortDefinition(Mapped.mapStraightThrough(clientsLostToFollowUp));

        return rd;
        }

        @Override
        public String getVersion() {
        return "0.3";
        }
        }
