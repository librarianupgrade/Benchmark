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

import java.util.ArrayList;
import org.isf.admission.model.Admission;
import org.isf.opd.model.Opd;
import org.isf.operation.model.OperationRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author hp
 */
@Repository
public interface OperationRowIoOperationRepository extends JpaRepository<OperationRow, String> {
	ArrayList<OperationRow> findByOrderByOpDateDesc();

	ArrayList<OperationRow> findByAdmission(Admission adm);

	OperationRow findById(int id);

	ArrayList<OperationRow> findByOpd(Opd opd);
}
