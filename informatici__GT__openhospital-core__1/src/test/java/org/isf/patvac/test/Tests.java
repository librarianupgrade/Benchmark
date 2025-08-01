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
package org.isf.patvac.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.assertj.core.api.Condition;
import org.isf.OHCoreTestCase;
import org.isf.patient.model.Patient;
import org.isf.patient.model.PatientMergedEvent;
import org.isf.patient.service.PatientIoOperationRepository;
import org.isf.patient.test.TestPatient;
import org.isf.patvac.manager.PatVacManager;
import org.isf.patvac.model.PatientVaccine;
import org.isf.patvac.service.PatVacIoOperationRepository;
import org.isf.patvac.service.PatVacIoOperations;
import org.isf.utils.exception.OHServiceException;
import org.isf.vaccine.model.Vaccine;
import org.isf.vaccine.service.VaccineIoOperationRepository;
import org.isf.vaccine.test.TestVaccine;
import org.isf.vactype.model.VaccineType;
import org.isf.vactype.service.VaccineTypeIoOperationRepository;
import org.isf.vactype.test.TestVaccineType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

public class Tests extends OHCoreTestCase {

	private static TestPatientVaccine testPatientVaccine;
	private static TestVaccine testVaccine;
	private static TestVaccineType testVaccineType;
	private static TestPatient testPatient;

	@Autowired
	PatVacIoOperations patvacIoOperation;
	@Autowired
	PatientIoOperationRepository patientIoOperationRepository;
	@Autowired
	PatVacManager patVacManager;
	@Autowired
	VaccineIoOperationRepository vaccineIoOperationRepository;
	@Autowired
	VaccineTypeIoOperationRepository vaccineTypeIoOperationRepository;
	@Autowired
	PatVacIoOperationRepository patVacIoOperationRepository;
	@Autowired
	ApplicationEventPublisher applicationEventPublisher;

	@BeforeClass
	public static void setUpClass() {
		testPatientVaccine = new TestPatientVaccine();
		testPatient = new TestPatient();
		testVaccine = new TestVaccine();
		testVaccineType = new TestVaccineType();
	}

	@Before
	public void setUp() {
		cleanH2InMemoryDb();
	}

	@Test
	public void testPatientVaccineGets() throws Exception {
		int code = _setupTestPatientVaccine(false);
		_checkPatientVaccineIntoDb(code);
	}

	@Test
	public void testPatientVaccineSets() throws Exception {
		int code = _setupTestPatientVaccine(true);
		_checkPatientVaccineIntoDb(code);
	}

	@Test
	public void testIoGetPatientVaccineToday() throws Exception {
		VaccineType vaccineType = testVaccineType.setup(false);
		Vaccine vaccine = testVaccine.setup(vaccineType, false);
		Patient patient = testPatient.setup(false);
		PatientVaccine patientVaccine = testPatientVaccine.setup(patient, vaccine, true);

		patientVaccine.setVaccineDate(new GregorianCalendar());

		vaccineTypeIoOperationRepository.saveAndFlush(vaccineType);
		vaccineIoOperationRepository.saveAndFlush(vaccine);
		patientIoOperationRepository.saveAndFlush(patient);
		patVacIoOperationRepository.saveAndFlush(patientVaccine);

		ArrayList<PatientVaccine> patientVaccines = patvacIoOperation.getPatientVaccine(false);
		assertThat(patientVaccines.get(patientVaccines.size() - 1).getPatName()).isEqualTo(patientVaccine.getPatName());
	}

	@Test
	public void testIoGetPatientVaccineLastWeek() throws Exception {
		VaccineType vaccineType = testVaccineType.setup(false);
		Vaccine vaccine = testVaccine.setup(vaccineType, false);
		Patient patient = testPatient.setup(false);
		PatientVaccine patientVaccine = testPatientVaccine.setup(patient, vaccine, true);

		GregorianCalendar date = new GregorianCalendar();
		date.add(Calendar.DAY_OF_MONTH, -3);
		patientVaccine.setVaccineDate(date);

		vaccineTypeIoOperationRepository.saveAndFlush(vaccineType);
		vaccineIoOperationRepository.saveAndFlush(vaccine);
		patientIoOperationRepository.saveAndFlush(patient);
		patVacIoOperationRepository.saveAndFlush(patientVaccine);

		ArrayList<PatientVaccine> patientVaccines = patvacIoOperation.getPatientVaccine(true);
		assertThat(patientVaccines.get(patientVaccines.size() - 1).getPatName()).isEqualTo(patientVaccine.getPatName());
	}

