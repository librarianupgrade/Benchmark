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
package org.isf.medicalstockward.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.isf.medicals.model.Medical;
import org.isf.medicalstock.model.Lot;
import org.isf.utils.db.Auditable;
import org.isf.utils.db.DbJpaUtil;
import org.isf.ward.model.Ward;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * ------------------------------------------
 * Medical Ward - model for the medical ward entity
 * -----------------------------------------
 * modification history
 * ? - ?
 * 17/01/2015 - Antonio - ported to JPA
 * ------------------------------------------
 */
@Entity
@Table(name = "MEDICALDSRWARD")
@EntityListeners(AuditingEntityListener.class)
@AttributeOverrides({ @AttributeOverride(name = "createdBy", column = @Column(name = "MDSRWRD_CREATED_BY")),
		@AttributeOverride(name = "createdDate", column = @Column(name = "MDSRWRD_CREATED_DATE")),
		@AttributeOverride(name = "lastModifiedBy", column = @Column(name = "MDSRWRD_LAST_MODIFIED_BY")),
		@AttributeOverride(name = "active", column = @Column(name = "MDSRWRD_ACTIVE")),
		@AttributeOverride(name = "lastModifiedDate", column = @Column(name = "MDSRWRD_LAST_MODIFIED_DATE")) })
public class MedicalWard extends Auditable<String> implements Comparable<Object> {
	@Autowired
	@Transient
	private DbJpaUtil jpa;

	@EmbeddedId
	MedicalWardId id;

	@Column(name = "MDSRWRD_IN_QTI")
	private float in_quantity;

	@Column(name = "MDSRWRD_OUT_QTI")
	private float out_quantity;

	@Transient
	private Double qty = 0.0;

	@Transient
	private volatile int hashCode = 0;

	public MedicalWard() {
		super();
		this.id = new MedicalWardId();

	}

	public MedicalWard(Medical medical, Double qty) {
		super();
		this.id = new MedicalWardId();
		this.id.setMedical(medical);
		this.qty = qty;
	}

	public MedicalWard(Ward ward, Medical medical, float in_quantity, float out_quantity, Lot lot) {
		super();
		this.id = new MedicalWardId(ward, medical, lot);
		this.in_quantity = in_quantity;
		this.out_quantity = out_quantity;

	}

	public MedicalWard(Medical med, double qanty, Lot lot) {
		super();
		this.id = new MedicalWardId();

		this.id.setMedical(med);
		this.id.setLot(lot);
		this.qty = qanty;
	}

	public MedicalWardId getId() {
		return id;
	}

	public void setId(Ward ward, Medical medical, Lot lot) {
		this.id = new MedicalWardId(ward, medical, lot);
	}

	public Lot getLot() {
		return id.getLot();
	}

	public void setMedical(Medical medical) {
		this.id.setMedical(medical);
	}

	public Double getQty() {
		return qty;
	}

	public void setQty(Double qty) {
		this.qty = qty;
	}

	public int compareTo(Object anObject) {

		Medical medical = id.getMedical();
		if (anObject instanceof MedicalWard)
			return (medical.getDescription().toUpperCase()
					.compareTo(((MedicalWard) anObject).getMedical().getDescription().toUpperCase()));
		else
			return 0;
	}

	public Ward getWard() {
		return this.id.getWard();
	}

	public void setWard(Ward ward) {
		this.id.setWard(ward);
	}

	public Medical getMedical() {
		return this.id.getMedical();
	}

	public void setLot(Lot lot) {
		id.setLot(lot);
	}

	public float getInQuantity() {
		return this.in_quantity;
	}

	public void setInQuantity(float in_quantity) {
		this.in_quantity = in_quantity;
	}

	public float getOutQuantity() {
		return this.out_quantity;
	}

	public void setOutQuantity(float out_quantity) {
		this.out_quantity = out_quantity;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof MedicalWard)) {
			return false;
		}

		MedicalWard ward = (MedicalWard) obj;
		return (this.id.getMedical() == ward.id.getMedical());
	}

	@Override
	public int hashCode() {
		if (this.hashCode == 0) {
			final int m = 23;
			int c = 133;

			c = m * c + this.id.getMedical().getCode();

			this.hashCode = c;
		}

		return this.hashCode;
	}
}
