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
package org.isf.accounting.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.isf.accounting.model.Bill;
import org.isf.accounting.model.BillItems;
import org.isf.accounting.model.BillPayments;
import org.isf.patient.model.Patient;
import org.isf.utils.db.TranslateOHServiceException;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.time.TimeTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence class for Accounting module.
 */
@Service
@Transactional(rollbackFor = OHServiceException.class)
@TranslateOHServiceException
public class AccountingIoOperations {

	@Autowired
	private AccountingBillIoOperationRepository billRepository;
	@Autowired
	private AccountingBillPaymentIoOperationRepository billPaymentRepository;
	@Autowired
	private AccountingBillItemsIoOperationRepository billItemsRepository;

	/**
	 * Returns all the pending {@link Bill}s for the specified patient.
	 * @param patID the patient id.
	 * @return the list of pending bills.
	 * @throws OHServiceException if an error occurs retrieving the pending bills.
	 */
	public ArrayList<Bill> getPendingBills(int patID) throws OHServiceException {
		if (patID != 0)
			return new ArrayList<>(billRepository.findByStatusAndBillPatientCodeOrderByDateDesc("O", patID));

		return new ArrayList<>(billRepository.findByStatusOrderByDateDesc("O"));
	}

	/**
	 * Get all the {@link Bill}s.
	 * @return a list of bills.
	 * @throws OHServiceException if an error occurs retrieving the bills.
	 */
	public ArrayList<Bill> getBills() throws OHServiceException {
		return new ArrayList<>(billRepository.findAllByOrderByDateDesc());
	}

	/**
	 * Get the {@link Bill} with specified billID.
	 * @param billID
	 * @return the {@link Bill}.
	 * @throws OHServiceException if an error occurs retrieving the bill.
	 */
	public Bill getBill(int billID) throws OHServiceException {
		return billRepository.findOne(billID);
	}

	/**
	 * Returns all user ids from {@link BillPayments}.
	 * @return a list of user id.
	 * @throws OHServiceException if an error occurs retrieving the users list.
	 */
	public ArrayList<String> getUsers() throws OHServiceException {

		return new ArrayList<>(billPaymentRepository.findUserDistinctByOrderByUserAsc());
	}

	/**
	 * Returns the {@link BillItems} associated to the specified {@link Bill} id or all 
	 * the stored {@link BillItems} if no id is provided. 
	 * @param billID the bill id or <code>0</code>.
	 * @return a list of {@link BillItems} associated to the bill id or all the stored bill items.
	 * @throws OHServiceException if an error occurs retrieving the bill items.
	 */
	public ArrayList<BillItems> getItems(int billID) throws OHServiceException {
		ArrayList<BillItems> billItems = null;

		if (billID != 0) {
			billItems = new ArrayList<>(billItemsRepository.findByBill_idOrderByIdAsc(billID));
		} else {
			billItems = new ArrayList<>(billItemsRepository.findAllByOrderByIdAsc());
		}

		return billItems;
	}

	/**
	 * Retrieves all the {@link BillPayments} for the specified date range.
	 * @param dateFrom low endpoint, inclusive, for the date range. 
	 * @param dateTo high endpoint, inclusive, for the date range.
	 * @return a list of {@link BillPayments} for the specified date range.
	 * @throws OHServiceException if an error occurs retrieving the bill payments.
	 */
	public ArrayList<BillPayments> getPayments(GregorianCalendar dateFrom, GregorianCalendar dateTo)
			throws OHServiceException {

		return new ArrayList<>(billPaymentRepository.findByDateBetweenOrderByIdAscDateAsc(
				TimeTools.getBeginningOfDay(dateFrom), TimeTools.getBeginningOfNextDay(dateTo)));
	}