	@Test
	public void testIoGetPatientVaccine() throws Exception {
		int code = _setupTestPatientVaccine(false);
		PatientVaccine foundPatientVaccine = patVacIoOperationRepository.findOne(code);
		ArrayList<PatientVaccine> patientVaccines = patvacIoOperation.getPatientVaccine(
				foundPatientVaccine.getVaccine().getVaccineType().getCode(), foundPatientVaccine.getVaccine().getCode(),
				foundPatientVaccine.getVaccineDate(), foundPatientVaccine.getVaccineDate(),
				foundPatientVaccine.getPatient().getSex(), foundPatientVaccine.getPatient().getAge(),
				foundPatientVaccine.getPatient().getAge());
		assertThat(patientVaccines.get(patientVaccines.size() - 1).getPatName())
				.isEqualTo(foundPatientVaccine.getPatName());
	}

	@Test
	@Transactional // requires active session because of lazy loading patient
	public void testIoUpdatePatientVaccine() throws Exception {
		int code = _setupTestPatientVaccine(false);
		PatientVaccine foundPatientVaccine = patVacIoOperationRepository.findOne(code);
		foundPatientVaccine.setPatName("Update");
		boolean result = patvacIoOperation.updatePatientVaccine(foundPatientVaccine);
		assertThat(result).isTrue();
		PatientVaccine updatePatientVaccine = patVacIoOperationRepository.findOne(code);
		assertThat(updatePatientVaccine.getPatName()).isEqualTo("Update");
	}

	@Test
	public void testIoNewPatientVaccine() throws Exception {
		Patient patient = testPatient.setup(false);
		VaccineType vaccineType = testVaccineType.setup(false);
		Vaccine vaccine = testVaccine.setup(vaccineType, false);
		patientIoOperationRepository.saveAndFlush(patient);
		vaccineTypeIoOperationRepository.saveAndFlush(vaccineType);
		vaccineIoOperationRepository.saveAndFlush(vaccine);
		PatientVaccine patientVaccine = testPatientVaccine.setup(patient, vaccine, true);
		boolean result = patvacIoOperation.newPatientVaccine(patientVaccine);
		assertThat(result).isTrue();
		_checkPatientVaccineIntoDb(patientVaccine.getCode());
	}

	@Test
	public void testIoDeletePatientVaccine() throws Exception {
		int code = _setupTestPatientVaccine(false);
		PatientVaccine foundPatientVaccine = patVacIoOperationRepository.findOne(code);
		boolean result = patvacIoOperation.deletePatientVaccine(foundPatientVaccine);
		assertThat(result).isTrue();
		result = patvacIoOperation.isCodePresent(code);
		assertThat(result).isFalse();
	}

	@Test
	public void testIoGetProgYear() throws Exception {
		int prog_year = 0;
		int found_prog_year = 0;
		_setupTestPatientVaccine(true);
		List<PatientVaccine> patientVaccineList = patvacIoOperation.getPatientVaccine(null, null, null, null, 'A', 0,
				0);
		for (PatientVaccine patVac : patientVaccineList) {
			if (patVac.getProgr() > found_prog_year)
				found_prog_year = patVac.getProgr();
		}
		prog_year = patvacIoOperation.getProgYear(0);
		assertThat(prog_year).isEqualTo(found_prog_year);

		prog_year = patvacIoOperation.getProgYear(1984); //TestPatientVaccine's year
		assertThat(prog_year).isEqualTo(10);
	}

	@Test
	public void testListenerShouldUpdatePatientToMergedWhenPatientMergedEventArrive() throws Exception {
		// given:
		int id = _setupTestPatientVaccine(false);
		PatientVaccine found = patVacIoOperationRepository.findOne(id);
		Patient mergedPatient = _setupTestPatient(false);

		// when:
		applicationEventPublisher.publishEvent(new PatientMergedEvent(found.getPatient(), mergedPatient));

		// then:
		PatientVaccine result = patVacIoOperationRepository.findOne(id);
		assertThat(result.getPatient().getCode()).isEqualTo(mergedPatient.getCode());
	}

