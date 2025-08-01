/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2020 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data management.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.isf.examination.manager;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.isf.examination.model.PatientExamination;
import org.isf.examination.service.ExaminationOperations;
import org.isf.generaldata.ExaminationParameters;
import org.isf.generaldata.MessageBundle;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.isf.utils.exception.model.OHSeverityLevel;
import org.isf.utils.validator.DefaultSorter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExaminationBrowserManager {

	@Autowired
	private ExaminationOperations ioOperations;

	protected HashMap<String, String> diuresisDescriptionHashMap;
	protected HashMap<String, String> bowelDescriptionHashMap;
	protected LinkedHashMap<String, String> auscultationHashMap;

	/**
	 * Default PatientExamination
	 */
	public PatientExamination getDefaultPatientExamination(Patient patient) {
		PatientExamination defaultPatient = new PatientExamination(new GregorianCalendar(), patient,
				ExaminationParameters.HEIGHT_INIT, (double) ExaminationParameters.WEIGHT_INIT,
				ExaminationParameters.AP_MIN_INIT, ExaminationParameters.AP_MAX_INIT, ExaminationParameters.HR_INIT,
				(double) ExaminationParameters.TEMP_INIT, (double) ExaminationParameters.SAT_INIT,
				ExaminationParameters.HGT_INIT, ExaminationParameters.DIURESIS_INIT,
				ExaminationParameters.DIURESIS_DESC_INIT, ExaminationParameters.BOWEL_DESC_INIT,
				ExaminationParameters.RR_INIT, ExaminationParameters.AUSCULTATION_INIT, "");
		return defaultPatient;
	}

	/**
	 * Get from last PatientExamination (only height, weight & note)
	 */
	public PatientExamination getFromLastPatientExamination(PatientExamination lastPatientExamination) {
		PatientExamination newPatientExamination = new PatientExamination(new GregorianCalendar(),
				lastPatientExamination.getPatient(), lastPatientExamination.getPex_height(),
				lastPatientExamination.getPex_weight(), lastPatientExamination.getPex_ap_min(),
				lastPatientExamination.getPex_ap_max(), lastPatientExamination.getPex_hr(),
				lastPatientExamination.getPex_temp(), lastPatientExamination.getPex_sat(),
				lastPatientExamination.getPex_hgt(), lastPatientExamination.getPex_diuresis(),
				lastPatientExamination.getPex_diuresis_desc(), lastPatientExamination.getPex_bowel_desc(),
				lastPatientExamination.getPex_rr(), lastPatientExamination.getPex_auscultation(),
				lastPatientExamination.getPex_note());
		return newPatientExamination;
	}

	private void buildAuscultationHashMap() {
		auscultationHashMap = new LinkedHashMap<>();
		auscultationHashMap.put("normal", MessageBundle.getMessage("angal.examination.auscultation.normal.txt"));
		auscultationHashMap.put("wheezes", MessageBundle.getMessage("angal.examination.auscultation.wheezes.txt"));
		auscultationHashMap.put("rhonchi", MessageBundle.getMessage("angal.examination.auscultation.rhonchi.txt"));
		auscultationHashMap.put("crackles", MessageBundle.getMessage("angal.examination.auscultation.crackles.txt"));
		auscultationHashMap.put("stridor", MessageBundle.getMessage("angal.examination.auscultation.stridor.txt"));
		auscultationHashMap.put("bronchial", MessageBundle.getMessage("angal.examination.auscultation.bronchial.txt"));
	}

	public ArrayList<String> getAuscultationList() {
		if (auscultationHashMap == null)
			buildAuscultationHashMap();
		ArrayList<String> auscultationDescriptionList = new ArrayList<>(auscultationHashMap.values());
		auscultationDescriptionList
				.sort(new DefaultSorter(MessageBundle.getMessage("angal.examination.auscultation.normal.txt")));
		return auscultationDescriptionList;
	}

	public String getAuscultationTranslated(String auscultationKey) {
		if (auscultationHashMap == null)
			buildAuscultationHashMap();
		return auscultationHashMap.get(auscultationKey);
	}

	public String getAuscultationKey(String description) {
		if (auscultationHashMap == null) {
			buildAuscultationHashMap();
		}
		for (String key : auscultationHashMap.keySet()) {
			if (auscultationHashMap.get(key).equals(description)) {
				return key;
			}
		}
		return "";
	}

	/**
	 * @param patex - the PatientExamination to save
	 * @throws OHServiceException
	 */
	public void saveOrUpdate(PatientExamination patex) throws OHServiceException {
		List<OHExceptionMessage> errors = validateExamination(patex);
		if (!errors.isEmpty()) {
			throw new OHServiceException(errors);
		}
		ioOperations.saveOrUpdate(patex);
	}

	public PatientExamination getByID(int id) throws OHServiceException {
		return ioOperations.getByID(id);
	}

	public PatientExamination getLastByPatID(int patID) throws OHServiceException {
		ArrayList<PatientExamination> patExamination = getByPatID(patID);

		return !patExamination.isEmpty() ? patExamination.get(0) : null;
	}

	public ArrayList<PatientExamination> getLastNByPatID(int patID, int number) throws OHServiceException {
		return ioOperations.getLastNByPatID(patID, number);
	}

	public ArrayList<PatientExamination> getByPatID(int patID) throws OHServiceException {
		return ioOperations.getByPatID(patID);
	}

	/**
	 * @param patexList - the {@link PatientExamination} to delete.
	 * @throws OHServiceException
	 */
	public void remove(ArrayList<PatientExamination> patexList) throws OHServiceException {
		ioOperations.remove(patexList);
	}

	public String getBMIdescription(double bmi) {
		if (bmi < 16.5)
			return MessageBundle.getMessage("angal.examination.bmi.severeunderweight.txt");
		if (bmi >= 16.5 && bmi < 18.5)
			return MessageBundle.getMessage("angal.examination.bmi.underweight.txt");
		if (bmi >= 18.5 && bmi < 24.5)
			return MessageBundle.getMessage("angal.examination.bmi.normalweight.txt");
		if (bmi >= 24.5 && bmi < 30)
			return MessageBundle.getMessage("angal.examination.bmi.overweight.txt");
		if (bmi >= 30 && bmi < 35)
			return MessageBundle.getMessage("angal.examination.bmi.obesityclassilight.txt");
		if (bmi >= 35 && bmi < 40)
			return MessageBundle.getMessage("angal.examination.bmi.obesityclassiimedium.txt");
		return MessageBundle.getMessage("angal.examination.bmi.obesityclassiiisevere.txt");
	}

	private void buildDiuresisDescriptionHashMap() {
		diuresisDescriptionHashMap = new HashMap<>();
		diuresisDescriptionHashMap.put("physiological",
				MessageBundle.getMessage("angal.examination.diuresis.physiological.txt"));
		diuresisDescriptionHashMap.put("oliguria", MessageBundle.getMessage("angal.examination.diuresis.oliguria.txt"));
		diuresisDescriptionHashMap.put("anuria", MessageBundle.getMessage("angal.examination.diuresis.anuria.txt"));
		diuresisDescriptionHashMap.put("frequent", MessageBundle.getMessage("angal.examination.diuresis.frequent.txt"));
		diuresisDescriptionHashMap.put("nocturia", MessageBundle.getMessage("angal.examination.diuresis.nocturia.txt"));
		diuresisDescriptionHashMap.put("stranguria",
				MessageBundle.getMessage("angal.examination.diuresis.stranguria.txt"));
		diuresisDescriptionHashMap.put("hematuria",
				MessageBundle.getMessage("angal.examination.diuresis.hematuria.txt"));
		diuresisDescriptionHashMap.put("pyuria", MessageBundle.getMessage("angal.examination.diuresis.pyuria.txt"));
	}

	private void buildBowelDescriptionHashMap() {
		bowelDescriptionHashMap = new HashMap<>();
		bowelDescriptionHashMap.put("regular", MessageBundle.getMessage("angal.examination.bowel.regular.txt"));
		bowelDescriptionHashMap.put("irregular", MessageBundle.getMessage("angal.examination.bowel.irregular.txt"));
		bowelDescriptionHashMap.put("constipation",
				MessageBundle.getMessage("angal.examination.bowel.constipation.txt"));
		bowelDescriptionHashMap.put("diarrheal", MessageBundle.getMessage("angal.examination.bowel.diarrheal.txt"));
	}

	/**
	 * Return a list of diuresis descriptions:
	 * physiological,
	 * oliguria,
	 * anuria,
	 * fequent,
	 * nocturia,
	 * stranguria,
	 * hematuria,
	 * pyuria
	 *
	 * @return
	 */
	public ArrayList<String> getDiuresisDescriptionList() {
		if (diuresisDescriptionHashMap == null)
			buildDiuresisDescriptionHashMap();
		ArrayList<String> diuresisDescriptionList = new ArrayList<>(diuresisDescriptionHashMap.values());
		diuresisDescriptionList
				.sort(new DefaultSorter(MessageBundle.getMessage("angal.examination.diuresis.physiological.txt")));
		return diuresisDescriptionList;
	}

	/**
	 * Return a list of bowel descriptions:
	 * regular,
	 * irregular,
	 * constipation,
	 * diarrheal
	 *
	 * @return
	 */
	public ArrayList<String> getBowelDescriptionList() {
		if (bowelDescriptionHashMap == null)
			buildBowelDescriptionHashMap();
		ArrayList<String> bowelDescriptionList = new ArrayList<>(bowelDescriptionHashMap.values());
		bowelDescriptionList.sort(new DefaultSorter(MessageBundle.getMessage("angal.examination.bowel.regular.txt")));
		return bowelDescriptionList;
	}

	public String getBowelDescriptionTranslated(String pex_bowel_desc_key) {
		if (bowelDescriptionHashMap == null)
			buildBowelDescriptionHashMap();
		return bowelDescriptionHashMap.get(pex_bowel_desc_key);
	}

	public String getBowelDescriptionKey(String description) {
		if (bowelDescriptionHashMap == null) {
			buildBowelDescriptionHashMap();
		}
		for (String key : bowelDescriptionHashMap.keySet()) {
			if (bowelDescriptionHashMap.get(key).equals(description)) {
				return key;
			}
		}
		return "";
	}

	public String getDiuresisDescriptionTranslated(String pex_diuresis_desc_key) {
		if (diuresisDescriptionHashMap == null)
			buildDiuresisDescriptionHashMap();
		return diuresisDescriptionHashMap.get(pex_diuresis_desc_key);
	}

	public String getDiuresisDescriptionKey(String description) {
		if (diuresisDescriptionHashMap == null) {
			buildDiuresisDescriptionHashMap();
		}
		for (String key : diuresisDescriptionHashMap.keySet()) {
			if (diuresisDescriptionHashMap.get(key).equals(description)) {
				return key;
			}
		}
		return "";
	}

	/**
	 * Verify if the object is valid for CRUD and return a list of errors, if any
	 *
	 * @param patex
	 * @return list of {@link OHExceptionMessage}
	 */
	protected List<OHExceptionMessage> validateExamination(PatientExamination patex) {
		List<OHExceptionMessage> errors = new ArrayList<>();
		if (patex.getPex_diuresis_desc() != null
				&& !diuresisDescriptionHashMap.containsKey(patex.getPex_diuresis_desc()))
			errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
					MessageBundle.getMessage("angal.examination.pleaseinsertavaliddiuresisdescription.msg"),
					OHSeverityLevel.ERROR));
		if (patex.getPex_bowel_desc() != null && !bowelDescriptionHashMap.containsKey(patex.getPex_bowel_desc()))
			errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
					MessageBundle.getMessage("angal.examination.pleaseinsertavalidboweldescription.msg"),
					OHSeverityLevel.ERROR));
		if (patex.getPex_auscultation() != null && !auscultationHashMap.containsKey(patex.getPex_auscultation()))
			errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
					MessageBundle.getMessage("angal.examination.pleaseinsertavalidauscultationdescription.msg"),
					OHSeverityLevel.ERROR));
		return errors;
	}
}