	/**
	 * Retrieves all the {@link BillPayments} for the specified {@link Bill} id, or all 
	 * the stored {@link BillPayments} if no id is indicated.
	 * @param billID the bill id or <code>0</code>.
	 * @return the list of bill payments.
	 * @throws OHServiceException if an error occurs retrieving the bill payments.
	 */
	public ArrayList<BillPayments> getPayments(int billID) throws OHServiceException {
		ArrayList<BillPayments> payments = null;

		if (billID != 0) {
			payments = (ArrayList<BillPayments>) billPaymentRepository.findAllWherBillIdByOrderByBillAndDate(billID);
		} else {
			payments = (ArrayList<BillPayments>) billPaymentRepository.findAllByOrderByBillAndDate();
		}

		return payments;
	}

	/**
	 * Stores a new {@link Bill}.
	 * @param newBill the bill to store.
	 * @return the generated {@link Bill} id.
	 * @throws OHServiceException if an error occurs storing the bill.
	 */
	public int newBill(Bill newBill) throws OHServiceException {

		return billRepository.save(newBill).getId();
	}

	/**
	 * Stores a list of {@link BillItems} associated to a {@link Bill}.
	 * @param bill the bill.
	 * @param billItems the bill items to store.
	 * @return <code>true</code> if the {@link BillItems} have been store, <code>false</code> otherwise.
	 * @throws OHServiceException if an error occurs during the store operation.
	 */
	public boolean newBillItems(Bill bill, ArrayList<BillItems> billItems) throws OHServiceException {
		boolean result = true;

		result = _deleteBillsInsideBillItems(bill.getId());

		result &= _insertNewBillInsideBillItems(bill, billItems);

		return result;
	}

	private boolean _deleteBillsInsideBillItems(int id) throws OHServiceException {
		boolean result = true;

		billItemsRepository.deleteWhereId(id);

		return result;
	}

	private boolean _insertNewBillInsideBillItems(Bill bill, ArrayList<BillItems> billItems) throws OHServiceException {
		boolean result = true;

		for (BillItems item : billItems) {
			item.setBill(bill);
			billItemsRepository.save(item);
		}

		return result;
	}

	/**
	 * Stores a list of {@link BillPayments} associated to a {@link Bill}.
	 * @param bill the bill.
	 * @param payItems the bill payments.
	 * @return <code>true</code> if the payment have stored, <code>false</code> otherwise.
	 * @throws OHServiceException if an error occurs during the store procedure.
	 */
	public boolean newBillPayments(Bill bill, ArrayList<BillPayments> payItems) throws OHServiceException {
		boolean result = true;

		result = _deleteBillsInsideBillPayments(bill.getId());

		result &= _insertNewBillInsideBillPayments(bill, payItems);

		return result;
	}

	private boolean _deleteBillsInsideBillPayments(int id) throws OHServiceException {
		boolean result = true;

		billPaymentRepository.deleteWhereId(id);

		return result;
	}

	private boolean _insertNewBillInsideBillPayments(Bill bill, ArrayList<BillPayments> billPayments)
			throws OHServiceException {

		boolean result = true;

		for (BillPayments payment : billPayments) {
			payment.setBill(bill);
			billPaymentRepository.save(payment);
		}

		return result;
	}

	/**
	 * Updates the specified {@link Bill}.
	 * @param updateBill the bill to update.
	 * @return <code>true</code> if the bill has been updated, <code>false</code> otherwise.
	 * @throws OHServiceException if an error occurs during the update.
	 */
	public boolean updateBill(Bill updateBill) throws OHServiceException {
		boolean result = true;

		Bill savedBill = billRepository.save(updateBill);
		result = (savedBill != null);

		return result;
	}

	/**
	 * Deletes the specified {@link Bill}.
	 * @param deleteBill the bill to delete.
	 * @return <code>true</code> if the bill has been deleted, <code>false</code> otherwise.
	 * @throws OHServiceException if an error occurs deleting the bill.
	 */
	public boolean deleteBill(Bill deleteBill) throws OHServiceException {
		boolean result = true;

		billRepository.updateDeleteWhereId(deleteBill.getId());

		return result;
	}