	@Test
	public void testMgrGetPatientVaccineToday() throws Exception {
		VaccineType vaccineType = testVaccineType.setup(false);
		vaccineType.setCode("A");
		Vaccine vaccine = testVaccine.setup(vaccineType, false);
		vaccine.setCode("ABC");
		Patient patient = testPatient.setup(false);
		PatientVaccine patientVaccine = testPatientVaccine.setup(patient, vaccine, true);

		// Today
		GregorianCalendar now = new GregorianCalendar();
		patientVaccine.setVaccineDate(now);

		vaccineTypeIoOperationRepository.saveAndFlush(vaccineType);
		vaccineIoOperationRepository.saveAndFlush(vaccine);
		patientIoOperationRepository.saveAndFlush(patient);
		patVacIoOperationRepository.saveAndFlush(patientVaccine);

		VaccineType vaccineType2 = testVaccineType.setup(false);
		vaccineType2.setCode("X");
		Vaccine vaccine2 = testVaccine.setup(vaccineType2, false);
		vaccine2.setCode("CBA");
		Patient patient2 = testPatient.setup(false);
		PatientVaccine patientVaccine2 = testPatientVaccine.setup(patient2, vaccine2, true);

		// 8 days ago
		now.add(Calendar.DATE, -8);
		patientVaccine2.setVaccineDate(now);

		vaccineTypeIoOperationRepository.saveAndFlush(vaccineType2);
		vaccineIoOperationRepository.saveAndFlush(vaccine2);
		patientIoOperationRepository.saveAndFlush(patient2);
		patVacIoOperationRepository.saveAndFlush(patientVaccine2);

		ArrayList<PatientVaccine> patientVaccines = patVacManager.getPatientVaccine(false);
		assertThat(patientVaccines).hasSize(1);
		assertThat(patientVaccines.get(patientVaccines.size() - 1).getPatName()).isEqualTo(patientVaccine.getPatName());
	}

	@Test
	public void testMgrGetPatientVaccineLastWeek() throws Exception {
		VaccineType vaccineType = testVaccineType.setup(false);
		vaccineType.setCode("A");
		vaccineType.setDescription("Type Description1");
		Vaccine vaccine = testVaccine.setup(vaccineType, false);
		vaccine.setCode("ABC");
		vaccine.setDescription("Description1");
		Patient patient = testPatient.setup(false);
		patient.setFirstName("firstName1");
		patient.setSecondName("secondName1");
		PatientVaccine patientVaccine = testPatientVaccine.setup(patient, vaccine, true);

		GregorianCalendar now = new GregorianCalendar();
		now.add(Calendar.DAY_OF_MONTH, -3);
		patientVaccine.setVaccineDate(now);

		vaccineTypeIoOperationRepository.saveAndFlush(vaccineType);
		vaccineIoOperationRepository.saveAndFlush(vaccine);
		patientIoOperationRepository.saveAndFlush(patient);
		patVacIoOperationRepository.saveAndFlush(patientVaccine);

		VaccineType vaccineType2 = testVaccineType.setup(false);
		vaccineType2.setCode("Z");
		vaccineType2.setDescription("Type Description2");
		Vaccine vaccine2 = testVaccine.setup(vaccineType2, false);
		vaccine2.setCode("CBA");
		vaccine2.setDescription("Description2");
		Patient patient2 = testPatient.setup(false);
		patient2.setFirstName("firstName2");
		patient2.setSecondName("secondName2");
		PatientVaccine patientVaccine2 = testPatientVaccine.setup(patient2, vaccine2, true);

		// 8 days ago
		now.add(Calendar.DATE, -8);
		patientVaccine2.setVaccineDate(now);

		vaccineTypeIoOperationRepository.saveAndFlush(vaccineType2);
		vaccineIoOperationRepository.saveAndFlush(vaccine2);
		patientIoOperationRepository.saveAndFlush(patient2);
		patVacIoOperationRepository.saveAndFlush(patientVaccine2);

		ArrayList<PatientVaccine> patientVaccines = patVacManager.getPatientVaccine(true);
		assertThat(patientVaccines).hasSize(1);
		assertThat(patientVaccines.get(patientVaccines.size() - 1).getPatName()).isEqualTo(patientVaccine.getPatName());
	}

