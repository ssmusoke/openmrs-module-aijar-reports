package org.openmrs.module.ugandaemrreports.library;

import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.ugandaemrreports.UgandaEMRReportUtil;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.CommonCohortLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

/**
 * Common dimensions shared across multiple reports
 */
@Component
public class CommonDimensionLibrary extends BaseDefinitionLibrary<CohortDefinitionDimension> {

    @Autowired
    private CommonCohortDefinitionLibrary cohortDefinitionLibrary;
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    TBCohortDefinitionLibrary tbCohortDefinitionLibrary;

    @Autowired
    private DataFactory df;
    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private CommonCohortLibrary commonCohortLibrary;


    @Override
    public Class<? super CohortDefinitionDimension> getDefinitionType() {
        return CohortDefinitionDimension.class;
    }

    @Override
    public String getKeyPrefix() {
        return "ugemr.dim.common.";
    }

    /**
     * Gender dimension
     *
     * @return the dimension
     */
    public CohortDefinitionDimension genders() {
        CohortDefinitionDimension dimGender = new CohortDefinitionDimension();
        dimGender.setName("Gender");
        dimGender.addCohortDefinition(cohortDefinitionLibrary.males().getName(), Mapped.mapStraightThrough(cohortDefinitionLibrary.males()));
        dimGender.addCohortDefinition(cohortDefinitionLibrary.females().getName(), Mapped.mapStraightThrough(cohortDefinitionLibrary.females()));

        return dimGender;
    }

    /**
     * Dimension of age using the standard age groups
     *
     * @return the dimension
     */
    public CohortDefinitionDimension get106aAgeGroup() {
        CohortDefinitionDimension dimAges = new CohortDefinitionDimension();
        dimAges.setName("Age - 106a 1A");
        dimAges.setDescription("Age desegregation for HMIS 106A Section 1A which are  - AgesGroup (< 2 years, 2 - 5 years, 5 - 14 years, 15+ years)");
        dimAges.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        dimAges.addCohortDefinition("< 2 years", UgandaEMRReportUtil
                .map(cohortDefinitionLibrary.below2Years(), "effectiveDate=${effectiveDate}"));
        dimAges.addCohortDefinition("2 - 5 years", UgandaEMRReportUtil
                .map(cohortDefinitionLibrary.between2And5Years(), "effectiveDate=${effectiveDate}"));
        dimAges.addCohortDefinition("5 - 14 years", UgandaEMRReportUtil
                .map(cohortDefinitionLibrary.between5And14Years(), "effectiveDate=${effectiveDate}"));
        dimAges.addCohortDefinition("15+ years", UgandaEMRReportUtil
                .map(cohortDefinitionLibrary.above15Years(), "effectiveDate=${effectiveDate}"));

        return dimAges;
    }


    /**
     * Dimension of age using the standard age and gender groups
     *
     * @return the dimension
     */
    public CohortDefinitionDimension get106aAgeGenderGroup() {
        CohortDefinitionDimension ageGenderDimension = new CohortDefinitionDimension();

        CohortDefinition below2Years = cohortDefinitionLibrary.below2Years();
        CohortDefinition between2And4Years = cohortDefinitionLibrary.between2And5Years();
        CohortDefinition between5And14Years = cohortDefinitionLibrary.between5And14Years();
        CohortDefinition above15Years = cohortDefinitionLibrary.above15Years();

        CohortDefinition males = cohortDefinitionLibrary.males();
        CohortDefinition females = cohortDefinitionLibrary.females();


        CohortDefinition a = df.getPatientsInAll(below2Years, males);
        CohortDefinition b = df.getPatientsInAll(below2Years, females);
        CohortDefinition c = df.getPatientsInAll(between2And4Years, males);
        CohortDefinition d = df.getPatientsInAll(between2And4Years, females);

        CohortDefinition e = df.getPatientsInAll(between5And14Years, males);
        CohortDefinition f = df.getPatientsInAll(between5And14Years, females);
        CohortDefinition g = df.getPatientsInAll(above15Years, males);
        CohortDefinition h = df.getPatientsInAll(above15Years, females);

        ageGenderDimension.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ageGenderDimension.addCohortDefinition("below2male", Mapped.mapStraightThrough(a));
        ageGenderDimension.addCohortDefinition("below2female", Mapped.mapStraightThrough(b));
        ageGenderDimension.addCohortDefinition("between2and5male", Mapped.mapStraightThrough(c));
        ageGenderDimension.addCohortDefinition("between2and5female", Mapped.mapStraightThrough(d));
        ageGenderDimension.addCohortDefinition("between5and14male", Mapped.mapStraightThrough(e));
        ageGenderDimension.addCohortDefinition("between5and14female", Mapped.mapStraightThrough(f));
        ageGenderDimension.addCohortDefinition("above15male", Mapped.mapStraightThrough(g));
        ageGenderDimension.addCohortDefinition("above15female", Mapped.mapStraightThrough(h));
        ageGenderDimension.addCohortDefinition("child", Mapped.mapStraightThrough(cohortDefinitionLibrary.agedBetween(0, 14)));
        ageGenderDimension.addCohortDefinition("adult", Mapped.mapStraightThrough(cohortDefinitionLibrary.agedAtLeast(15)));
        return ageGenderDimension;
    }

    public CohortDefinitionDimension getFinerAgeDisaggregations() {
        CohortDefinitionDimension ageGenderDimension = new CohortDefinitionDimension();

        CohortDefinition below1Year = cohortDefinitionLibrary.below1Year();
        CohortDefinition between1And4Years = cohortDefinitionLibrary.between1And4years();
        CohortDefinition between5And9Years = cohortDefinitionLibrary.between5And9years();
        CohortDefinition between10And14Years = cohortDefinitionLibrary.between10And14years();
        CohortDefinition between15And19Years = cohortDefinitionLibrary.between15And19years();
        CohortDefinition between20And24Years = cohortDefinitionLibrary.between20And24years();
        CohortDefinition between25And29Years =cohortDefinitionLibrary.between25And29years();
        CohortDefinition between30And34Years =cohortDefinitionLibrary.between30And34years();
        CohortDefinition between35And39Years =cohortDefinitionLibrary.between35And39years();
        CohortDefinition between40And44Years =cohortDefinitionLibrary.between40And44years();
        CohortDefinition between45And49Years =cohortDefinitionLibrary.between45And49years();
        CohortDefinition above50Years = cohortDefinitionLibrary.above50Years();

        CohortDefinition males = cohortDefinitionLibrary.males();
        CohortDefinition females = cohortDefinitionLibrary.females();


        CohortDefinition a = df.getPatientsInAll(below1Year, males);
        CohortDefinition b = df.getPatientsInAll(below1Year, females);
        CohortDefinition c = df.getPatientsInAll(between1And4Years, males);
        CohortDefinition d = df.getPatientsInAll(between1And4Years, females);
        CohortDefinition e = df.getPatientsInAll(between5And9Years, males);
        CohortDefinition f = df.getPatientsInAll(between5And9Years, females);
        CohortDefinition g = df.getPatientsInAll(between10And14Years, males);
        CohortDefinition h = df.getPatientsInAll(between10And14Years, females);
        CohortDefinition i = df.getPatientsInAll(between15And19Years, males);
        CohortDefinition j = df.getPatientsInAll(between15And19Years, females);
        CohortDefinition k = df.getPatientsInAll(between20And24Years, males);
        CohortDefinition l = df.getPatientsInAll(between20And24Years, females);
        CohortDefinition m = df.getPatientsInAll(between25And29Years, males);
        CohortDefinition n = df.getPatientsInAll(between25And29Years, females);
        CohortDefinition o = df.getPatientsInAll(between30And34Years, males);
        CohortDefinition p = df.getPatientsInAll(between30And34Years, females);
        CohortDefinition q = df.getPatientsInAll(between35And39Years, males);
        CohortDefinition r = df.getPatientsInAll(between35And39Years, females);
        CohortDefinition s = df.getPatientsInAll(between40And44Years, males);
        CohortDefinition t = df.getPatientsInAll(between40And44Years, females);
        CohortDefinition w = df.getPatientsInAll(between45And49Years, males);
        CohortDefinition x = df.getPatientsInAll(between45And49Years, females);
        CohortDefinition u= df.getPatientsInAll(above50Years, males);
        CohortDefinition v = df.getPatientsInAll(above50Years, females);




        ageGenderDimension.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ageGenderDimension.addCohortDefinition("below1male", Mapped.mapStraightThrough(a));
        ageGenderDimension.addCohortDefinition("below1female", Mapped.mapStraightThrough(b));
        ageGenderDimension.addCohortDefinition("between1and4male", Mapped.mapStraightThrough(c));
        ageGenderDimension.addCohortDefinition("between1and4female", Mapped.mapStraightThrough(d));
        ageGenderDimension.addCohortDefinition("between5and9male", Mapped.mapStraightThrough(e));
        ageGenderDimension.addCohortDefinition("between5and9female", Mapped.mapStraightThrough(f));
        ageGenderDimension.addCohortDefinition("between10and14male", Mapped.mapStraightThrough(g));
        ageGenderDimension.addCohortDefinition("between10and14female", Mapped.mapStraightThrough(h));
        ageGenderDimension.addCohortDefinition("between15and19male", Mapped.mapStraightThrough(i));
        ageGenderDimension.addCohortDefinition("between15and19female", Mapped.mapStraightThrough(j));
        ageGenderDimension.addCohortDefinition("between20and24male", Mapped.mapStraightThrough(k));
        ageGenderDimension.addCohortDefinition("between20and24female", Mapped.mapStraightThrough(l));
        ageGenderDimension.addCohortDefinition("between25and29male", Mapped.mapStraightThrough(m));
        ageGenderDimension.addCohortDefinition("between25and29female", Mapped.mapStraightThrough(n));
        ageGenderDimension.addCohortDefinition("between30and34male", Mapped.mapStraightThrough(o));
        ageGenderDimension.addCohortDefinition("between30and34female", Mapped.mapStraightThrough(p));
        ageGenderDimension.addCohortDefinition("between35and39male", Mapped.mapStraightThrough(q));
        ageGenderDimension.addCohortDefinition("between35and39female", Mapped.mapStraightThrough(r));
        ageGenderDimension.addCohortDefinition("between40and44male", Mapped.mapStraightThrough(s));
        ageGenderDimension.addCohortDefinition("between40and44female", Mapped.mapStraightThrough(t));
        ageGenderDimension.addCohortDefinition("between45and49male", Mapped.mapStraightThrough(w));
        ageGenderDimension.addCohortDefinition("between45and49female", Mapped.mapStraightThrough(x));
        ageGenderDimension.addCohortDefinition("above50male", Mapped.mapStraightThrough(u));
        ageGenderDimension.addCohortDefinition("above50female", Mapped.mapStraightThrough(v));



        return ageGenderDimension;
    }