	/**
	 * Retrieves all the {@link Bill}s for the specified date range.
	 * @param dateFrom the low date range endpoint, inclusive. 
	 * @param dateTo the high date range endpoint, inclusive.
	 * @return a list of retrieved {@link Bill}s.
	 * @throws OHServiceException if an error occurs retrieving the bill list.
	 * @deprecated use {@link #getBillsBetweenDates(GregorianCalendar, GregorianCalendar)}
	 */
	@Deprecated
	public ArrayList<Bill> getBills(GregorianCalendar dateFrom, GregorianCalendar dateTo) throws OHServiceException {
		return getBillsBetweenDates(dateFrom, dateTo);
	}

	/**
	 * Retrieves all the {@link Bill}s for the specified date range.
	 * @param dateFrom the low date range endpoint, inclusive.
	 * @param dateTo the high date range endpoint, inclusive.
	 * @return a list of retrieved {@link Bill}s.
	 * @throws OHServiceException if an error occurs retrieving the bill list.
	 */
	public ArrayList<Bill> getBillsBetweenDates(GregorianCalendar dateFrom, GregorianCalendar dateTo)
			throws OHServiceException {
		return new ArrayList<>(billRepository.findByDateBetween(TimeTools.getBeginningOfDay(dateFrom),
				TimeTools.getBeginningOfNextDay(dateTo)));
	}

	/**
	 * Gets all the {@link Bill}s associated to the passed {@link BillPayments}.
	 * @param payments the {@link BillPayments} associated to the bill to retrieve.
	 * @return a list of {@link Bill} associated to the passed {@link BillPayments}.
	 * @throws OHServiceException if an error occurs retrieving the bill list.
	 */
	public ArrayList<Bill> getBills(ArrayList<BillPayments> payments) throws OHServiceException {
		Set<Bill> bills = new TreeSet<>(new Comparator<Bill>() {

			@Override
			public int compare(Bill o1, Bill o2) {
				return o1.getId() == o2.getId() ? 1 : 0;
			}
		});
		for (BillPayments bp : payments) {
			bills.add(bp.getBill());
		}

		return new ArrayList<>(bills);
	}

	/**
	 * Retrieves all the {@link BillPayments} associated to the passed {@link Bill} list.
	 * @param bills the bill list.
	 * @return a list of {@link BillPayments} associated to the passed bill list.
	 * @throws OHServiceException if an error occurs retrieving the payments.
	 */
	public ArrayList<BillPayments> getPayments(ArrayList<Bill> bills) throws OHServiceException {
		return new ArrayList<>(billPaymentRepository.findAllByBillIn(bills));
	}

	/**
	 * Retrieves all billPayments for a given patient in the period dateFrom -> dateTo
	 * @param dateFrom
	 * @param dateTo
	 * @param patient
	 * @return
	 * @throws OHServiceException
	 * @deprecated use {@link #getPaymentsBetweenDatesWherePatient(GregorianCalendar, GregorianCalendar, Patient)}
	 */
	@Deprecated
	public ArrayList<BillPayments> getPayments(GregorianCalendar dateFrom, GregorianCalendar dateTo, Patient patient)
			throws OHServiceException {
		return getPaymentsBetweenDatesWherePatient(dateFrom, dateTo, patient);
	}

	/**
	 * Retrieves all billPayments for a given patient in the period dateFrom -> dateTo
	 * @param dateFrom
	 * @param dateTo
	 * @param patient
	 * @return
	 * @throws OHServiceException
	 */
	public ArrayList<BillPayments> getPaymentsBetweenDatesWherePatient(GregorianCalendar dateFrom,
			GregorianCalendar dateTo, Patient patient) throws OHServiceException {
		ArrayList<BillPayments> payments = billPaymentRepository.findByDateAndPatient(dateFrom, dateTo,
				patient.getCode());
		return payments;
	}