	@Test
	public void testMgrGetPatientVaccine() throws Exception {
		int code = _setupTestPatientVaccine(false);
		PatientVaccine foundPatientVaccine = patVacIoOperationRepository.findOne(code);
		ArrayList<PatientVaccine> patientVaccines = patVacManager.getPatientVaccine(
				foundPatientVaccine.getVaccine().getVaccineType().getCode(), foundPatientVaccine.getVaccine().getCode(),
				foundPatientVaccine.getVaccineDate(), foundPatientVaccine.getVaccineDate(),
				foundPatientVaccine.getPatient().getSex(), foundPatientVaccine.getPatient().getAge(),
				foundPatientVaccine.getPatient().getAge());
		assertThat(patientVaccines.get(patientVaccines.size() - 1).getPatName())
				.isEqualTo(foundPatientVaccine.getPatName());
	}

	@Test
	@Transactional // requires active session because of lazy loading patient
	public void testMgrUpdatePatientVaccine() throws Exception {
		int code = _setupTestPatientVaccine(false);
		PatientVaccine foundPatientVaccine = patVacIoOperationRepository.findOne(code);
		foundPatientVaccine.setPatName("Update");
		boolean result = patVacManager.updatePatientVaccine(foundPatientVaccine);
		assertThat(result).isTrue();
		PatientVaccine updatePatientVaccine = patVacIoOperationRepository.findOne(code);
		assertThat(updatePatientVaccine.getPatName()).isEqualTo("Update");
	}

	@Test
	public void testMgrNewPatientVaccine() throws Exception {
		Patient patient = testPatient.setup(false);
		VaccineType vaccineType = testVaccineType.setup(false);
		Vaccine vaccine = testVaccine.setup(vaccineType, false);
		patientIoOperationRepository.saveAndFlush(patient);
		vaccineTypeIoOperationRepository.saveAndFlush(vaccineType);
		vaccineIoOperationRepository.saveAndFlush(vaccine);
		PatientVaccine patientVaccine = testPatientVaccine.setup(patient, vaccine, true);
		boolean result = patVacManager.newPatientVaccine(patientVaccine);
		assertThat(result).isTrue();
		_checkPatientVaccineIntoDb(patientVaccine.getCode());
	}

	@Test
	public void testMgrDeletePatientVaccine() throws Exception {
		int code = _setupTestPatientVaccine(true);
		PatientVaccine foundPatientVaccine = patVacIoOperationRepository.findOne(code);
		assertThat(patVacManager.deletePatientVaccine(foundPatientVaccine)).isTrue();
		assertThat(patvacIoOperation.isCodePresent(code)).isFalse();
	}

	@Test
	public void testMgrGetProgYear() throws Exception {
		int prog_year = 0;
		int found_prog_year = 0;
		_setupTestPatientVaccine(true);
		List<PatientVaccine> patientVaccineList = patVacManager.getPatientVaccine(null, null, null, null, 'A', 0, 0);
		for (PatientVaccine patVac : patientVaccineList) {
			if (patVac.getProgr() > found_prog_year)
				found_prog_year = patVac.getProgr();
		}
		prog_year = patVacManager.getProgYear(0);
		assertThat(prog_year).isEqualTo(found_prog_year);

		prog_year = patVacManager.getProgYear(1984); //TestPatientVaccine's year
		assertThat(prog_year).isEqualTo(10);
	}

	@Test
	public void testMgrValidationVaccineDateIsNull() {
		assertThatThrownBy(() -> {
			VaccineType vaccineType = testVaccineType.setup(false);
			Vaccine vaccine = testVaccine.setup(vaccineType, false);
			Patient patient = testPatient.setup(false);
			PatientVaccine patientVaccine = testPatientVaccine.setup(patient, vaccine, true);

			patientVaccine.setVaccineDate(null);

			patVacManager.newPatientVaccine(patientVaccine);
		}).isInstanceOf(OHServiceException.class).has(new Condition<Throwable>(
				(e -> ((OHServiceException) e).getMessages().size() == 1), "Expecting single validation error"));
	}

