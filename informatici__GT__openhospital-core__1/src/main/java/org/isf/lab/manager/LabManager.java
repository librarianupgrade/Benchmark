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
package org.isf.lab.manager;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.lab.model.Laboratory;
import org.isf.lab.model.LaboratoryForPrint;
import org.isf.lab.model.LaboratoryRow;
import org.isf.lab.service.LabIoOperations;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHDataValidationException;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.isf.utils.exception.model.OHSeverityLevel;
import org.isf.utils.validator.DefaultSorter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ------------------------------------------
 * LabManager - laboratory exam manager class
 * -----------------------------------------
 * modification history
 * 10/11/2006 - ross - added editing capability
 * ------------------------------------------
 */
@Component
public class LabManager {

	@Autowired
	private LabIoOperations ioOperations;

	protected HashMap<String, String> materialHashMap;

	protected void setPatientConsistency(Laboratory laboratory) {
		if (GeneralData.LABEXTENDED && laboratory.getPatient() != null) {
			/*
			 * Age and Sex has not to be updated for reporting purposes
			 */
			laboratory.setPatName(laboratory.getPatient().getName());
			laboratory.setAge(laboratory.getPatient().getAge());
			laboratory.setSex(String.valueOf(laboratory.getPatient().getSex()));
		}
	}

