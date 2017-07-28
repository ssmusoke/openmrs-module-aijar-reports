/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ugandaemrreports.fragment.controller;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.ugandaemrreports.library.UgandaEMRReporting;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.ui.framework.page.PageModel;

import java.util.Date;


/**
 *  * Controller for a fragment that shows all users  
 */
public class DenormalizeFragmentController {

    public void controller(UiSessionContext sessionContext, FragmentModel model) {
    }

    public void get(@SpringBean PageModel pageModel) throws Exception {
        String lastDate = UgandaEMRReporting.getGlobalProperty("ugandaemrreports.lastDenormalizationDate");
        if (StringUtils.isBlank(lastDate)) {
            lastDate = "1900-01-01 00:00:00";
        }
        UgandaEMRReporting.normalizeObs(lastDate, UgandaEMRReporting.sqlConnection(), 100000);
        Date now = new Date();
        String newDate = UgandaEMRReporting.DEFAULT_DATE_FORMAT.format(now);
        UgandaEMRReporting.setGlobalProperty("ugandaemrreports.lastDenormalizationDate", newDate);
    }
}