	@Test
	public void testMgrValidationProgrLessThanZero() {
		assertThatThrownBy(() -> {
			VaccineType vaccineType = testVaccineType.setup(false);
			Vaccine vaccine = testVaccine.setup(vaccineType, false);
			Patient patient = testPatient.setup(false);
			PatientVaccine patientVaccine = testPatientVaccine.setup(patient, vaccine, true);

			patientVaccine.setProgr(-99);

			patVacManager.newPatientVaccine(patientVaccine);
		}).isInstanceOf(OHServiceException.class).has(new Condition<Throwable>(
				(e -> ((OHServiceException) e).getMessages().size() == 1), "Expecting single validation error"));
	}

	@Test
	public void testMgrValidationVaccineIsNull() {
		assertThatThrownBy(() -> {
			VaccineType vaccineType = testVaccineType.setup(false);
			Vaccine vaccine = testVaccine.setup(vaccineType, false);
			Patient patient = testPatient.setup(false);
			PatientVaccine patientVaccine = testPatientVaccine.setup(patient, vaccine, true);

			patientVaccine.setVaccine(null);

			patVacManager.newPatientVaccine(patientVaccine);
		}).isInstanceOf(OHServiceException.class).has(new Condition<Throwable>(
				(e -> ((OHServiceException) e).getMessages().size() == 1), "Expecting single validation error"));
	}

	@Test
	public void testMgrValidationPatientIsNull() {
		assertThatThrownBy(() -> {
			VaccineType vaccineType = testVaccineType.setup(false);
			Vaccine vaccine = testVaccine.setup(vaccineType, false);
			Patient patient = testPatient.setup(false);
			PatientVaccine patientVaccine = testPatientVaccine.setup(patient, vaccine, true);

			patientVaccine.setPatient(null);

			patVacManager.newPatientVaccine(patientVaccine);
		}).isInstanceOf(OHServiceException.class).has(new Condition<Throwable>(
				(e -> ((OHServiceException) e).getMessages().size() == 1), "Expecting single validation error"));
	}

	@Test
	public void testMgrValidationPatientNameIsEmpty() {
		assertThatThrownBy(() -> {
			VaccineType vaccineType = testVaccineType.setup(false);
			Vaccine vaccine = testVaccine.setup(vaccineType, false);
			Patient patient = testPatient.setup(false);
			PatientVaccine patientVaccine = testPatientVaccine.setup(patient, vaccine, true);

			patientVaccine.getPatient().setFirstName(" ");

			patVacManager.newPatientVaccine(patientVaccine);
		}).isInstanceOf(OHServiceException.class).has(new Condition<Throwable>(
				(e -> ((OHServiceException) e).getMessages().size() == 1), "Expecting single validation error"));
	}

	@Test
	public void testMgrValidationPatientSexIsEmpty() {
		assertThatThrownBy(() -> {
			VaccineType vaccineType = testVaccineType.setup(false);
			Vaccine vaccine = testVaccine.setup(vaccineType, false);
			Patient patient = testPatient.setup(false);
			PatientVaccine patientVaccine = testPatientVaccine.setup(patient, vaccine, true);

			patientVaccine.getPatient().setSex(' ');

			patVacManager.newPatientVaccine(patientVaccine);
		}).isInstanceOf(OHServiceException.class).has(new Condition<Throwable>(
				(e -> ((OHServiceException) e).getMessages().size() == 1), "Expecting single validation error"));
	}

	@Test
	public void testPatientVaccineConstuctor() {
		PatientVaccine patientVaccine = new PatientVaccine(0, 10, new GregorianCalendar(), new Patient(), new Vaccine(),
				0, "patName", 21, 'F');

		assertThat(patientVaccine.getPatAge()).isEqualTo(21);
		patientVaccine.setPatAge(-1);
		assertThat(patientVaccine.getPatAge()).isEqualTo(-1);

		assertThat(patientVaccine.getPatSex()).isEqualTo('F');
		patientVaccine.setPatSex(' ');
		assertThat(patientVaccine.getPatSex()).isEqualTo(' ');
	}