	/**
	 * Retrieves all the bills for a given patient in the period dateFrom -> dateTo
	 * @param dateFrom
	 * @param dateTo
	 * @param patient
	 * @return the bill list
	 * @throws OHServiceException
	 * @deprecated use {@link #getBillsBetweenDatesWherePatient(GregorianCalendar, GregorianCalendar, Patient)}
	 */
	@Deprecated
	public ArrayList<Bill> getBills(GregorianCalendar dateFrom, GregorianCalendar dateTo, Patient patient)
			throws OHServiceException {
		return getBillsBetweenDatesWherePatient(dateFrom, dateTo, patient);
	}

	/**
	 * Retrieves all the bills for a given patient in the period dateFrom -> dateTo
	 * @param dateFrom
	 * @param dateTo
	 * @param patient
	 * @return the bill list
	 * @throws OHServiceException
	 */
	public ArrayList<Bill> getBillsBetweenDatesWherePatient(GregorianCalendar dateFrom, GregorianCalendar dateTo,
			Patient patient) throws OHServiceException {
		ArrayList<Bill> bills = billRepository.findByDateAndPatient(dateFrom, dateTo, patient.getCode());
		return bills;
	}

	/**
	 * 
	 * @param patID
	 * @return
	 * @throws OHServiceException
	 */
	public ArrayList<Bill> getPendingBillsAffiliate(int patID) throws OHServiceException {
		ArrayList<Bill> pendingBills = billRepository.findAllPendindBillsByBillPatient(patID);
		return pendingBills;
	}

	/**
	 *
	 * @param patID
	 * @return
	 * @throws OHServiceException
	 */
	public List<Bill> getAllPatientsBills(int patID) throws OHServiceException {
		return billRepository.findByBillPatientCode(patID);
	}

	/**
	 * Return distinct BillItems
	 * added by u2g
	 * @return BillItems list 
	 * @throws OHServiceException
	 */
	public List<BillItems> getDistictsBillItems() throws OHServiceException {
		return billItemsRepository.findAllGroupByDescription();
	}

	/**
	 * Return the bill list which date between dateFrom and dateTo and containing given billItem
	 *
	 * added by u2g
	 *
	 * @param dateFrom
	 * @param dateTo
	 * @param billItem
	 * @return the bill list
	 * @throws OHServiceException
	 * @deprecated use {@link #getBillsBetweenDatesWhereBillItem(GregorianCalendar, GregorianCalendar, BillItems)}
	 */
	@Deprecated
	public ArrayList<Bill> getBills(GregorianCalendar dateFrom, GregorianCalendar dateTo, BillItems billItem)
			throws OHServiceException {
		return getBillsBetweenDatesWhereBillItem(dateFrom, dateTo, billItem);
	}

	/**
	 * Return the bill list which date between dateFrom and dateTo and containing given billItem
	 *
	 * @param dateFrom
	 * @param dateTo
	 * @param billItem
	 * @return the bill list
	 * @throws OHServiceException
	 */
	public ArrayList<Bill> getBillsBetweenDatesWhereBillItem(GregorianCalendar dateFrom, GregorianCalendar dateTo,
			BillItems billItem) throws OHServiceException {
		ArrayList<Bill> bills = null;
		if (billItem == null) {
			bills = (ArrayList<Bill>) billRepository.findByDateBetween(TimeTools.getBeginningOfDay(dateFrom),
					TimeTools.getBeginningOfNextDay(dateTo));
		} else {
			bills = (ArrayList<Bill>) billRepository.findAllWhereDatesAndBillItem(TimeTools.getBeginningOfDay(dateFrom),
					TimeTools.getBeginningOfNextDay(dateTo), billItem.getItemDescription());
			//for(Bill bill: bills)System.out.println("***************bill****************"+bill.toString());
		}
		return bills;
	}
}