    public CohortDefinitionDimension getTxCurrentAgeGenderGroup() {
        CohortDefinitionDimension ageGenderDimension = new CohortDefinitionDimension();

        CohortDefinition below1Year = cohortDefinitionLibrary.below1Year();
        CohortDefinition between1And4Years = cohortDefinitionLibrary.between1And4years();
        CohortDefinition between5And9Years = cohortDefinitionLibrary.between5And9years();
        CohortDefinition between10And14Years = cohortDefinitionLibrary.between10And14years();
        CohortDefinition between15And19Years = cohortDefinitionLibrary.between15And19years();
        CohortDefinition between20And24Years = cohortDefinitionLibrary.between20And24years();
        CohortDefinition between25And29Years =cohortDefinitionLibrary.between25And29years();
        CohortDefinition between30And34Years =cohortDefinitionLibrary.between30And34years();
        CohortDefinition between35And39Years =cohortDefinitionLibrary.between35And39years();
        CohortDefinition between40And44Years =cohortDefinitionLibrary.between40And44years();
        CohortDefinition between45And49Years =cohortDefinitionLibrary.between45And49years();
        CohortDefinition above50Years = cohortDefinitionLibrary.above50Years();

        CohortDefinition males = cohortDefinitionLibrary.males();
        CohortDefinition females = cohortDefinitionLibrary.females();


        CohortDefinition a = df.getPatientsInAll(below1Year, males);
        CohortDefinition b = df.getPatientsInAll(below1Year, females);
        CohortDefinition c = df.getPatientsInAll(between1And4Years, males);
        CohortDefinition d = df.getPatientsInAll(between1And4Years, females);
        CohortDefinition e = df.getPatientsInAll(between5And9Years, males);
        CohortDefinition f = df.getPatientsInAll(between5And9Years, females);
        CohortDefinition g = df.getPatientsInAll(between10And14Years, males);
        CohortDefinition h = df.getPatientsInAll(between10And14Years, females);
        CohortDefinition i = df.getPatientsInAll(between15And19Years, males);
        CohortDefinition j = df.getPatientsInAll(between15And19Years, females);
        CohortDefinition k = df.getPatientsInAll(between20And24Years, males);
        CohortDefinition l = df.getPatientsInAll(between20And24Years, females);
        CohortDefinition m = df.getPatientsInAll(between25And29Years, males);
        CohortDefinition n = df.getPatientsInAll(between25And29Years, females);
        CohortDefinition o = df.getPatientsInAll(between30And34Years, males);
        CohortDefinition p = df.getPatientsInAll(between30And34Years, females);
        CohortDefinition q = df.getPatientsInAll(between35And39Years, males);
        CohortDefinition r = df.getPatientsInAll(between35And39Years, females);
        CohortDefinition s = df.getPatientsInAll(between40And44Years, males);
        CohortDefinition t = df.getPatientsInAll(between40And44Years, females);
        CohortDefinition w = df.getPatientsInAll(between45And49Years, males);
        CohortDefinition x = df.getPatientsInAll(between45And49Years, females);
        CohortDefinition u= df.getPatientsInAll(above50Years, males);
        CohortDefinition v = df.getPatientsInAll(above50Years, females);




        ageGenderDimension.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ageGenderDimension.addCohortDefinition("below1male", Mapped.mapStraightThrough(a));
        ageGenderDimension.addCohortDefinition("below1female", Mapped.mapStraightThrough(b));
        ageGenderDimension.addCohortDefinition("between1and4male", Mapped.mapStraightThrough(c));
        ageGenderDimension.addCohortDefinition("between1and4female", Mapped.mapStraightThrough(d));
        ageGenderDimension.addCohortDefinition("between5and9male", Mapped.mapStraightThrough(e));
        ageGenderDimension.addCohortDefinition("between5and9female", Mapped.mapStraightThrough(f));
        ageGenderDimension.addCohortDefinition("between10and14male", Mapped.mapStraightThrough(g));
        ageGenderDimension.addCohortDefinition("between10and14female", Mapped.mapStraightThrough(h));
        ageGenderDimension.addCohortDefinition("between15and19male", Mapped.mapStraightThrough(i));
        ageGenderDimension.addCohortDefinition("between15and19female", Mapped.mapStraightThrough(j));
        ageGenderDimension.addCohortDefinition("between20and24male", Mapped.mapStraightThrough(k));
        ageGenderDimension.addCohortDefinition("between20and24female", Mapped.mapStraightThrough(l));
        ageGenderDimension.addCohortDefinition("between25and29male", Mapped.mapStraightThrough(m));
        ageGenderDimension.addCohortDefinition("between25and29female", Mapped.mapStraightThrough(n));
        ageGenderDimension.addCohortDefinition("between30and34male", Mapped.mapStraightThrough(o));
        ageGenderDimension.addCohortDefinition("between30and34female", Mapped.mapStraightThrough(p));
        ageGenderDimension.addCohortDefinition("between35and39male", Mapped.mapStraightThrough(q));
        ageGenderDimension.addCohortDefinition("between35and39female", Mapped.mapStraightThrough(r));
        ageGenderDimension.addCohortDefinition("between40and44male", Mapped.mapStraightThrough(s));
        ageGenderDimension.addCohortDefinition("between40and44female", Mapped.mapStraightThrough(t));
        ageGenderDimension.addCohortDefinition("between45and49male", Mapped.mapStraightThrough(w));
        ageGenderDimension.addCohortDefinition("between45and49female", Mapped.mapStraightThrough(x));
        ageGenderDimension.addCohortDefinition("above50male", Mapped.mapStraightThrough(u));
        ageGenderDimension.addCohortDefinition("above50female", Mapped.mapStraightThrough(v));



          return ageGenderDimension;
    }

