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
package org.isf.accounting.manager;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.isf.accounting.model.Bill;
import org.isf.accounting.model.BillItems;
import org.isf.accounting.model.BillPayments;
import org.isf.accounting.service.AccountingIoOperations;
import org.isf.generaldata.MessageBundle;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHDataValidationException;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.isf.utils.exception.model.OHSeverityLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BillBrowserManager {

	@Autowired
	private AccountingIoOperations ioOperations;

	public BillBrowserManager() {

	}

	public BillBrowserManager(AccountingIoOperations ioOperations) {
		if (ioOperations != null)
			this.ioOperations = ioOperations;
	}

	/**
	 * Verify if the object is valid for CRUD and return a list of errors, if any
	 * @param bill
	 * @param billItems
	 * @param billPayments
	 * @throws OHDataValidationException
	 */
	protected void validateBill(Bill bill, ArrayList<BillItems> billItems, ArrayList<BillPayments> billPayments)
			throws OHDataValidationException {
		List<OHExceptionMessage> errors = new ArrayList<>();

		GregorianCalendar today = new GregorianCalendar();
		GregorianCalendar upDate = new GregorianCalendar();
		GregorianCalendar firstPay = new GregorianCalendar();
		GregorianCalendar lastPay = new GregorianCalendar();
		// ensure all the times are exactly the same in case constructor generates different value
		upDate.setTime(today.getTime());
		firstPay.setTime(today.getTime());
		lastPay.setTime(today.getTime());

		GregorianCalendar billDate = bill.getDate();
		if (!billPayments.isEmpty()) {
			firstPay = billPayments.get(0).getDate();
			lastPay = billPayments.get(billPayments.size() - 1).getDate(); //most recent payment
			upDate = lastPay;
		} else {
			upDate = billDate;
		}
		bill.setUpdate(upDate);

		if (billDate.after(today)) {
			errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
					MessageBundle.getMessage("angal.newbill.billsinthefuturearenotallowed.msg"),
					OHSeverityLevel.ERROR));
		}
		if (lastPay.after(today)) {
			errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
					MessageBundle.getMessage("angal.newbill.payementsinthefuturearenotallowed.msg"),
					OHSeverityLevel.ERROR));
		}
		if (billDate.after(firstPay)) {
			errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
					MessageBundle.getMessage("angal.newbill.billdateaisfterthefirstpayment.msg"),
					OHSeverityLevel.ERROR));
		}
		if (bill.getPatName().isEmpty()) {
			errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
					MessageBundle.getMessage("angal.newbill.pleaseinsertanameforthepatient.msg"),
					OHSeverityLevel.ERROR));
		}
		if (bill.getStatus().equals("C") && bill.getBalance() != 0) {
			errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.common.error.title"),
					MessageBundle.getMessage("angal.newbill.abillwithanoutstandingbalancecannotbeclosed.msg"),
					OHSeverityLevel.ERROR));
		}
		if (!errors.isEmpty()) {
			throw new OHDataValidationException(errors);
		}
	}

	/**
	 * Returns all the stored {@link BillItems}.
	 * @return a list of {@link BillItems} or null if an error occurs.
	 * @throws OHServiceException 
	 * @deprecated this method should always be called with a parameter.
	 * See {@link #getItems(int) getItems} method.
	 */
	@Deprecated
	public ArrayList<BillItems> getItems() throws OHServiceException {
		return ioOperations.getItems(0);
	}

	/**
	 * Retrieves all the {@link BillItems} associated to the passed {@link Bill} id.
	 * @param billID the bill id.
	 * @return a list of {@link BillItems} or <code>null</code> if an error occurred.
	 * @throws OHServiceException 
	 */
	public ArrayList<BillItems> getItems(int billID) throws OHServiceException {
		if (billID == 0)
			return new ArrayList<>();
		return ioOperations.getItems(billID);
	}

	/**
	 * Retrieves all the bills of a given patient between dateFrom and datTo
	 * @param dateFrom
	 * @param dateTo
	 * @param patient
	 * @return the bills list
	 * @throws OHServiceException 
	 */
	public ArrayList<Bill> getBills(GregorianCalendar dateFrom, GregorianCalendar dateTo, Patient patient)
			throws OHServiceException {
		return ioOperations.getBillsBetweenDatesWherePatient(dateFrom, dateTo, patient);
	}

	/**
	 * Retrieves all the billPayments for a given patient between dateFrom and dateTo
	 * @param dateFrom
	 * @param dateTo
	 * @param patient
	 * @return the list of payments
	 * @throws OHServiceException 
	 */
	public ArrayList<BillPayments> getPayments(GregorianCalendar dateFrom, GregorianCalendar dateTo, Patient patient)
			throws OHServiceException {
		return ioOperations.getPaymentsBetweenDatesWherePatient(dateFrom, dateTo, patient);
	}

	/**
	 * Retrieves all the stored {@link BillPayments}.
	 * @return a list of bill payments or <code>null</code> if an error occurred.
	 * @throws OHServiceException
	 * @deprecated this method should always be called with a parameter.
	 * See {@link #getPayments(int) getPayments} method.
	 */
	@Deprecated
	public ArrayList<BillPayments> getPayments() throws OHServiceException {
		return ioOperations.getPayments(0);
	}

	/**
	 * Gets all the {@link BillPayments} for the specified {@link Bill}.
	 * @param billID the bill id.
	 * @return a list of {@link BillPayments} or <code>null</code> if an error occurred.
	 * @throws OHServiceException 
	 */
	public ArrayList<BillPayments> getPayments(int billID) throws OHServiceException {
		if (billID == 0)
			return new ArrayList<>();
		return ioOperations.getPayments(billID);
	}

	/**
	 * Stores a new {@link Bill} along with all its {@link BillItems} and {@link BillPayments}
	 * @param newBill - the bill to store.
	 * @param billItems - the list of bill's items
	 * @param billPayments - the list of bill's payments
	 * @return <code>true</code> if the bill has been stored, <code>false</code> otherwise.
	 * @throws OHServiceException 
	 */
	@Transactional(rollbackFor = OHServiceException.class)
	public boolean newBill(Bill newBill, ArrayList<BillItems> billItems, ArrayList<BillPayments> billPayments)
			throws OHServiceException {
		validateBill(newBill, billItems, billPayments);
		int billId = newBill(newBill);
		boolean result = billId > 0;
		if (!billItems.isEmpty())
			result = newBillItems(billId, billItems);
		if (!billPayments.isEmpty())
			result = result && newBillPayments(billId, billPayments);
		return result;
	}

	/**
	 * Stores a new {@link Bill}.
	 * @param newBill the bill to store.
	 * @return the generated id.
	 * @throws OHServiceException
	 */
	private int newBill(Bill newBill) throws OHServiceException {
		return ioOperations.newBill(newBill);
	}

	/**
	 * Stores a list of {@link BillItems} associated to a {@link Bill}.
	 * @param billID the bill id.
	 * @param billItems the bill items to store.
	 * @return <code>true</code> if the {@link BillItems} have been store, <code>false</code> otherwise.
	 * @throws OHServiceException
	 */
	private boolean newBillItems(int billID, ArrayList<BillItems> billItems) throws OHServiceException {
		return ioOperations.newBillItems(ioOperations.getBill(billID), billItems);
	}

	/**
	 * Stores a list of {@link BillPayments} associated to a {@link Bill}.
	 * @param billID the bill id.
	 * @param payItems the bill payments.
	 * @return <code>true</code> if the payments have been stored, <code>false</code> otherwise.
	 * @throws OHServiceException
	 */
	private boolean newBillPayments(int billID, ArrayList<BillPayments> payItems) throws OHServiceException {
		return ioOperations.newBillPayments(ioOperations.getBill(billID), payItems);
	}

	/**
	 * Updates the specified {@link Bill} along with all its {@link BillItems} and {@link BillPayments}
	 * @param updateBill - the bill to update.
	 * @param billItems - the list of bill's items
	 * @param billPayments - the list of bill's payments
	 * @return <code>true</code> if the bill has been updated, <code>false</code> otherwise.
	 * @throws OHServiceException
	 */
	@Transactional(rollbackFor = OHServiceException.class)
	public boolean updateBill(Bill updateBill, ArrayList<BillItems> billItems, ArrayList<BillPayments> billPayments)
			throws OHServiceException {
		validateBill(updateBill, billItems, billPayments);
		boolean result = updateBill(updateBill);
		result = result && newBillItems(updateBill.getId(), billItems);
		result = result && newBillPayments(updateBill.getId(), billPayments);
		return result;

	}

	/**
	 * Updates the specified {@link Bill}.
	 * @param updateBill the bill to update.
	 * @return <code>true</code> if the bill has been updated, <code>false</code> otherwise.
	 * @throws OHServiceException 
	 */
	private boolean updateBill(Bill updateBill) throws OHServiceException {
		return ioOperations.updateBill(updateBill);
	}

	/**
	 * Returns all the pending {@link Bill}s for the specified patient.
	 * @param patID the patient id.
	 * @return the list of pending bills or <code>null</code> if an error occurred.
	 * @throws OHServiceException 
	 */
	public ArrayList<Bill> getPendingBills(int patID) throws OHServiceException {
		return ioOperations.getPendingBills(patID);
	}

	/**
	 * Get all the {@link Bill}s.
	 * @return a list of bills or <code>null</code> if an error occurred.
	 * @throws OHServiceException
	 * @deprecated this method should not be called for its potentially huge resultset
	 */
	@Deprecated
	public ArrayList<Bill> getBills() throws OHServiceException {
		return ioOperations.getBills();
	}

	/**
	 * Get the {@link Bill} with specified billID
	 * @param billID
	 * @return the {@link Bill} or <code>null</code> if an error occurred.
	 * @throws OHServiceException 
	 */
	public Bill getBill(int billID) throws OHServiceException {
		return ioOperations.getBill(billID);
	}

	/**
	 * Returns all user ids related to a {@link BillPayments}.
	 * @return a list of user id or <code>null</code> if an error occurred.
	 * @throws OHServiceException 
	 */
	public ArrayList<String> getUsers() throws OHServiceException {
		return ioOperations.getUsers();
	}

	/**
	 * Deletes the specified {@link Bill}.
	 * @param deleteBill the bill to delete.
	 * @return <code>true</code> if the bill has been deleted, <code>false</code> otherwise.
	 * @throws OHServiceException 
	 */
	public boolean deleteBill(Bill deleteBill) throws OHServiceException {
		return ioOperations.deleteBill(deleteBill);
	}

	/**
	 * Retrieves all the {@link Bill}s for the specified date range.
	 * @param dateFrom the low date range endpoint, inclusive. 
	 * @param dateTo the high date range endpoint, inclusive.
	 * @return a list of retrieved {@link Bill}s or <code>null</code> if an error occurred.
	 * @throws OHServiceException 
	 */
	public ArrayList<Bill> getBills(GregorianCalendar dateFrom, GregorianCalendar dateTo) throws OHServiceException {
		return ioOperations.getBillsBetweenDates(dateFrom, dateTo);
	}

	/**
	 * Gets all the {@link Bill}s associated to the passed {@link BillPayments}.
	 * @param billPayments the {@link BillPayments} associated to the bill to retrieve.
	 * @return a list of {@link Bill} associated to the passed {@link BillPayments} or <code>null</code> if an error occurred.
	 * @throws OHServiceException 
	 */
	public ArrayList<Bill> getBills(ArrayList<BillPayments> billPayments) throws OHServiceException {
		if (billPayments.isEmpty())
			return new ArrayList<>();
		return ioOperations.getBills(billPayments);
	}

	/**
	 * Retrieves all the {@link BillPayments} for the specified date range.
	 * @param dateFrom low endpoint, inclusive, for the date range. 
	 * @param dateTo high endpoint, inclusive, for the date range.
	 * @return a list of {@link BillPayments} for the specified date range or <code>null</code> if an error occurred.
	 * @throws OHServiceException 
	 */
	public ArrayList<BillPayments> getPayments(GregorianCalendar dateFrom, GregorianCalendar dateTo)
			throws OHServiceException {
		return ioOperations.getPayments(dateFrom, dateTo);
	}

	/**
	 * Retrieves all the {@link BillPayments} associated to the passed {@link Bill} list.
	 * @param billArray the bill array list of {@link Bill}s.
	 * @return a list of {@link BillPayments} associated to the passed bill list or <code>null</code> if an error occurred. 
	 * @throws OHServiceException 
	 */
	public ArrayList<BillPayments> getPayments(ArrayList<Bill> billArray) throws OHServiceException {
		return ioOperations.getPayments(billArray);
	}

	/**
	 * Retrieves all the {@link Bill}s associated to the specified {@link Patient}.
	 * @param patID - the Patient's ID
	 * @return the list of {@link Bill}s
	 * @throws OHServiceException 
	 */
	public ArrayList<Bill> getPendingBillsAffiliate(int patID) throws OHServiceException {
		return ioOperations.getPendingBillsAffiliate(patID);
	}

	/**
	 * Returns all the distinct stored {@link BillItems}.
	 * 
	 * @return a list of  distinct {@link BillItems} or null if an error occurs.
	 * @throws OHServiceException 
	 */
	public List<BillItems> getDistinctItems() throws OHServiceException {
		return ioOperations.getDistictsBillItems();
	}

	/**
	 * Get the bills list with a given billItem
	 * @param dateFrom
	 * @param dateTo
	 * @param billItem
	 * @return
	 * @throws OHServiceException 
	 */
	public ArrayList<Bill> getBills(GregorianCalendar dateFrom, GregorianCalendar dateTo, BillItems billItem)
			throws OHServiceException {
		return ioOperations.getBillsBetweenDatesWhereBillItem(dateFrom, dateTo, billItem);
	}
}
