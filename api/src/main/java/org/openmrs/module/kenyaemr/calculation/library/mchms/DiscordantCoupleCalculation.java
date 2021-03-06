/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemr.calculation.library.mchms;

import org.openmrs.Concept;
import org.openmrs.Program;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.kenyacore.calculation.AbstractPatientCalculation;
import org.openmrs.module.kenyacore.calculation.BooleanResult;
import org.openmrs.module.kenyacore.calculation.Calculations;
import org.openmrs.module.kenyacore.calculation.Filters;
import org.openmrs.module.kenyaemr.Dictionary;
import org.openmrs.module.kenyaemr.calculation.EmrCalculationUtils;
import org.openmrs.module.kenyaemr.metadata.MchMetadata;
import org.openmrs.module.metadatadeploy.MetadataUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Determines if a given patient's HIV status is discordant with her male partner's. Discordance means one is positive
 * while the other is negative.
 */
public class DiscordantCoupleCalculation extends AbstractPatientCalculation {

	@Override
	public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> params, PatientCalculationContext context) {

		Program mchmsProgram = MetadataUtils.existing(Program.class, MchMetadata._Program.MCHMS);

		// Get all patients who are alive and in MCH-MS program
		Set<Integer> alive = Filters.alive(cohort, context);
		Set<Integer> inMchmsProgram = Filters.inProgram(mchmsProgram, alive, context);

		Concept patientHivStatusConcept = Dictionary.getConcept(Dictionary.HIV_STATUS);
		Concept partnerHivStatusConcept = Dictionary.getConcept(Dictionary.PARTNER_HIV_STATUS);

		CalculationResultMap lastPatientHivStatusObss = Calculations.lastObs(patientHivStatusConcept, inMchmsProgram, context);
		CalculationResultMap lastPartnerHivTestDateObss = Calculations.lastObs(partnerHivStatusConcept, inMchmsProgram, context);

		CalculationResultMap resultMap = new CalculationResultMap();

		for (Integer ptId : cohort) {
			Concept patientLastHivStatus = EmrCalculationUtils.codedObsResultForPatient(lastPatientHivStatusObss, ptId);
			Concept partnerLastHivStatus = EmrCalculationUtils.codedObsResultForPatient(lastPartnerHivTestDateObss, ptId);

			boolean qualified = false;
			if (patientLastHivStatus != null && partnerLastHivStatus != null) {
				Concept positive = Dictionary.getConcept(Dictionary.POSITIVE);
				Concept negative = Dictionary.getConcept(Dictionary.NEGATIVE);
				if ((patientLastHivStatus.equals(positive) || patientLastHivStatus.equals(negative))
						&& (partnerLastHivStatus.equals(positive) || partnerLastHivStatus.equals(negative))) {
					qualified = !patientLastHivStatus.equals(partnerLastHivStatus);
				}
			}
			resultMap.put(ptId, new BooleanResult(qualified, this, context));
		}
		return resultMap;
	}
}
