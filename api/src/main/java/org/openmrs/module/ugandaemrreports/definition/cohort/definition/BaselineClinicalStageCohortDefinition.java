package org.openmrs.module.ugandaemrreports.definition.cohort.definition;

 import java.util.List;
 import org.openmrs.Concept;
 import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
 import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;



 public class BaselineClinicalStageCohortDefinition
   extends BaseCohortDefinition
 {
   @ConfigurationProperty
   private List<Concept> values;

   public List<Concept> getValues()
   {
     return this.values;
   }

   public void setValues(List<Concept> values) {
     this.values = values;
   }
 }