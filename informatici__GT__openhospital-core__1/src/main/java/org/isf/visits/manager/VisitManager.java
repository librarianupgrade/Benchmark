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
package org.isf.visits.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.sms.manager.SmsManager;
import org.isf.sms.model.Sms;
import org.isf.sms.service.SmsOperations;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.time.TimeTools;
import org.isf.visits.model.Visit;
import org.isf.visits.service.VisitsIoOperations;
import org.isf.ward.model.Ward;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mwithi
 */
@Component
public class VisitManager {

	@Autowired
	private VisitsIoOperations ioOperations;

	@Autowired
	private SmsOperations smsOp;

	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * Returns the list of all {@link Visit}s related to a patID
	 *
	 * @param patID - the {@link Patient} ID. If <code>0</code> return the list of all {@link Visit}s
	 * @return the list of {@link Visit}s
	 * @throws OHServiceException
	 */
	public ArrayList<Visit> getVisits(int patID) throws OHServiceException {
		return ioOperations.getVisits(patID);
	}

	public ArrayList<Visit> getVisitsWard() throws OHServiceException {
		return getVisitsWard(null);
	}

	public ArrayList<Visit> getVisitsWard(String wardId) throws OHServiceException {
		return ioOperations.getVisitsWard(wardId);
	}

	public Visit newVisit(int visitID, GregorianCalendar date, Patient patient, String note, boolean sms, Ward ward,
			String duration, String service) throws OHServiceException {

		Visit vsRow = new Visit(visitID, date, patient, note, sms, ward, duration, service);
		return newVisit(vsRow);
	}

	/**
	 * Insert a new {@link Visit} for related Patient
	 *
	 * @param visit - the {@link Visit}
	 * @return the visitID
	 * @throws OHServiceException
	 */
	public Visit newVisit(Visit visit) throws OHServiceException {
		return ioOperations.newVisit(visit);
	}

	/**
	 * Inserts or replaces all {@link Visit}s related to a patID
	 *
	 * @param visits - the list of {@link Visit}s related to patID.
	 * @return <code>true</code> if the list has been replaced, <code>false</code> otherwise
	 * @throws OHServiceException
	 */
	@Transactional(rollbackFor = OHServiceException.class)
	public boolean newVisits(ArrayList<Visit> visits) throws OHServiceException {
		if (!visits.isEmpty()) {
			PatientBrowserManager patMan = this.applicationContext.getBean(PatientBrowserManager.class);
			int patID = visits.get(0).getPatient().getCode();
			ioOperations.deleteAllVisits(patID);
			smsOp.deleteByModuleModuleID("visit", String.valueOf(patID));

			for (Visit visit : visits) {

				visit.setVisitID(0); //reset ID in order to persist again (otherwise JPA think data is already persisted)
				int visitID = ioOperations.newVisit(visit).getVisitID();
				if (visitID == 0)
					return false;

				visit.setVisitID(visitID);
				if (visit.isSms()) {
					GregorianCalendar date = (GregorianCalendar) visit.getDate().clone();
					date.add(Calendar.DAY_OF_MONTH, -1);
					if (visit.getDate().after(TimeTools.getDateToday24())) {
						Patient pat = patMan.getPatientById(visit.getPatient().getCode());

						Sms sms = new Sms();
						sms.setSmsDateSched(date.getTime());
						sms.setSmsNumber(pat.getTelephone());
						sms.setSmsText(prepareSmsFromVisit(visit));
						sms.setSmsUser(UserBrowsingManager.getCurrentUser());
						sms.setModule("visit");
						sms.setModuleID(String.valueOf(patID));
						smsOp.saveOrUpdate(sms);
					}
				}
			}
		}
		return true;
	}

	/**
	 * Deletes all {@link Visit}s related to a patID
	 *
	 * @param patID - the {@link Patient} ID
	 * @return <code>true</code> if the list has been deleted, <code>false</code> otherwise
	 * @throws OHServiceException
	 */
	@Transactional(rollbackFor = OHServiceException.class)
	public boolean deleteAllVisits(int patID) throws OHServiceException {
		ioOperations.deleteAllVisits(patID);
		smsOp.deleteByModuleModuleID("visit", String.valueOf(patID));
		return true;
	}

	/**
	 * Builds the {@link Sms} text for the specified {@link Visit}
	 * If the length exceeds {@code SmsManager.MAX_LENGHT} the message will be truncated to
	 * the maximum length.
	 * (example:
	 * "REMINDER: dd/MM/yy - HH:mm:ss - {@link Visit#getNote()}")
	 *
	 * @param visit - the {@link Visit}
	 * @return a string containing the text
	 */
	private String prepareSmsFromVisit(Visit visit) {

		String note = visit.getNote();
		StringBuilder sb = new StringBuilder(MessageBundle.getMessage("angal.common.reminder.txt").toUpperCase())
				.append(": ");
		sb.append(visit.toStringSMS());
		if (note != null && !note.isEmpty()) {
			sb.append(" - ").append(note);
		}
		if (sb.toString().length() > SmsManager.MAX_LENGHT) {
			return sb.substring(0, SmsManager.MAX_LENGHT);
		}
		return sb.toString();
	}

	/**
	 * Returns the {@link Visit} based on visit id
	 *
	 * @param id - the  {@link Visit} id.
	 * @return the {@link Visit}
	 */
	public Visit findVisit(int id) throws OHServiceException {
		return ioOperations.findVisit(id);
	}
}