    public CohortDefinitionDimension getCBSAdultReportAgeGenderGroup() {
        CohortDefinitionDimension ageGenderDimension = new CohortDefinitionDimension();

        CohortDefinition below5Years = cohortDefinitionLibrary.below5Years();
        CohortDefinition between5And14Years = cohortDefinitionLibrary.between5And14Years();
        CohortDefinition above15Years = cohortDefinitionLibrary.above15Years();

        CohortDefinition males = cohortDefinitionLibrary.males();
        CohortDefinition females = cohortDefinitionLibrary.females();

        CohortDefinition a = df.getPatientsInAll(below5Years, males);
        CohortDefinition b = df.getPatientsInAll(below5Years, females);

        CohortDefinition e = df.getPatientsInAll(between5And14Years, males);
        CohortDefinition f = df.getPatientsInAll(between5And14Years, females);
        CohortDefinition g = df.getPatientsInAll(above15Years, males);
        CohortDefinition h = df.getPatientsInAll(above15Years, females);

        ageGenderDimension.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ageGenderDimension.addCohortDefinition("below5male", Mapped.mapStraightThrough(a));
        ageGenderDimension.addCohortDefinition("below5female", Mapped.mapStraightThrough(b));
        ageGenderDimension.addCohortDefinition("between5and14male", Mapped.mapStraightThrough(e));
        ageGenderDimension.addCohortDefinition("between5and14female", Mapped.mapStraightThrough(f));
        ageGenderDimension.addCohortDefinition("above15male", Mapped.mapStraightThrough(g));
        ageGenderDimension.addCohortDefinition("above15female", Mapped.mapStraightThrough(h));
        return ageGenderDimension;
    }

    public CohortDefinitionDimension getTxNewAgeGenderGroup() {
        CohortDefinitionDimension ageGenderDimension = new CohortDefinitionDimension();

        CohortDefinition below1Year = cohortDefinitionLibrary.below1Year();
        CohortDefinition between1And4Years = cohortDefinitionLibrary.between1And4years();
        CohortDefinition between5And9Years = cohortDefinitionLibrary.between5And9years();
        CohortDefinition between10And14Years = cohortDefinitionLibrary.between10And14years();
        CohortDefinition between15And19Years = cohortDefinitionLibrary.between15And19years();
        CohortDefinition between20And24Years = cohortDefinitionLibrary.between20And24years();
        CohortDefinition between25And29Years =cohortDefinitionLibrary.between25And29years();
        CohortDefinition between30And34Years =cohortDefinitionLibrary.between30And34years();
        CohortDefinition between35And39Years =cohortDefinitionLibrary.between35And39years();
        CohortDefinition between40And44Years =cohortDefinitionLibrary.between40And44years();
        CohortDefinition between45And49Years =cohortDefinitionLibrary.between45And49years();
        CohortDefinition above50Years = cohortDefinitionLibrary.above50Years();

        CohortDefinition males = cohortDefinitionLibrary.males();
        CohortDefinition females = cohortDefinitionLibrary.females();


        CohortDefinition a = df.getPatientsInAll(below1Year, males);
        CohortDefinition b = df.getPatientsInAll(below1Year, females);
        CohortDefinition c = df.getPatientsInAll(between1And4Years, males);
        CohortDefinition d = df.getPatientsInAll(between1And4Years, females);
        CohortDefinition e = df.getPatientsInAll(between5And9Years, males);
        CohortDefinition f = df.getPatientsInAll(between5And9Years, females);
        CohortDefinition g = df.getPatientsInAll(between10And14Years, males);
        CohortDefinition h = df.getPatientsInAll(between10And14Years, females);
        CohortDefinition i = df.getPatientsInAll(between15And19Years, males);
        CohortDefinition j = df.getPatientsInAll(between15And19Years, females);
        CohortDefinition k = df.getPatientsInAll(between20And24Years, males);
        CohortDefinition l = df.getPatientsInAll(between20And24Years, females);

        CohortDefinition ba= df.getPatientsInAll(between25And29Years, males);
        CohortDefinition bb = df.getPatientsInAll(between25And29Years, females);

        CohortDefinition bc= df.getPatientsInAll(between30And34Years, males);
        CohortDefinition bd = df.getPatientsInAll(between30And34Years, females);

        CohortDefinition be= df.getPatientsInAll(between35And39Years, males);
        CohortDefinition bf = df.getPatientsInAll(between35And39Years, females);

        CohortDefinition bg= df.getPatientsInAll(between40And44Years, males);
        CohortDefinition bh = df.getPatientsInAll(between40And44Years, females);

        CohortDefinition bi= df.getPatientsInAll(between45And49Years, males);
        CohortDefinition bj= df.getPatientsInAll(between45And49Years, females);

        CohortDefinition u= df.getPatientsInAll(above50Years, males);
        CohortDefinition v = df.getPatientsInAll(above50Years, females);




        ageGenderDimension.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ageGenderDimension.addCohortDefinition("below1male", Mapped.mapStraightThrough(a));
        ageGenderDimension.addCohortDefinition("below1female", Mapped.mapStraightThrough(b));
        ageGenderDimension.addCohortDefinition("between1and4male", Mapped.mapStraightThrough(c));
        ageGenderDimension.addCohortDefinition("between1and4female", Mapped.mapStraightThrough(d));
        ageGenderDimension.addCohortDefinition("between5and9male", Mapped.mapStraightThrough(e));
        ageGenderDimension.addCohortDefinition("between5and9female", Mapped.mapStraightThrough(f));
        ageGenderDimension.addCohortDefinition("between10and14male", Mapped.mapStraightThrough(g));
        ageGenderDimension.addCohortDefinition("between10and14female", Mapped.mapStraightThrough(h));
        ageGenderDimension.addCohortDefinition("between15and19male", Mapped.mapStraightThrough(i));
        ageGenderDimension.addCohortDefinition("between15and19female", Mapped.mapStraightThrough(j));
        ageGenderDimension.addCohortDefinition("between20and24male", Mapped.mapStraightThrough(k));
        ageGenderDimension.addCohortDefinition("between20and24female", Mapped.mapStraightThrough(l));
        ageGenderDimension.addCohortDefinition("above50male", Mapped.mapStraightThrough(u));
        ageGenderDimension.addCohortDefinition("above50female", Mapped.mapStraightThrough(v));

        ageGenderDimension.addCohortDefinition("between25and29male", Mapped.mapStraightThrough(ba));
        ageGenderDimension.addCohortDefinition("between25and29female", Mapped.mapStraightThrough(bb));
        ageGenderDimension.addCohortDefinition("between30and34male", Mapped.mapStraightThrough(bc));
        ageGenderDimension.addCohortDefinition("between30and34female", Mapped.mapStraightThrough(bd));
        ageGenderDimension.addCohortDefinition("between35and39male", Mapped.mapStraightThrough(be));
        ageGenderDimension.addCohortDefinition("between35and39female", Mapped.mapStraightThrough(bf));
        ageGenderDimension.addCohortDefinition("between40and44male", Mapped.mapStraightThrough(bg));
        ageGenderDimension.addCohortDefinition("between40and44female", Mapped.mapStraightThrough(bh));
        ageGenderDimension.addCohortDefinition("between45and49male", Mapped.mapStraightThrough(bi));
        ageGenderDimension.addCohortDefinition("between45and49female", Mapped.mapStraightThrough(bj));

        ageGenderDimension.addCohortDefinition("child", Mapped.mapStraightThrough(cohortDefinitionLibrary.agedBetween(0, 14)));
        ageGenderDimension.addCohortDefinition("adult", Mapped.mapStraightThrough(cohortDefinitionLibrary.agedAtLeast(15)));

        ageGenderDimension.addCohortDefinition("male",Mapped.mapStraightThrough(males));
        ageGenderDimension.addCohortDefinition("female",Mapped.mapStraightThrough(females));
        return ageGenderDimension;

    }

