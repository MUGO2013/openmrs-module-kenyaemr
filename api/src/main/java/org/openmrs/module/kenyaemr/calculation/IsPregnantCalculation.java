/**
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

package org.openmrs.module.kenyaemr.calculation;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.kenyaemr.MetadataConstants;

import java.util.Collection;
import java.util.Map;

/**
 * Calculates the recorded pregnancy status of patients. Calculation returns null for patients with no recorded status
 */
public class IsPregnantCalculation extends BaseKenyaEmrCalculation {

	@Override
	public String getShortMessage() {
		return "Patients Who Are Pregnant";
	}

	@Override
	public String getSinglePatientMessage() {
		return "Pregnant";
	}

	@Override
	public String[] getTags() {
		return new String[] { "alert" };
	}

    /**
     * @see org.openmrs.calculation.patient.PatientCalculation#evaluate(java.util.Collection, java.util.Map, org.openmrs.calculation.patient.PatientCalculationContext)
     * @should calculate last recorded WHO stage for all patients
     */
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> parameterValues, PatientCalculationContext context) {
		Concept yes = Context.getConceptService().getConceptByUuid(MetadataConstants.YES_CONCEPT_UUID);
		CalculationResultMap pregStatusObss = lastObs(MetadataConstants.PREGNANCY_STATUS_CONCEPT_UUID, cohort, context);
		CalculationResultMap ret = new CalculationResultMap();

		for (Integer ptId : cohort) {

			Obs pregStatusObs = CalculationUtils.obsResultForPatient(pregStatusObss, ptId);

			if (pregStatusObs != null) {
				ret.put(ptId, new BooleanResult(pregStatusObs.getValueCoded().equals(yes), this));
			}
			else {
				ret.put(ptId, null);
			}
		}

		return ret;
    }
}