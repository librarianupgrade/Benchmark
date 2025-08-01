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
package org.isf.dicomtype.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;

import org.assertj.core.api.Condition;
import org.isf.OHCoreTestCase;
import org.isf.dicomtype.manager.DicomTypeBrowserManager;
import org.isf.dicomtype.model.DicomType;
import org.isf.dicomtype.service.DicomTypeIoOperation;
import org.isf.dicomtype.service.DicomTypeIoOperationRepository;
import org.isf.utils.exception.OHDataIntegrityViolationException;
import org.isf.utils.exception.OHDataValidationException;
import org.isf.utils.exception.OHException;
import org.isf.utils.exception.OHServiceException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class Tests extends OHCoreTestCase {

	private static TestDicomType testDicomType;

	@Autowired
	DicomTypeIoOperation dicomTypeIoOperation;
	@Autowired
	DicomTypeIoOperationRepository dicomTypeIoOperationRepository;
	@Autowired
	DicomTypeBrowserManager dicomTypeBrowserManager;

	@BeforeClass
	public static void setUpClass() {
		testDicomType = new TestDicomType();
	}

	@Before
	public void setUp() {
		cleanH2InMemoryDb();
	}

	@Test
	public void testDicomTypeGets() throws Exception {
		String code = _setupTestDicomType(false);
		_checkDicomTypeIntoDb(code);
	}

	@Test
	public void testDicomTypeSets() throws Exception {
		String code = _setupTestDicomType(true);
		_checkDicomTypeIntoDb(code);
	}

	@Test
	public void testIoGetDicomType() throws Exception {
		String typeId = _setupTestDicomType(false);
		ArrayList<DicomType> dicomTypes = dicomTypeIoOperation.getDicomType();
		testDicomType.check(dicomTypes.get(0));
	}

	@Test
	public void testIoUpdateDicomType() throws Exception {
		String typeId = _setupTestDicomType(false);
		DicomType dicomType = dicomTypeIoOperationRepository.findOne(typeId);
		dicomType.setDicomTypeDescription("newDescription");
		assertThat(dicomTypeIoOperation.updateDicomType(dicomType)).isTrue();
		DicomType dicomType2 = dicomTypeIoOperationRepository.findOne(typeId);
		assertThat(dicomType2.getDicomTypeDescription()).isEqualTo("newDescription");
	}

	@Test
	public void testIoNewDicomType() throws Exception {
		DicomType dicomType = new DicomType("id", "description");
		assertThat(dicomTypeIoOperation.newDicomType(dicomType)).isTrue();
		DicomType dicomType2 = dicomTypeIoOperationRepository.findOne(dicomType.getDicomTypeID());
		assertThat(dicomType2.getDicomTypeDescription()).isEqualTo("description");
	}

	@Test
	public void testIoDeleteDicomType() throws Exception {
		DicomType dicomType = new DicomType("id", "description");
		assertThat(dicomTypeIoOperation.newDicomType(dicomType)).isTrue();
		assertThat(dicomTypeIoOperation.deleteDicomType(dicomType)).isTrue();
		assertThat(dicomTypeIoOperation.isCodePresent(dicomType.getDicomTypeID())).isFalse();
	}

	@Test
	public void testIoIsCodePresent() throws Exception {
		DicomType dicomType = new DicomType("id", "description");
		assertThat(dicomTypeIoOperation.newDicomType(dicomType)).isTrue();
		assertThat(dicomTypeIoOperation.isCodePresent(dicomType.getDicomTypeID())).isTrue();
	}

	@Test
	public void testMgrGetDicomType() throws Exception {
		String typeId = _setupTestDicomType(false);
		ArrayList<DicomType> dicomTypes = dicomTypeBrowserManager.getDicomType();
		testDicomType.check(dicomTypes.get(0));
	}

	@Test
	public void testMgrUpdateDicomType() throws Exception {
		String typeId = _setupTestDicomType(false);
		DicomType dicomType = dicomTypeIoOperationRepository.findOne(typeId);
		dicomType.setDicomTypeDescription("newDescription");
		assertThat(dicomTypeBrowserManager.updateDicomType(dicomType)).isTrue();
		DicomType dicomType2 = dicomTypeIoOperationRepository.findOne(typeId);
		assertThat(dicomType2.getDicomTypeDescription()).isEqualTo("newDescription");
	}

	@Test
	public void testMgrNewDicomType() throws Exception {
		DicomType dicomType = new DicomType("id", "description");
		assertThat(dicomTypeBrowserManager.newDicomType(dicomType)).isTrue();
		DicomType dicomType2 = dicomTypeIoOperationRepository.findOne(dicomType.getDicomTypeID());
		assertThat(dicomType2.getDicomTypeDescription()).isEqualTo("description");
	}

	@Test
	public void testMgrDeleteDicomType() throws Exception {
		DicomType dicomType = new DicomType("id", "description");
		assertThat(dicomTypeBrowserManager.newDicomType(dicomType)).isTrue();
		assertThat(dicomTypeBrowserManager.deleteDicomType(dicomType)).isTrue();
		assertThat(dicomTypeBrowserManager.isCodePresent(dicomType.getDicomTypeID())).isFalse();
	}

	@Test
	public void testMgrIsCodePresent() throws Exception {
		DicomType dicomType = new DicomType("id", "description");
		assertThat(dicomTypeBrowserManager.newDicomType(dicomType)).isTrue();
		assertThat(dicomTypeBrowserManager.isCodePresent(dicomType.getDicomTypeID())).isTrue();
	}

	@Test
	public void testMgrValidationTypeIdIsEmpty() throws Exception {
		String typeId = _setupTestDicomType(false);
		DicomType dicomType = dicomTypeIoOperationRepository.findOne(typeId);
		dicomType.setDicomTypeID("");
		assertThatThrownBy(() -> dicomTypeBrowserManager.updateDicomType(dicomType))
				.isInstanceOf(OHDataValidationException.class)
				.has(new Condition<Throwable>((e -> ((OHServiceException) e).getMessages().size() == 1),
						"Expecting single validation error"));
	}

	@Test
	public void testMgrValidationTypeIdIsTooLong() throws Exception {
		String typeId = _setupTestDicomType(false);
		DicomType dicomType = dicomTypeIoOperationRepository.findOne(typeId);
		dicomType.setDicomTypeID("thisIsAKeyThatIsTooLong");
		assertThatThrownBy(() -> dicomTypeBrowserManager.updateDicomType(dicomType))
				.isInstanceOf(OHDataValidationException.class)
				.has(new Condition<Throwable>((e -> ((OHServiceException) e).getMessages().size() == 1),
						"Expecting single validation error"));
	}

	@Test
	public void testMgrValidationDescriptionIsEmpty() throws Exception {
		DicomType dicomType = new DicomType("id", "");
		assertThatThrownBy(() -> dicomTypeBrowserManager.newDicomType(dicomType))
				.isInstanceOf(OHDataValidationException.class)
				.has(new Condition<Throwable>((e -> ((OHServiceException) e).getMessages().size() == 1),
						"Expecting single validation error"));
	}

	@Test
	public void testMgrValidationCodeAlreadyExists() throws Exception {
		String typeId = _setupTestDicomType(true);
		DicomType dicomType = dicomTypeIoOperationRepository.findOne(typeId);
		assertThatThrownBy(() -> dicomTypeBrowserManager.newDicomType(dicomType))
				.isInstanceOf(OHDataIntegrityViolationException.class)
				.has(new Condition<Throwable>((e -> ((OHServiceException) e).getMessages().size() == 1),
						"Expecting single validation error"));
	}

	@Test
	public void testDicomTypeToString() throws Exception {
		DicomType dicomType = new DicomType("id", "someDescription");
		assertThat(dicomType).hasToString("someDescription");
	}

	private String _setupTestDicomType(boolean usingSet) throws OHException {
		DicomType dicomType = testDicomType.setup(usingSet);
		dicomTypeIoOperationRepository.saveAndFlush(dicomType);
		return dicomType.getDicomTypeID();
	}

	private void _checkDicomTypeIntoDb(String code) throws OHException {
		DicomType foundDicomType = dicomTypeIoOperationRepository.findOne(code);
		testDicomType.check(foundDicomType);
	}
}