    public CohortDefinitionDimension getFinerAgeWith55And65Ranges() {
        CohortDefinitionDimension ageGenderDimension = new CohortDefinitionDimension();

        CohortDefinition below1Year = cohortDefinitionLibrary.below1Year();
        CohortDefinition between1And4Years = cohortDefinitionLibrary.between1And4years();
        CohortDefinition between5And9Years = cohortDefinitionLibrary.between5And9years();
        CohortDefinition between10And14Years = cohortDefinitionLibrary.between10And14years();
        CohortDefinition between15And19Years = cohortDefinitionLibrary.between15And19years();
        CohortDefinition between20And24Years = cohortDefinitionLibrary.between20And24years();
        CohortDefinition between25And29Years =cohortDefinitionLibrary.between25And29years();
        CohortDefinition between30And34Years =cohortDefinitionLibrary.between30And34years();
        CohortDefinition between35And39Years =cohortDefinitionLibrary.between35And39years();
        CohortDefinition between40And44Years =cohortDefinitionLibrary.between40And44years();
        CohortDefinition between45And49Years =cohortDefinitionLibrary.between45And49years();
        CohortDefinition between50And54years =cohortDefinitionLibrary.between50And54years();
        CohortDefinition between55And59years =cohortDefinitionLibrary.between55And59years();
        CohortDefinition between60And64years =cohortDefinitionLibrary.between60And64years();
        CohortDefinition above65Years = cohortDefinitionLibrary.agedAtLeast(65);

        CohortDefinition males = cohortDefinitionLibrary.males();
        CohortDefinition females = cohortDefinitionLibrary.females();


        CohortDefinition a = df.getPatientsInAll(below1Year, males);
        CohortDefinition b = df.getPatientsInAll(below1Year, females);
        CohortDefinition c = df.getPatientsInAll(between1And4Years, males);
        CohortDefinition d = df.getPatientsInAll(between1And4Years, females);
        CohortDefinition e = df.getPatientsInAll(between5And9Years, males);
        CohortDefinition f = df.getPatientsInAll(between5And9Years, females);
        CohortDefinition g = df.getPatientsInAll(between10And14Years, males);
        CohortDefinition h = df.getPatientsInAll(between10And14Years, females);
        CohortDefinition i = df.getPatientsInAll(between15And19Years, males);
        CohortDefinition j = df.getPatientsInAll(between15And19Years, females);
        CohortDefinition k = df.getPatientsInAll(between20And24Years, males);
        CohortDefinition l = df.getPatientsInAll(between20And24Years, females);

        CohortDefinition ba= df.getPatientsInAll(between25And29Years, males);
        CohortDefinition bb = df.getPatientsInAll(between25And29Years, females);

        CohortDefinition bc= df.getPatientsInAll(between30And34Years, males);
        CohortDefinition bd = df.getPatientsInAll(between30And34Years, females);

        CohortDefinition be= df.getPatientsInAll(between35And39Years, males);
        CohortDefinition bf = df.getPatientsInAll(between35And39Years, females);

        CohortDefinition bg= df.getPatientsInAll(between40And44Years, males);
        CohortDefinition bh = df.getPatientsInAll(between40And44Years, females);

        CohortDefinition bi= df.getPatientsInAll(between45And49Years, males);
        CohortDefinition bj= df.getPatientsInAll(between45And49Years, females);

        CohortDefinition bk= df.getPatientsInAll(between50And54years, males);
        CohortDefinition bl = df.getPatientsInAll(between50And54years, females);

        CohortDefinition bm= df.getPatientsInAll(between55And59years, males);
        CohortDefinition bn = df.getPatientsInAll(between55And59years, females);

        CohortDefinition bo= df.getPatientsInAll(between60And64years, males);
        CohortDefinition bp = df.getPatientsInAll(between60And64years, females);

        CohortDefinition bq= df.getPatientsInAll(above65Years, males);
        CohortDefinition br = df.getPatientsInAll(above65Years, females);




        ageGenderDimension.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ageGenderDimension.addCohortDefinition("below1male", Mapped.mapStraightThrough(a));
        ageGenderDimension.addCohortDefinition("below1female", Mapped.mapStraightThrough(b));
        ageGenderDimension.addCohortDefinition("between1and4male", Mapped.mapStraightThrough(c));
        ageGenderDimension.addCohortDefinition("between1and4female", Mapped.mapStraightThrough(d));
        ageGenderDimension.addCohortDefinition("between5and9male", Mapped.mapStraightThrough(e));
        ageGenderDimension.addCohortDefinition("between5and9female", Mapped.mapStraightThrough(f));
        ageGenderDimension.addCohortDefinition("between10and14male", Mapped.mapStraightThrough(g));
        ageGenderDimension.addCohortDefinition("between10and14female", Mapped.mapStraightThrough(h));
        ageGenderDimension.addCohortDefinition("between15and19male", Mapped.mapStraightThrough(i));
        ageGenderDimension.addCohortDefinition("between15and19female", Mapped.mapStraightThrough(j));
        ageGenderDimension.addCohortDefinition("between20and24male", Mapped.mapStraightThrough(k));
        ageGenderDimension.addCohortDefinition("between20and24female", Mapped.mapStraightThrough(l));

        ageGenderDimension.addCohortDefinition("between25and29male", Mapped.mapStraightThrough(ba));
        ageGenderDimension.addCohortDefinition("between25and29female", Mapped.mapStraightThrough(bb));
        ageGenderDimension.addCohortDefinition("between30and34male", Mapped.mapStraightThrough(bc));
        ageGenderDimension.addCohortDefinition("between30and34female", Mapped.mapStraightThrough(bd));
        ageGenderDimension.addCohortDefinition("between35and39male", Mapped.mapStraightThrough(be));
        ageGenderDimension.addCohortDefinition("between35and39female", Mapped.mapStraightThrough(bf));
        ageGenderDimension.addCohortDefinition("between40and44male", Mapped.mapStraightThrough(bg));
        ageGenderDimension.addCohortDefinition("between40and44female", Mapped.mapStraightThrough(bh));
        ageGenderDimension.addCohortDefinition("between45and49male", Mapped.mapStraightThrough(bi));
        ageGenderDimension.addCohortDefinition("between45and49female", Mapped.mapStraightThrough(bj));
        ageGenderDimension.addCohortDefinition("between50and54male", Mapped.mapStraightThrough(bk));
        ageGenderDimension.addCohortDefinition("between50and54female", Mapped.mapStraightThrough(bl));
        ageGenderDimension.addCohortDefinition("between55and59male", Mapped.mapStraightThrough(bm));
        ageGenderDimension.addCohortDefinition("between55and59female", Mapped.mapStraightThrough(bn));
        ageGenderDimension.addCohortDefinition("between60and64male", Mapped.mapStraightThrough(bo));
        ageGenderDimension.addCohortDefinition("between60and64female", Mapped.mapStraightThrough(bp));
        ageGenderDimension.addCohortDefinition("above65male", Mapped.mapStraightThrough(bq));
        ageGenderDimension.addCohortDefinition("above65female", Mapped.mapStraightThrough(br));

        ageGenderDimension.addCohortDefinition("child", Mapped.mapStraightThrough(cohortDefinitionLibrary.agedBetween(0, 14)));
        ageGenderDimension.addCohortDefinition("adult", Mapped.mapStraightThrough(cohortDefinitionLibrary.agedAtLeast(15)));

        ageGenderDimension.addCohortDefinition("male",Mapped.mapStraightThrough(males));
        ageGenderDimension.addCohortDefinition("female",Mapped.mapStraightThrough(females));
        return ageGenderDimension;

    }

