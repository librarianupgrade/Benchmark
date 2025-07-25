/*
 * $Id: SimpleRegistrationForm.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is part of the 'iText Tutorial'.
 * You can find the complete tutorial at the following address:
 * http://itextdocs.lowagie.com/tutorial/
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * itext-questions@lists.sourceforge.net
 */
package com.lowagie.examples.forms;

import org.junit.Test;

import com.lowagie.text.Document;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfFormField;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTestBase;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.TextField;

/**
 * General example using TableEvents and CellEvents.
 */
public class SimpleRegistrationFormTest implements PdfPCellEvent {

	/** the writer with the acroform */
	private PdfWriter writer;

	/** the current fieldname */
	private String fieldname = "NoName";

	/**
	 * Construct an implementation of PdfPCellEvent.
	 * 
	 * @param writer
	 *            the writer with the Acroform that will have to hold the
	 *            fields.
	 */
	public SimpleRegistrationFormTest() {
		super();
	}

	/**
	 * @see com.lowagie.text.pdf.PdfPCellEvent#cellLayout(com.lowagie.text.pdf.PdfPCell,
	 *      com.lowagie.text.Rectangle, com.lowagie.text.pdf.PdfContentByte[])
	 */
	public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
		TextField tf = new TextField(writer, position, fieldname);
		tf.setFontSize(12);
		try {
			PdfFormField field = tf.getTextField();
			writer.addAnnotation(field);
		} catch (Exception e) {
			throw new ExceptionConverter(e);
		}
	}

	/**
	 * Example originally written by Wendy Smoak to generate a Table with
	 * 'floating boxes'. Adapted by Bruno Lowagie.
	 * 
	 */
	@Test
	public void main() throws Exception {
		// step 1
		Document document = new Document();

		// step 2

		PdfWriter writer = PdfWriter.getInstance(document, PdfTestBase.getOutputStream("SimpleRegistrationForm.pdf"));
		// step 3
		document.open();
		// step 4
		PdfPTable table = new PdfPTable(2);
		PdfPCell cell;
		table.getDefaultCell().setPadding(5f);

		table.addCell("Your name:");
		cell = new PdfPCell();
		cell.setCellEvent(createNewForm(writer, "name"));
		table.addCell(cell);

		table.addCell("Your home address:");
		cell = new PdfPCell();
		cell.setCellEvent(createNewForm(writer, "address"));
		table.addCell(cell);

		table.addCell("Postal code:");
		cell = new PdfPCell();
		cell.setCellEvent(createNewForm(writer, "postal_code"));
		table.addCell(cell);

		table.addCell("Your email address:");
		cell = new PdfPCell();
		cell.setCellEvent(createNewForm(writer, "email"));
		table.addCell(cell);

		document.add(table);

		// step 5
		document.close();
	}

	private static SimpleRegistrationFormTest createNewForm(PdfWriter writer, String fieldName) {
		SimpleRegistrationFormTest form = new SimpleRegistrationFormTest();
		form.writer = writer;
		form.fieldname = fieldName;
		return form;
	}
}