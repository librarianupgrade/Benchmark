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
package org.isf.operation.service;

import org.isf.operation.model.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;
import java.util.List;

public interface OperationIoOperationRepository extends JpaRepository<Operation, String> {
	List<Operation> findByOrderByDescriptionAsc();

	List<Operation> findAllByDescriptionContainsOrderByDescriptionDesc(String description);

	Operation findOneByDescriptionAndType_Code(String description, String type);

	Operation findByCode(String code);

	@Query(value = "SELECT * FROM OPERATION JOIN OPERATIONTYPE ON OPE_OCL_ID_A = OCL_ID_A WHERE OPE_FOR LIKE 1 OR  OPE_FOR LIKE 3  ORDER BY OPE_DESC", nativeQuery = true)
	ArrayList<Operation> findAllWithoutDescriptionOpd();

	@Query(value = "SELECT * FROM OPERATION JOIN OPERATIONTYPE ON OPE_OCL_ID_A = OCL_ID_A WHERE OPE_FOR LIKE 1 OR  OPE_FOR LIKE 2  ORDER BY OPE_DESC", nativeQuery = true)
	ArrayList<Operation> findAllWithoutDescriptionAdm();

	List<Operation> findAllByType_DescriptionContainsOrderByDescriptionAsc(String typeDescription);
}