    public CohortDefinitionDimension getProgramsDimensionGroup() {
        CohortDefinitionDimension programDimension = new CohortDefinitionDimension();

        CohortDefinition below2Years = cohortDefinitionLibrary.below2Years();
        CohortDefinition between2And4Years = cohortDefinitionLibrary.between2And5Years();
        CohortDefinition between5And14Years = cohortDefinitionLibrary.between5And14Years();
        CohortDefinition above15Years = cohortDefinitionLibrary.above15Years();

        CohortDefinition males = cohortDefinitionLibrary.males();
        CohortDefinition females = cohortDefinitionLibrary.females();

        CohortDefinition below2Male = df.getPatientsInAll(below2Years, males);
        CohortDefinition below2Female = df.getPatientsInAll(below2Years, females);
        CohortDefinition between2And4male = df.getPatientsInAll(between2And4Years, males);
        CohortDefinition between2And4Female = df.getPatientsInAll(between2And4Years, females);

        CohortDefinition between5And14Male = df.getPatientsInAll(between5And14Years, males);
        CohortDefinition between5And14Female = df.getPatientsInAll(between5And14Years, females);
        CohortDefinition above15Male = df.getPatientsInAll(above15Years, males);
        CohortDefinition above15Female = df.getPatientsInAll(above15Years, females);

//        getting cohort definitions for programs
        CohortDefinition fbim_patients = commonCohortLibrary.getPatientsInProgramDuringPeriod(getProgramByUuid("de5d54ae-c304-11e8-9ad0-529269fb1459"));
        CohortDefinition ftr_patients = commonCohortLibrary.getPatientsInProgramDuringPeriod(getProgramByUuid("de5d5896-c304-11e8-9ad0-529269fb1459"));
        CohortDefinition fbg_patients = commonCohortLibrary.getPatientsInProgramDuringPeriod(getProgramByUuid("de5d5b34-c304-11e8-9ad0-529269fb1459"));
        CohortDefinition cddp_patients = commonCohortLibrary.getPatientsInProgramDuringPeriod(getProgramByUuid("de5d6034-c304-11e8-9ad0-529269fb1459"));
        CohortDefinition cclad_patients = commonCohortLibrary.getPatientsInProgramDuringPeriod(getProgramByUuid("de5d5da0-c304-11e8-9ad0-529269fb1459"));

        programDimension.addParameter(ReportingConstants.START_DATE_PARAMETER);
        programDimension.addParameter(ReportingConstants.END_DATE_PARAMETER);

        //cohort defintion combiming programs to ageAndGenderCombined
        //FBIM
        CohortDefinition fbim_Below2Male = df.getPatientsInAll(fbim_patients,below2Male);
        CohortDefinition fbim_Below2Female = df.getPatientsInAll(fbim_patients,below2Female);
        CohortDefinition fbim_between2And4Male = df.getPatientsInAll(fbim_patients,between2And4male);
        CohortDefinition fbim_between2And4Female = df.getPatientsInAll(fbim_patients,between2And4Female);
        CohortDefinition fbim_between5And14Male = df.getPatientsInAll(fbim_patients,between5And14Male);
        CohortDefinition fbim_between5And14Female = df.getPatientsInAll(fbim_patients,between5And14Female);
        CohortDefinition fbim_above15Male = df.getPatientsInAll(fbim_patients,above15Male);
        CohortDefinition fbim_above15Female = df.getPatientsInAll(fbim_patients,above15Female);

        //FBG
        CohortDefinition fbg_Below2Male = df.getPatientsInAll(fbg_patients,below2Male);
        CohortDefinition fbg_Below2Female = df.getPatientsInAll(fbg_patients,below2Female);
        CohortDefinition fbg_between2And4Male = df.getPatientsInAll(fbg_patients,between2And4male);
        CohortDefinition fbg_between2And4Female = df.getPatientsInAll(fbg_patients,between2And4Female);
        CohortDefinition fbg_between5And14Male = df.getPatientsInAll(fbg_patients,between5And14Male);
        CohortDefinition fbg_between5And14Female = df.getPatientsInAll(fbg_patients,between5And14Female);
        CohortDefinition fbg_above15Male = df.getPatientsInAll(fbg_patients,above15Male);
        CohortDefinition fbg_above15Female = df.getPatientsInAll(fbg_patients,above15Female);

        //FTR
        CohortDefinition ftr_Below2Male = df.getPatientsInAll(ftr_patients,below2Male);
        CohortDefinition ftr_Below2Female = df.getPatientsInAll(ftr_patients,below2Female);
        CohortDefinition ftr_between2And4Male = df.getPatientsInAll(ftr_patients,between2And4male);
        CohortDefinition ftr_between2And4Female = df.getPatientsInAll(ftr_patients,between2And4Female);
        CohortDefinition ftr_between5And14Male = df.getPatientsInAll(ftr_patients,between5And14Male);
        CohortDefinition ftr_between5And14Female = df.getPatientsInAll(ftr_patients,between5And14Female);
        CohortDefinition ftr_above15Male = df.getPatientsInAll(ftr_patients,above15Male);
        CohortDefinition ftr_above15Female = df.getPatientsInAll(ftr_patients,above15Female);

        //CDDP
        CohortDefinition cddp_Below2Male = df.getPatientsInAll(cddp_patients,below2Male);
        CohortDefinition cddp_Below2Female = df.getPatientsInAll(cddp_patients,below2Female);
        CohortDefinition cddp_between2And4Male = df.getPatientsInAll(cddp_patients,between2And4male);
        CohortDefinition cddp_between2And4Female = df.getPatientsInAll(cddp_patients,between2And4Female);
        CohortDefinition cddp_between5And14Male = df.getPatientsInAll(cddp_patients,between5And14Male);
        CohortDefinition cddp_between5And14Female = df.getPatientsInAll(cddp_patients,between5And14Female);
        CohortDefinition cddp_above15Male = df.getPatientsInAll(cddp_patients,above15Male);
        CohortDefinition cddp_above15Female = df.getPatientsInAll(cddp_patients,above15Female);

        //cclad
        CohortDefinition cclad_Below2Male = df.getPatientsInAll(cclad_patients,below2Male);
        CohortDefinition cclad_Below2Female = df.getPatientsInAll(cclad_patients,below2Female);
        CohortDefinition cclad_between2And4Male = df.getPatientsInAll(cclad_patients,between2And4male);
        CohortDefinition cclad_between2And4Female = df.getPatientsInAll(cclad_patients,between2And4Female);
        CohortDefinition cclad_between5And14Male = df.getPatientsInAll(cclad_patients,between5And14Male);
        CohortDefinition cclad_between5And14Female = df.getPatientsInAll(cclad_patients,between5And14Female);
        CohortDefinition cclad_above15Male = df.getPatientsInAll(cclad_patients,above15Male);
        CohortDefinition cclad_above15Female = df.getPatientsInAll(cclad_patients,above15Female);


        programDimension.addCohortDefinition("fbimbelow2Male", Mapped.mapStraightThrough(fbim_Below2Male));
        programDimension.addCohortDefinition("fbimbelow2Female", Mapped.mapStraightThrough(fbim_Below2Female));
        programDimension.addCohortDefinition("fbimbetween2And4Male", Mapped.mapStraightThrough(fbim_between2And4Male));
        programDimension.addCohortDefinition("fbimbetween2And4Female", Mapped.mapStraightThrough(fbim_between2And4Female));
        programDimension.addCohortDefinition("fbimbetween5And14Male", Mapped.mapStraightThrough(fbim_between5And14Male));
        programDimension.addCohortDefinition("fbimbetween5And14Female", Mapped.mapStraightThrough(fbim_between5And14Female));
        programDimension.addCohortDefinition("fbimabove15Male", Mapped.mapStraightThrough(fbim_above15Male));
        programDimension.addCohortDefinition("fbimabove15Female", Mapped.mapStraightThrough(fbim_above15Female));

        programDimension.addCohortDefinition("fbgbelow2Male", Mapped.mapStraightThrough(fbg_Below2Male));
        programDimension.addCohortDefinition("fbgbelow2Female", Mapped.mapStraightThrough(fbg_Below2Female));
        programDimension.addCohortDefinition("fbgbetween2And4Male", Mapped.mapStraightThrough(fbg_between2And4Male));
        programDimension.addCohortDefinition("fbgbetween2And4Female", Mapped.mapStraightThrough(fbg_between2And4Female));
        programDimension.addCohortDefinition("fbgbetween5And14Male", Mapped.mapStraightThrough(fbg_between5And14Male));
        programDimension.addCohortDefinition("fbgbetween5And14Female", Mapped.mapStraightThrough(fbg_between5And14Female));
        programDimension.addCohortDefinition("fbgabove15Male", Mapped.mapStraightThrough(fbg_above15Male));
        programDimension.addCohortDefinition("fbgabove15Female", Mapped.mapStraightThrough(fbg_above15Female));

        programDimension.addCohortDefinition("ftrbelow2Male", Mapped.mapStraightThrough(ftr_Below2Male));
        programDimension.addCohortDefinition("ftrbelow2Female", Mapped.mapStraightThrough(ftr_Below2Female));
        programDimension.addCohortDefinition("ftrbetween2And4Male", Mapped.mapStraightThrough(ftr_between2And4Male));
        programDimension.addCohortDefinition("ftrbetween2And4Female", Mapped.mapStraightThrough(ftr_between2And4Female));
        programDimension.addCohortDefinition("ftrbetween5And14Male", Mapped.mapStraightThrough(ftr_between5And14Male));
        programDimension.addCohortDefinition("ftrbetween5And14Female", Mapped.mapStraightThrough(ftr_between5And14Female));
        programDimension.addCohortDefinition("ftrabove15Male", Mapped.mapStraightThrough(ftr_above15Male));
        programDimension.addCohortDefinition("ftrabove15Female", Mapped.mapStraightThrough(ftr_above15Female));

        programDimension.addCohortDefinition("cddpbelow2Male", Mapped.mapStraightThrough(cddp_Below2Male));
        programDimension.addCohortDefinition("cddpbelow2Female", Mapped.mapStraightThrough(cddp_Below2Female));
        programDimension.addCohortDefinition("cddpbetween2And4Male", Mapped.mapStraightThrough(cddp_between2And4Male));
        programDimension.addCohortDefinition("cddpbetween2And4Female", Mapped.mapStraightThrough(cddp_between2And4Female));
        programDimension.addCohortDefinition("cddpbetween5And14Male", Mapped.mapStraightThrough(cddp_between5And14Male));
        programDimension.addCohortDefinition("cddpbetween5And14Female", Mapped.mapStraightThrough(cddp_between5And14Female));
        programDimension.addCohortDefinition("cddpabove15Male", Mapped.mapStraightThrough(cddp_above15Male));
        programDimension.addCohortDefinition("cddpabove15Female", Mapped.mapStraightThrough(cddp_above15Female));

        programDimension.addCohortDefinition("ccladbelow2Male", Mapped.mapStraightThrough(cclad_Below2Male));
        programDimension.addCohortDefinition("ccladbelow2Female", Mapped.mapStraightThrough(cclad_Below2Female));
        programDimension.addCohortDefinition("ccladbetween2And4Male", Mapped.mapStraightThrough(cclad_between2And4Male));
        programDimension.addCohortDefinition("ccladbetween2And4Female", Mapped.mapStraightThrough(cclad_between2And4Female));
        programDimension.addCohortDefinition("ccladbetween5And14Male", Mapped.mapStraightThrough(cclad_between5And14Male));
        programDimension.addCohortDefinition("ccladbetween5And14Female", Mapped.mapStraightThrough(cclad_between5And14Female));
        programDimension.addCohortDefinition("ccladabove15Male", Mapped.mapStraightThrough(cclad_above15Male));
        programDimension.addCohortDefinition("ccladabove15Female", Mapped.mapStraightThrough(cclad_above15Female));


        return programDimension;
    }