	@Test
	public void testPatientVaccineHashCode() {
		PatientVaccine patientVaccine = new PatientVaccine(0, 0, null, new Patient(), null, 0, "patName", 21, 'F');
		patientVaccine.setPatient(null);
		assertThat(patientVaccine.hashCode()).isEqualTo(31 * 31 * 31 * 31 * 31);
	}

	@Test
	public void testPatientVaccineEquals() {
		PatientVaccine patientVaccine1 = new PatientVaccine(0, 0, null, new Patient(), null, 0, "patName", 21, 'F');
		PatientVaccine patientVaccine2 = new PatientVaccine(0, 0, null, new Patient(), null, 0, "patName", 21, 'F');

		assertThat(patientVaccine1.equals(patientVaccine1)).isTrue();
		assertThat(patientVaccine1).isNotNull().isNotEqualTo("someString");

		patientVaccine2.setCode(-99);
		assertThat(patientVaccine1).isNotEqualTo(patientVaccine2);

		patientVaccine2.setCode(patientVaccine1.getCode());
		patientVaccine2.setPatient(null);
		assertThat(patientVaccine1).isNotEqualTo(patientVaccine2);

		patientVaccine1.setPatient(null);
		patientVaccine2.setPatient(new Patient());
		patientVaccine2.getPatient().setCode(1);
		assertThat(patientVaccine1).isNotEqualTo(patientVaccine2);

		patientVaccine1.setPatient(new Patient());
		patientVaccine1.getPatient().setCode(1);
		assertThat(patientVaccine1).isEqualTo(patientVaccine2);

		patientVaccine2.setProgr(-99);
		assertThat(patientVaccine1).isNotEqualTo(patientVaccine2);

		patientVaccine2.setProgr(patientVaccine1.getProgr());
		patientVaccine2.setVaccine(new Vaccine());
		patientVaccine2.getVaccine().setCode("Z");
		patientVaccine2.getVaccine().setDescription("desciption");
		patientVaccine2.getVaccine().setVaccineType(new VaccineType());
		patientVaccine2.getVaccine().getVaccineType().setCode("A");
		patientVaccine2.getVaccine().getVaccineType().setDescription("description");
		assertThat(patientVaccine1).isNotEqualTo(patientVaccine2);

		patientVaccine1.setVaccine(new Vaccine());
		patientVaccine1.getVaccine().setCode("Z");
		patientVaccine1.getVaccine().setDescription("desciption");
		patientVaccine1.getVaccine().setVaccineType(new VaccineType());
		patientVaccine1.getVaccine().getVaccineType().setCode("A");
		patientVaccine1.getVaccine().getVaccineType().setDescription("description");
		assertThat(patientVaccine1).isEqualTo(patientVaccine2);

		patientVaccine1.setVaccineDate(new GregorianCalendar());
		patientVaccine2.setVaccineDate(null);
		assertThat(patientVaccine1).isNotEqualTo(patientVaccine2);

		patientVaccine2.setVaccineDate(patientVaccine1.getVaccineDate());
		patientVaccine1.setVaccineDate(null);
		assertThat(patientVaccine1).isNotEqualTo(patientVaccine2);

		patientVaccine2.setVaccineDate(null);
		assertThat(patientVaccine1).isEqualTo(patientVaccine2);
	}

	private Patient _setupTestPatient(boolean usingSet) throws Exception {
		Patient patient = testPatient.setup(usingSet);
		patientIoOperationRepository.saveAndFlush(patient);
		return patient;
	}

	private int _setupTestPatientVaccine(boolean usingSet) throws Exception {
		VaccineType vaccineType = testVaccineType.setup(false);
		Vaccine vaccine = testVaccine.setup(vaccineType, false);
		Patient patient = testPatient.setup(false);
		PatientVaccine patientVaccine = testPatientVaccine.setup(patient, vaccine, usingSet);
		vaccineTypeIoOperationRepository.saveAndFlush(vaccineType);
		vaccineIoOperationRepository.saveAndFlush(vaccine);
		patientIoOperationRepository.saveAndFlush(patient);
		patVacIoOperationRepository.saveAndFlush(patientVaccine);
		return patientVaccine.getCode();
	}

	private void _checkPatientVaccineIntoDb(int code) {
		PatientVaccine foundPatientVaccine = patVacIoOperationRepository.findOne(code);
		testPatientVaccine.check(foundPatientVaccine);
	}
}