	/**
	 * Verify if the object is valid for CRUD and return a list of errors, if any
	 *
	 * @param laboratory
	 * @throws OHDataValidationException
	 */
	protected void validateLaboratory(Laboratory laboratory) throws OHDataValidationException {
		List<OHExceptionMessage> errors = new ArrayList<>();
		if (laboratory.getDate() == null)
			laboratory.setDate(new GregorianCalendar());
		if (laboratory.getExam() != null && laboratory.getExam().getProcedure() == 2) {
			laboratory.setResult(MessageBundle.getMessage("angal.lab.multipleresults.txt"));
		}

		// Check Exam Date
		if (laboratory.getExamDate() == null) {
			errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
					MessageBundle.getMessage("angal.lab.pleaseinsertavalidexamdate.msg"), OHSeverityLevel.ERROR));
		}
		// Check Patient
		if (GeneralData.LABEXTENDED && laboratory.getPatient() == null) {
			errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
					MessageBundle.getMessage("angal.common.pleaseselectapatient.msg"), OHSeverityLevel.ERROR));
		} else if (laboratory.getPatient() == null) {
			String sex = laboratory.getSex().toUpperCase();
			if (!(sex.equals("M") || sex.equals("F"))) {
				errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
						MessageBundle.getMessage("angal.lab.pleaseinsertmformaleorfforfemale.msg"),
						OHSeverityLevel.ERROR));
			}
			if (laboratory.getAge() < 0) {
				errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
						MessageBundle.getMessage("angal.lab.insertvalidage.msg"), OHSeverityLevel.ERROR));
			}
		}
		if (laboratory.getExam() == null) {
			errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
					MessageBundle.getMessage("angal.lab.pleaseselectanexam.msg"), OHSeverityLevel.ERROR));
		}
		if (laboratory.getResult().isEmpty()) {
			errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
					MessageBundle.getMessage("angal.labnew.someexamswithoutresultpleasecheck.msg"),
					OHSeverityLevel.ERROR));
		}
		if (laboratory.getMaterial().isEmpty()) {
			errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
					MessageBundle.getMessage("angal.lab.pleaseselectamaterial.msg"), OHSeverityLevel.ERROR));
		}
		if (laboratory.getExamDate() == null) {
			errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
					MessageBundle.getMessage("angal.lab.pleaseinsertanexamdate.msg"), OHSeverityLevel.ERROR));
		}
		if (laboratory.getInOutPatient().isEmpty()) {
			errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
					MessageBundle.getMessage("angal.lab.pleaseinsertiforipdoroforopd.msg"), OHSeverityLevel.ERROR));
		}
		if (!errors.isEmpty()) {
			throw new OHDataValidationException(errors);
		}
	}

	/**
	 * Return the whole list of exams ({@link Laboratory}s) within last year.
	 *
	 * @return the list of {@link Laboratory}s. It could be <code>empty</code>.
	 * @throws OHServiceException
	 */
	public ArrayList<Laboratory> getLaboratory() throws OHServiceException {
		return ioOperations.getLaboratory();
	}

	/**
	 * Return a list of exams ({@link Laboratory}s) related to a {@link Patient}.
	 *
	 * @param aPatient - the {@link Patient}.
	 * @return the list of {@link Laboratory}s related to the {@link Patient}. It could be <code>empty</code>.
	 * @throws OHServiceException
	 */
	public ArrayList<Laboratory> getLaboratory(Patient aPatient) throws OHServiceException {
		return ioOperations.getLaboratory(aPatient);
	}

	/*
	 * NO LONGER USED
	 *
	 * public ArrayList<Laboratory> getLaboratory(String aCode){ return
	 * ioOperations.getLaboratory(); }
	 */

	/**
	 * Return a list of exams ({@link Laboratory}s) between specified dates and matching passed exam name
	 *
	 * @param exam - the exam name as <code>String</code>
	 * @param dateFrom - the lower date for the range
	 * @param dateTo - the highest date for the range
	 * @return the list of {@link Laboratory}s. It could be <code>empty</code>.
	 * @throws OHServiceException
	 */
	public ArrayList<Laboratory> getLaboratory(String exam, GregorianCalendar dateFrom, GregorianCalendar dateTo)
			throws OHServiceException {
		return ioOperations.getLaboratory(exam, dateFrom, dateTo);
	}

	/**
	 * Return a list of exams suitable for printing ({@link LaboratoryForPrint}s)
	 * between specified dates and matching passed exam name. If a lab has multiple
	 * results, these are concatenated and added to the result string
	 *
	 * @param exam - the exam name as <code>String</code>
	 * @param dateFrom - the lower date for the range
	 * @param dateTo - the highest date for the range
	 * @return the list of {@link LaboratoryForPrint}s . It could be <code>empty</code>.
	 * @throws OHServiceException
	 */
	public ArrayList<LaboratoryForPrint> getLaboratoryForPrint(String exam, GregorianCalendar dateFrom,
			GregorianCalendar dateTo) throws OHServiceException {
		ArrayList<LaboratoryForPrint> labs = ioOperations.getLaboratoryForPrint(exam, dateFrom, dateTo);
		setLabMultipleResults(labs);

		return labs;
	}

	/**
	 * Inserts one Laboratory exam {@link Laboratory} (All Procedures)
	 *
	 * @param laboratory - the laboratory with its result (Procedure 1)
	 * @param labRow - the list of results (Procedure 2) - it can be <code>null</code>
	 * @return <code>true</code> if the exam has been inserted, <code>false</code> otherwise
	 * @throws OHServiceException
	 */
	public boolean newLaboratory(Laboratory laboratory, ArrayList<String> labRow) throws OHServiceException {
		validateLaboratory(laboratory);
		setPatientConsistency(laboratory);
		if (laboratory.getExam().getProcedure() == 1) {
			return ioOperations.newLabFirstProcedure(laboratory);
		} else if (laboratory.getExam().getProcedure() == 2) {
			if (labRow == null || labRow.isEmpty())
				throw new OHDataValidationException(
						new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
								MessageBundle.getMessage("angal.labnew.someexamswithoutresultpleasecheck.msg"),
								OHSeverityLevel.ERROR));
			return ioOperations.newLabSecondProcedure(laboratory, labRow);
		} else {
			throw new OHDataValidationException(
					new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
							MessageBundle.getMessage("angal.lab.unknownprocedure.msg"), OHSeverityLevel.ERROR));
		}
	}

	/**
	 * Inserts one Laboratory exam {@link Laboratory} (All Procedures)
	 *
	 * @param laboratory - the laboratory with its result (Procedure 1)
	 * @param labRow - the list of results (Procedure 2) - it can be <code>null</code>
	 * @return <code>true</code> if the exam has been inserted, <code>false</code> otherwise
	 * @throws OHServiceException
	 */
	public boolean newLaboratory2(Laboratory laboratory, ArrayList<LaboratoryRow> labRow) throws OHServiceException {
		validateLaboratory(laboratory);
		setPatientConsistency(laboratory);
		if (laboratory.getExam().getProcedure() == 1) {
			return ioOperations.newLabFirstProcedure(laboratory);
		} else if (laboratory.getExam().getProcedure() == 2) {
			if (labRow == null || labRow.isEmpty())
				throw new OHDataValidationException(
						new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
								MessageBundle.getMessage("angal.labnew.someexamswithoutresultpleasecheck.msg"),
								OHSeverityLevel.ERROR));
			return ioOperations.newLabSecondProcedure2(laboratory, labRow);
		} else if (laboratory.getExam().getProcedure() == 3) {
			return ioOperations.newLabFirstProcedure(laboratory);
		} else
			throw new OHDataValidationException(
					new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
							MessageBundle.getMessage("angal.lab.unknownprocedure.msg"), OHSeverityLevel.ERROR));
	}

	/**
	 * Inserts one Laboratory exam {@link Laboratory} (All Procedures)
	 *
	 * @param laboratory - the laboratory with its result (Procedure 1)
	 * @param labRow - the list of results (Procedure 2) - it can be <code>null</code>
	 * @return <code>true</code> if the exam has been inserted, <code>false</code> otherwise
	 * @throws OHServiceException
	 */
	public boolean updateLaboratory(Laboratory laboratory, ArrayList<String> labRow) throws OHServiceException {
		validateLaboratory(laboratory);
		if (laboratory.getExam().getProcedure() == 1) {
			return ioOperations.updateLabFirstProcedure(laboratory);
		} else if (laboratory.getExam().getProcedure() == 2) {
			if (labRow == null || labRow.isEmpty())
				throw new OHDataValidationException(
						new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
								MessageBundle.getMessage("angal.labnew.someexamswithoutresultpleasecheck.msg"),
								OHSeverityLevel.ERROR));
			return ioOperations.updateLabSecondProcedure(laboratory, labRow);
		} else if (laboratory.getExam().getProcedure() == 3) {
			//TODO: is it enough to call FirstProcedure?
			return ioOperations.updateLabFirstProcedure(laboratory);
		} else
			throw new OHDataValidationException(
					new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
							MessageBundle.getMessage("angal.lab.unknownprocedure.msg"), OHSeverityLevel.ERROR));
	}

	/**
	 * Inserts list of Laboratory exams {@link Laboratory} (All Procedures)
	 *
	 * @param labList - the laboratory list with results
	 * @param labRowList - the list of results, it can be <code>null</code>
	 * @return <code>true</code> if the exam has been inserted, <code>false</code> otherwise
	 * @throws OHServiceException
	 */
	@Transactional(rollbackFor = OHServiceException.class)
	public boolean newLaboratory(List<Laboratory> labList, ArrayList<ArrayList<String>> labRowList)
			throws OHServiceException {
		if (labList.isEmpty())
			throw new OHDataValidationException(
					new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
							MessageBundle.getMessage("angal.labnew.noexamsinserted.msg"), OHSeverityLevel.ERROR));
		if (labList.size() != labRowList.size())
			throw new OHDataValidationException(
					new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
							MessageBundle.getMessage("angal.labnew.someexamswithoutresultpleasecheck.msg"),
							OHSeverityLevel.ERROR));
		boolean result = true;
		for (int i = 0; i < labList.size(); i++) {
			result = result && newLaboratory(labList.get(i), labRowList.get(i));
		}
		return result;
	}

	/**
	 * Inserts list of Laboratory exams {@link Laboratory} (All Procedures)
	 *
	 * @param labList - the laboratory list with results
	 * @param labRowList - the list of results, it can be <code>null</code>
	 * @return <code>true</code> if the exam has been inserted, <code>false</code> otherwise
	 * @throws OHServiceException
	 */
	@Transactional(rollbackFor = OHServiceException.class)
	public boolean newLaboratory2(List<Laboratory> labList, ArrayList<ArrayList<LaboratoryRow>> labRowList)
			throws OHServiceException {
		if (labList.isEmpty())
			throw new OHDataValidationException(
					new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
							MessageBundle.getMessage("angal.labnew.noexamsinserted.msg"), OHSeverityLevel.ERROR));
		if (labList.size() != labRowList.size())
			throw new OHDataValidationException(
					new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
							MessageBundle.getMessage("angal.labnew.someexamswithoutresultpleasecheck.msg"),
							OHSeverityLevel.ERROR));
		boolean result = true;
		for (int i = 0; i < labList.size(); i++) {
			result = result && newLaboratory2(labList.get(i), labRowList.get(i));
		}
		return result;
	}

	/**
	 * Inserts one Laboratory exam {@link Laboratory} (Procedure One)
	 *
	 * @param laboratory - the {@link Laboratory} to insert
	 * @return <code>true</code> if the exam has been inserted, <code>false</code> otherwise
	 * @throws OHServiceException
	 */
	protected boolean newLabFirstProcedure(Laboratory laboratory) throws OHServiceException {
		return ioOperations.newLabFirstProcedure(laboratory);
	}

	/**
	 * Inserts one Laboratory exam {@link Laboratory} with multiple results (Procedure Two)
	 *
	 * @param laboratory - the {@link Laboratory} to insert
	 * @param labRow - the list of results ({@link String}s)
	 * @return <code>true</code> if the exam has been inserted with all its results, <code>false</code> otherwise
	 * @throws OHServiceException
	 */
	protected boolean newLabSecondProcedure(Laboratory laboratory, ArrayList<String> labRow) throws OHServiceException {
		return ioOperations.newLabSecondProcedure(laboratory, labRow);
	}

	/**
	 * Update an already existing Laboratory exam {@link Laboratory} (Procedure One).
	 * If old exam was Procedure Two all its releated result are deleted.
	 *
	 * @param laboratory - the {@link Laboratory} to update
	 * @return <code>true</code> if the exam has been updated, <code>false</code> otherwise
	 * @throws OHServiceException
	 * @deprecated use updateLaboratory() for all procedures
	 */
	@Deprecated
	protected boolean editLabFirstProcedure(Laboratory laboratory) throws OHServiceException {
		return ioOperations.updateLabFirstProcedure(laboratory);
	}

	/**
	 * Update an already existing Laboratory exam {@link Laboratory} (Procedure Two).
	 * Previous results are deleted and replaced with new ones.
	 *
	 * @param laboratory - the {@link Laboratory} to update
	 * @return <code>true</code> if the exam has been updated with all its results, <code>false</code> otherwise
	 * @throws OHServiceException
	 * @deprecated use updateLaboratory() for all procedures
	 */
	@Deprecated
	protected boolean editLabSecondProcedure(Laboratory laboratory, ArrayList<String> labRow)
			throws OHServiceException {
		return ioOperations.updateLabSecondProcedure(laboratory, labRow);
	}

	/**
	 * Delete a Laboratory exam {@link Laboratory} (Procedure One or Two).
	 * Previous results, if any, are deleted as well.
	 *
	 * @param laboratory - the {@link Laboratory} to delete
	 * @return <code>true</code> if the exam has been deleted with all its results, if any. <code>false</code> otherwise
	 * @throws OHServiceException
	 */
	public boolean deleteLaboratory(Laboratory laboratory) throws OHServiceException {
		return ioOperations.deleteLaboratory(laboratory);
	}

	private void setLabMultipleResults(List<LaboratoryForPrint> labs) throws OHServiceException {

		List<LaboratoryRow> rows = null;

		for (LaboratoryForPrint lab : labs) {
			String labResult = lab.getResult();
			if (labResult.equalsIgnoreCase(MessageBundle.getMessage("angal.lab.multipleresults.txt"))) {
				rows = ioOperations.getLabRow(lab.getCode());

				if (rows == null || rows.isEmpty()) {
					lab.setResult(MessageBundle.getMessage("angal.lab.allnegative.txt"));
				} else {
					lab.setResult(
							MessageBundle.getMessage("angal.lab.positive.txt") + " : " + rows.get(0).getDescription());
					for (LaboratoryRow row : rows) {
						labResult += ("," + row.getDescription());
					}
					lab.setResult(labResult);
				}
			}
		}
	}

	public String getMaterialTranslated(String materialKey) {
		if (materialHashMap == null)
			buildMaterialHashMap();
		if (materialKey == null || !materialHashMap.containsKey(materialKey))
			return MessageBundle.getMessage("angal.lab.undefined.txt");
		else
			return materialHashMap.get(materialKey);
	}

	public String getMaterialKey(String description) {
		if (materialHashMap == null) {
			buildMaterialHashMap();
		}
		for (String key : materialHashMap.keySet()) {
			if (materialHashMap.get(key).equals(description)) {
				return key;
			}
		}
		return "undefined";
	}

	private void buildMaterialHashMap() {
		materialHashMap = new HashMap<>();
		materialHashMap.put("undefined", MessageBundle.getMessage("angal.lab.undefined.txt"));
		materialHashMap.put("blood", MessageBundle.getMessage("angal.lab.blood.txt"));
		materialHashMap.put("urine", MessageBundle.getMessage("angal.lab.urine.txt"));
		materialHashMap.put("stool", MessageBundle.getMessage("angal.lab.stool.txt"));
		materialHashMap.put("sputum", MessageBundle.getMessage("angal.lab.sputum.txt"));
		materialHashMap.put("cfs", MessageBundle.getMessage("angal.lab.cfs.txt"));
		materialHashMap.put("swabs", MessageBundle.getMessage("angal.lab.swabs.txt"));
		materialHashMap.put("tissues", MessageBundle.getMessage("angal.lab.tissues.txt"));
		materialHashMap.put("film", MessageBundle.getMessage("angal.lab.film.txt"));
	}

	/**
	 * Return a list of material descriptions (default: undefined):
	 * undefined,
	 * blood,
	 * urine,
	 * stool,
	 * sputum,
	 * cfs,
	 * swabs,
	 * tissues,
	 * film
	 *
	 * @return
	 */
	public ArrayList<String> getMaterialList() {
		if (materialHashMap == null)
			buildMaterialHashMap();
		ArrayList<String> materialDescriptionList = new ArrayList<>(materialHashMap.values());
		materialDescriptionList.sort(new DefaultSorter(MessageBundle.getMessage("angal.lab.undefined.txt")));
		return materialDescriptionList;
	}

	//	/**
	//	 * Returns the max progressive number within specified month of specified year.
	//	 *
	//	 * @param lab
	//	 * @return <code>int</code> - the progressive number in the month
	//	 * @throws org.isf.utils.exception.OHServiceException
	//	 */
	/*public int getProgMonth(int month, int year)  throws OHServiceException {
	    return ioOperations.getProgMonth(month, year);
	}*/

	/*  public Integer newLabFirstProcedure2(Laboratory lab)  throws OHServiceException {
	   return ioOperations.newLabFirstProcedure2(lab); //To change body of generated methods, choose Tools | Templates.
	}
	
	public Laboratory newLabSecondProcedure2(Laboratory lab, ArrayList<LaboratoryRow> laboratoryRows)  throws OHServiceException {
	    return ioOperations.newLabSecondProcedure2(lab, laboratoryRows); //To change body of generated methods, choose Tools | Templates.
	}*/
}