    public Program getProgramByUuid(String uuid){
        Program program=Context.getProgramWorkflowService().getProgramByUuid(uuid);
        return  program;
    }

    public ProgramWorkflow getProgramWorkFlow(String uuid){
        ProgramWorkflow program=Context.getProgramWorkflowService().getWorkflowByUuid(uuid);
        return  program;
    }

    public ProgramWorkflowState getProgramState(String uuid){
        ProgramWorkflowState program=Context.getProgramWorkflowService().getStateByUuid(uuid);
        return  program;
    }

    public Location getLocationByUuid(String uuid){
        Location location=Context.getLocationService().getLocationByUuid(uuid);
        return  location;
    }

    public CohortDefinitionDimension getAdherenceGroup() {
        CohortDefinitionDimension adherenceDimension = new CohortDefinitionDimension();

        CohortDefinition good = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getAdherence(), Arrays.asList(hivMetadata.getARTEncounterEncounterType()), Arrays.asList(hivMetadata.getGoodAdherence()), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition fair = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getAdherence(), Arrays.asList(hivMetadata.getARTEncounterEncounterType()), Arrays.asList(hivMetadata.getFairAdherence()), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition poor = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getAdherence(), Arrays.asList(hivMetadata.getARTEncounterEncounterType()), Arrays.asList(hivMetadata.getPoorAdherence()), BaseObsCohortDefinition.TimeModifier.ANY);

