 package org.openmrs.module.ugandaemrreports.definition.dataset.definition;

 import java.util.Date;
 import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
 import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;





 public class ViralLoadDatasetDefinition
   extends BaseDataSetDefinition
 {
   @ConfigurationProperty
   private Date startDate;
   @ConfigurationProperty
   private Date endDate;

   public ViralLoadDatasetDefinition() {}

   public ViralLoadDatasetDefinition(String name, String description)
   {
     super(name, description);
   }

   public Date getStartDate() {
     return this.startDate;
   }

   public void setStartDate(Date startDate) {
     this.startDate = startDate;
   }

   public Date getEndDate() {
     return this.endDate;
   }

   public void setEndDate(Date endDate) {
     this.endDate = endDate;
   }
 }