        adherenceDimension.addParameter(ReportingConstants.END_DATE_PARAMETER);
        adherenceDimension.addCohortDefinition("good", Mapped.mapStraightThrough(good));
        adherenceDimension.addCohortDefinition("fair", Mapped.mapStraightThrough(fair));
        adherenceDimension.addCohortDefinition("poor", Mapped.mapStraightThrough(poor));
        return adherenceDimension;
    }


    public CohortDefinitionDimension get106bEMTCTGroup() {
        CohortDefinitionDimension eMTCTDimension = new CohortDefinitionDimension();

        CohortDefinition pregnant = hivCohortDefinitionLibrary.getPregnantPatientsAtArtStart();
        CohortDefinition lactating = hivCohortDefinitionLibrary.getLactatingPatientsAtArtStart();
        CohortDefinition pregnantOrLactating = df.getPatientsInAny(pregnant, lactating);

        eMTCTDimension.addCohortDefinition("pregnant", Mapped.mapStraightThrough(pregnantOrLactating));

        return eMTCTDimension;
    }

    public CohortDefinitionDimension getTxTBAgeGenderGroup() {
        CohortDefinitionDimension ageGenderDimension = new CohortDefinitionDimension();

        CohortDefinition below15Years = cohortDefinitionLibrary.agedAtMost(14);
        CohortDefinition atleast15Years = cohortDefinitionLibrary.above15Years();


        CohortDefinition males = cohortDefinitionLibrary.males();
        CohortDefinition females = cohortDefinitionLibrary.females();


        CohortDefinition a = df.getPatientsInAll(below15Years, males);
        CohortDefinition b = df.getPatientsInAll(below15Years, females);
        CohortDefinition c = df.getPatientsInAll(atleast15Years, males);
        CohortDefinition d = df.getPatientsInAll(atleast15Years, females);



        ageGenderDimension.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ageGenderDimension.addCohortDefinition("below15male", Mapped.mapStraightThrough(a));
        ageGenderDimension.addCohortDefinition("below15female", Mapped.mapStraightThrough(b));
        ageGenderDimension.addCohortDefinition("above15+male", Mapped.mapStraightThrough(c));
        ageGenderDimension.addCohortDefinition("above15+female", Mapped.mapStraightThrough(d));

        ageGenderDimension.addCohortDefinition("child", Mapped.mapStraightThrough(cohortDefinitionLibrary.agedBetween(0, 14)));
        ageGenderDimension.addCohortDefinition("adult", Mapped.mapStraightThrough(cohortDefinitionLibrary.agedAtLeast(15)));
        return ageGenderDimension;
    }


    public CohortDefinitionDimension getTB_STATAndTB_ARTAgeGenderGroup(){
        CohortDefinitionDimension ageGenderDimension = new CohortDefinitionDimension();

        CohortDefinition below1Year = cohortDefinitionLibrary.below1Year();
        CohortDefinition between1And4Years = cohortDefinitionLibrary.between1And4years();
        CohortDefinition between5And9Years = cohortDefinitionLibrary.between5And9years();
        CohortDefinition between10And14Years = cohortDefinitionLibrary.between10And14years();
        CohortDefinition between15And19Years = cohortDefinitionLibrary.between15And19years();
        CohortDefinition between20And24Years = cohortDefinitionLibrary.between20And24years();
        CohortDefinition between25And29Years =cohortDefinitionLibrary.between25And29years();
        CohortDefinition between30And34Years = cohortDefinitionLibrary.between30And34years();
        CohortDefinition between35And39Years = cohortDefinitionLibrary.between35And39years();
        CohortDefinition between40And44Years = cohortDefinitionLibrary.agedBetween(40,44);;
        CohortDefinition between45And49Years = cohortDefinitionLibrary.agedBetween(45,49);;
        CohortDefinition above50Years = cohortDefinitionLibrary.above50Years();


        CohortDefinition males = cohortDefinitionLibrary.males();
        CohortDefinition females = cohortDefinitionLibrary.females();


        CohortDefinition a = df.getPatientsInAll(below1Year, males);
        CohortDefinition b = df.getPatientsInAll(below1Year, females);
        CohortDefinition c = df.getPatientsInAll(between1And4Years, males);
        CohortDefinition d = df.getPatientsInAll(between1And4Years, females);
        CohortDefinition e = df.getPatientsInAll(between5And9Years, males);
        CohortDefinition f = df.getPatientsInAll(between5And9Years, females);
        CohortDefinition g = df.getPatientsInAll(between10And14Years, males);
        CohortDefinition h = df.getPatientsInAll(between10And14Years, females);
        CohortDefinition i = df.getPatientsInAll(between15And19Years, males);
        CohortDefinition j = df.getPatientsInAll(between15And19Years, females);
        CohortDefinition k = df.getPatientsInAll(between20And24Years, males);
        CohortDefinition l = df.getPatientsInAll(between20And24Years, females);
        CohortDefinition m = df.getPatientsInAll(between25And29Years, males);
        CohortDefinition n = df.getPatientsInAll(between25And29Years, females);
        CohortDefinition o = df.getPatientsInAll(between30And34Years, males);
        CohortDefinition p = df.getPatientsInAll(between30And34Years, females);
        CohortDefinition q = df.getPatientsInAll(between35And39Years, males);
        CohortDefinition r = df.getPatientsInAll(between35And39Years, females);
        CohortDefinition s = df.getPatientsInAll(between40And44Years, males);
        CohortDefinition t = df.getPatientsInAll(between40And44Years, females);
        CohortDefinition u = df.getPatientsInAll(between45And49Years, males);
        CohortDefinition v = df.getPatientsInAll(between45And49Years, females);
        CohortDefinition w= df.getPatientsInAll(above50Years, males);
        CohortDefinition x = df.getPatientsInAll(above50Years, females);

        ageGenderDimension.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ageGenderDimension.addCohortDefinition("below1male", Mapped.mapStraightThrough(a));
        ageGenderDimension.addCohortDefinition("below1female", Mapped.mapStraightThrough(b));
        ageGenderDimension.addCohortDefinition("between1and4male", Mapped.mapStraightThrough(c));
        ageGenderDimension.addCohortDefinition("between1and4female", Mapped.mapStraightThrough(d));
        ageGenderDimension.addCohortDefinition("between5and9male", Mapped.mapStraightThrough(e));
        ageGenderDimension.addCohortDefinition("between5and9female", Mapped.mapStraightThrough(f));
        ageGenderDimension.addCohortDefinition("between10and14male", Mapped.mapStraightThrough(g));
        ageGenderDimension.addCohortDefinition("between10and14female", Mapped.mapStraightThrough(h));
        ageGenderDimension.addCohortDefinition("between15and19male", Mapped.mapStraightThrough(i));
        ageGenderDimension.addCohortDefinition("between15and19female", Mapped.mapStraightThrough(j));
        ageGenderDimension.addCohortDefinition("between20and24male", Mapped.mapStraightThrough(k));
        ageGenderDimension.addCohortDefinition("between20and24female", Mapped.mapStraightThrough(l));
        ageGenderDimension.addCohortDefinition("between25and29male", Mapped.mapStraightThrough(m));
        ageGenderDimension.addCohortDefinition("between25and29female", Mapped.mapStraightThrough(n));
        ageGenderDimension.addCohortDefinition("between30and34male", Mapped.mapStraightThrough(o));
        ageGenderDimension.addCohortDefinition("between30and34female", Mapped.mapStraightThrough(p));
        ageGenderDimension.addCohortDefinition("between35and39male", Mapped.mapStraightThrough(q));
        ageGenderDimension.addCohortDefinition("between35and39female", Mapped.mapStraightThrough(r));
        ageGenderDimension.addCohortDefinition("between40and44male", Mapped.mapStraightThrough(s));
        ageGenderDimension.addCohortDefinition("between40and44female", Mapped.mapStraightThrough(t));
        ageGenderDimension.addCohortDefinition("between45and49male", Mapped.mapStraightThrough(u));
        ageGenderDimension.addCohortDefinition("between45and49female", Mapped.mapStraightThrough(v));
        ageGenderDimension.addCohortDefinition("above50male", Mapped.mapStraightThrough(w));
        ageGenderDimension.addCohortDefinition("above50female", Mapped.mapStraightThrough(x));

        return ageGenderDimension;

    }

    public CohortDefinitionDimension getPMTCT_STAT_AgeGenderGroup() {
        CohortDefinitionDimension ageGenderDimension = new CohortDefinitionDimension();

        CohortDefinition below10Years = cohortDefinitionLibrary.below10Years();
        CohortDefinition between10And14Years = cohortDefinitionLibrary.between10And14years();
        CohortDefinition between15And19Years = cohortDefinitionLibrary.between15And19years();
        CohortDefinition between20And24Years = cohortDefinitionLibrary.between20And24years();
        CohortDefinition between25And29Years = cohortDefinitionLibrary.between25And29years();
        CohortDefinition between30And34Years = cohortDefinitionLibrary.between30And34years();
        CohortDefinition between35And39Years =cohortDefinitionLibrary.between35And39years();
        CohortDefinition between40And44Years =cohortDefinitionLibrary.between40And44years();
        CohortDefinition between45And49Years =cohortDefinitionLibrary.between45And49years();
        CohortDefinition above50Years = cohortDefinitionLibrary.above50Years();



        CohortDefinition females = cohortDefinitionLibrary.females();


        CohortDefinition b = df.getPatientsInAll(below10Years, females);
        CohortDefinition d = df.getPatientsInAll(between10And14Years, females);
        CohortDefinition f = df.getPatientsInAll(between15And19Years, females);
        CohortDefinition h = df.getPatientsInAll(between20And24Years, females);
        CohortDefinition j = df.getPatientsInAll(between25And29Years, females);
        CohortDefinition l = df.getPatientsInAll(between30And34Years, females);
        CohortDefinition n = df.getPatientsInAll(between35And39Years, females);
        CohortDefinition p = df.getPatientsInAll(between40And44Years, females);
        CohortDefinition r = df.getPatientsInAll(between45And49Years, females);
        CohortDefinition v = df.getPatientsInAll(above50Years, females);



        ageGenderDimension.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ageGenderDimension.addCohortDefinition("below10female", Mapped.mapStraightThrough(b));
        ageGenderDimension.addCohortDefinition("between10and14female", Mapped.mapStraightThrough(d));
        ageGenderDimension.addCohortDefinition("between15and19female", Mapped.mapStraightThrough(f));
        ageGenderDimension.addCohortDefinition("between20and24female", Mapped.mapStraightThrough(h));
        ageGenderDimension.addCohortDefinition("between25and29female", Mapped.mapStraightThrough(j));
        ageGenderDimension.addCohortDefinition("between30and34female", Mapped.mapStraightThrough(l));
        ageGenderDimension.addCohortDefinition("between35and39female", Mapped.mapStraightThrough(n));
        ageGenderDimension.addCohortDefinition("between40and44female",Mapped.mapStraightThrough(p));
        ageGenderDimension.addCohortDefinition("between45and49female",Mapped.mapStraightThrough(r));
        ageGenderDimension.addCohortDefinition("above50female", Mapped.mapStraightThrough(v));


        ageGenderDimension.addCohortDefinition("female",Mapped.mapStraightThrough(females));
        return ageGenderDimension;

    }

    public CohortDefinitionDimension getPatientTypeDimension(){
        CohortDefinitionDimension patientTypeDimension= new CohortDefinitionDimension();

        CohortDefinition newPatients = tbCohortDefinitionLibrary.getNewPatientsDuringPeriod();
        CohortDefinition relapsedPatients = tbCohortDefinitionLibrary.getRelapsedPatientsDuringPeriod();
        CohortDefinition treatedAfterLTFP = tbCohortDefinitionLibrary.getTreatedAfterLTFPPatientsDuringPeriod();
        CohortDefinition treatedAfterFailure = tbCohortDefinitionLibrary.getTreatedAfterFailurePatientsDuringPeriod();
        CohortDefinition treatementHistoryUnknown = tbCohortDefinitionLibrary.getTreatmentHistoryUnknownPatientsDuringPeriod();
        CohortDefinition referredFromCommunity = tbCohortDefinitionLibrary.getPatientsReferredFromComunity();
        patientTypeDimension.addParameter(ReportingConstants.START_DATE_PARAMETER);
        patientTypeDimension.addParameter(ReportingConstants.END_DATE_PARAMETER);

        patientTypeDimension.addCohortDefinition("newPatients", Mapped.mapStraightThrough(newPatients));
        patientTypeDimension.addCohortDefinition("relapsedPatients", Mapped.mapStraightThrough(relapsedPatients));
        patientTypeDimension.addCohortDefinition("treatedAfterLTFP", Mapped.mapStraightThrough(treatedAfterLTFP));
        patientTypeDimension.addCohortDefinition("treatedAfterFailure", Mapped.mapStraightThrough(treatedAfterFailure));
        patientTypeDimension.addCohortDefinition("treatementHistoryUnknown", Mapped.mapStraightThrough(treatementHistoryUnknown));
        patientTypeDimension.addCohortDefinition("referred", Mapped.mapStraightThrough(referredFromCommunity));

        return patientTypeDimension;
    }

    public CohortDefinitionDimension getCovidAgeDimension() {
        CohortDefinitionDimension ageGenderDimension = new CohortDefinitionDimension();

        CohortDefinition below1Year = cohortDefinitionLibrary.below1Year();

        CohortDefinition between1And4Years = cohortDefinitionLibrary.between1And4years();
        CohortDefinition between5And9Years = cohortDefinitionLibrary.between5And9years();

        CohortDefinition between10And11Years = cohortDefinitionLibrary.between10And11years();
        CohortDefinition between12And14Years = cohortDefinitionLibrary.between12And14years();

        CohortDefinition between15And17Years = cohortDefinitionLibrary.between15And17years();
        CohortDefinition between18And19Years = cohortDefinitionLibrary.between18And19years();

        CohortDefinition between20And24Years = cohortDefinitionLibrary.between20And24years();
        CohortDefinition between25And29Years =cohortDefinitionLibrary.between25And29years();

        CohortDefinition between30And34Years =cohortDefinitionLibrary.between30And34years();
        CohortDefinition between35And39Years =cohortDefinitionLibrary.between35And39years();

        CohortDefinition between40And44Years =cohortDefinitionLibrary.between40And44years();
        CohortDefinition between45And49Years =cohortDefinitionLibrary.between45And49years();

        CohortDefinition between50And54years =cohortDefinitionLibrary.between50And54years();
        CohortDefinition between55And59years =cohortDefinitionLibrary.between55And59years();

        CohortDefinition above60Years = cohortDefinitionLibrary.agedAtLeast(60);

        CohortDefinition males = cohortDefinitionLibrary.males();
        CohortDefinition females = cohortDefinitionLibrary.females();


        CohortDefinition malesBelow1Year = df.getPatientsInAll(below1Year, males);
        CohortDefinition femalesBelow1Year = df.getPatientsInAll(below1Year, females);

        CohortDefinition malesBetween1And4Years = df.getPatientsInAll(between1And4Years, males);
        CohortDefinition femalesBetween1And4Years = df.getPatientsInAll(between1And4Years, females);

        CohortDefinition malesBetween5And9Years = df.getPatientsInAll(between5And9Years, males);
        CohortDefinition femalesBetween5And9Years = df.getPatientsInAll(between5And9Years, females);

        CohortDefinition malesBetween10And11Years = df.getPatientsInAll(between10And11Years, males);
        CohortDefinition femalesBetween10And11Years = df.getPatientsInAll(between10And11Years, females);

        CohortDefinition malesBetween12And14Years = df.getPatientsInAll(between12And14Years, males);
        CohortDefinition femalesBetween12And14Years = df.getPatientsInAll(between12And14Years, females);

        CohortDefinition malesBetween15And17Years = df.getPatientsInAll(between15And17Years, males);
        CohortDefinition femalesBetween15And17Years = df.getPatientsInAll(between15And17Years, females);

        CohortDefinition malesBetween18And19Years = df.getPatientsInAll(between18And19Years, males);
        CohortDefinition femalesBetween18And19Years = df.getPatientsInAll(between18And19Years, females);

        CohortDefinition malesBetween20And24Years = df.getPatientsInAll(between20And24Years, males);
        CohortDefinition femalesBetween20And24Years = df.getPatientsInAll(between20And24Years, females);

        CohortDefinition malesBetween25And29Years = df.getPatientsInAll(between25And29Years, males);
        CohortDefinition femalesBetween25And29Years = df.getPatientsInAll(between25And29Years, females);

        CohortDefinition malesBetween30And34Years = df.getPatientsInAll(between30And34Years, males);
        CohortDefinition femalesBetween30And34Years = df.getPatientsInAll(between30And34Years, females);

        CohortDefinition malesBetween35And39Years = df.getPatientsInAll(between35And39Years, males);
        CohortDefinition femalesBetween35And39Years = df.getPatientsInAll(between35And39Years, females);

        CohortDefinition malesBetween40And44Years = df.getPatientsInAll(between40And44Years, males);
        CohortDefinition femalesBetween40And44Years = df.getPatientsInAll(between40And44Years, females);

        CohortDefinition malesBetween45And49Years = df.getPatientsInAll(between45And49Years, males);
        CohortDefinition femalesBetween45And49Years = df.getPatientsInAll(between45And49Years, females);

        CohortDefinition malesBetween50And54years = df.getPatientsInAll(between50And54years, males);
        CohortDefinition femalesBetween50And54years = df.getPatientsInAll(between50And54years, females);

        CohortDefinition malesBetween55And59years = df.getPatientsInAll(between55And59years, males);
        CohortDefinition femalesBetween55And59years = df.getPatientsInAll(between55And59years, females);

        CohortDefinition malesAbove60Years = df.getPatientsInAll(above60Years, males);
        CohortDefinition femalesAbove60Years = df.getPatientsInAll(above60Years, females);


        ageGenderDimension.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ageGenderDimension.addCohortDefinition("below1male", Mapped.mapStraightThrough(malesBelow1Year));
        ageGenderDimension.addCohortDefinition("below1female", Mapped.mapStraightThrough(femalesBelow1Year));
        ageGenderDimension.addCohortDefinition("between1and4male", Mapped.mapStraightThrough(malesBetween1And4Years));
        ageGenderDimension.addCohortDefinition("between1and4female", Mapped.mapStraightThrough(femalesBetween1And4Years));
        ageGenderDimension.addCohortDefinition("between5and9male", Mapped.mapStraightThrough(malesBetween5And9Years));
        ageGenderDimension.addCohortDefinition("between5and9female", Mapped.mapStraightThrough(femalesBetween5And9Years));
        ageGenderDimension.addCohortDefinition("between10and11male", Mapped.mapStraightThrough(malesBetween10And11Years));
        ageGenderDimension.addCohortDefinition("between10and11female", Mapped.mapStraightThrough(femalesBetween10And11Years));
        ageGenderDimension.addCohortDefinition("between12and14male", Mapped.mapStraightThrough(malesBetween12And14Years));
        ageGenderDimension.addCohortDefinition("between12and14female", Mapped.mapStraightThrough(femalesBetween12And14Years));
        ageGenderDimension.addCohortDefinition("between15and17male", Mapped.mapStraightThrough(malesBetween15And17Years));
        ageGenderDimension.addCohortDefinition("between15and17female", Mapped.mapStraightThrough(femalesBetween15And17Years));
        ageGenderDimension.addCohortDefinition("between18and19male", Mapped.mapStraightThrough(malesBetween18And19Years));
        ageGenderDimension.addCohortDefinition("between18and19female", Mapped.mapStraightThrough(femalesBetween18And19Years));
        ageGenderDimension.addCohortDefinition("between20and24male", Mapped.mapStraightThrough(malesBetween20And24Years));
        ageGenderDimension.addCohortDefinition("between20and24female", Mapped.mapStraightThrough(femalesBetween20And24Years));

        ageGenderDimension.addCohortDefinition("between25and29male", Mapped.mapStraightThrough(malesBetween25And29Years));
        ageGenderDimension.addCohortDefinition("between25and29female", Mapped.mapStraightThrough(femalesBetween25And29Years));
        ageGenderDimension.addCohortDefinition("between30and34male", Mapped.mapStraightThrough(malesBetween30And34Years));
        ageGenderDimension.addCohortDefinition("between30and34female", Mapped.mapStraightThrough(femalesBetween30And34Years));
        ageGenderDimension.addCohortDefinition("between35and39male", Mapped.mapStraightThrough(malesBetween35And39Years));
        ageGenderDimension.addCohortDefinition("between35and39female", Mapped.mapStraightThrough(femalesBetween35And39Years));
        ageGenderDimension.addCohortDefinition("between40and44male", Mapped.mapStraightThrough(malesBetween40And44Years));
        ageGenderDimension.addCohortDefinition("between40and44female", Mapped.mapStraightThrough(femalesBetween40And44Years));
        ageGenderDimension.addCohortDefinition("between45and49male", Mapped.mapStraightThrough(malesBetween45And49Years));
        ageGenderDimension.addCohortDefinition("between45and49female", Mapped.mapStraightThrough(femalesBetween45And49Years));
        ageGenderDimension.addCohortDefinition("between50and54male", Mapped.mapStraightThrough(malesBetween50And54years));
        ageGenderDimension.addCohortDefinition("between50and54female", Mapped.mapStraightThrough(femalesBetween50And54years));
        ageGenderDimension.addCohortDefinition("between55and59male", Mapped.mapStraightThrough(malesBetween55And59years));
        ageGenderDimension.addCohortDefinition("between55and59female", Mapped.mapStraightThrough(femalesBetween55And59years));
        ageGenderDimension.addCohortDefinition("above60male", Mapped.mapStraightThrough(malesAbove60Years));
        ageGenderDimension.addCohortDefinition("above60female", Mapped.mapStraightThrough(femalesAbove60Years));

        return ageGenderDimension;
    }
}
