/*
 * $Id: FragmentTable.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.objects.tables.pdfptable;

import java.awt.Color;

import org.junit.Test;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTestBase;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Break a large table up into different smaller tables in order to save memory.
 */
public class FragmentTableTest {

	/**
	 * Break a large table up into several smaller tables for memory management
	 * purposes.
	 * 
	 */
	@Test
	public void main() throws Exception {
		int fragmentsize = 50;
		// step1
		Document document = new Document(PageSize.A4.rotate(), 10, 10, 10, 10);
		// step2
		PdfWriter.getInstance(document, PdfTestBase.getOutputStream("FragmentTable.pdf"));
		// step3
		document.open();
		// step4
		Font font = FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK);

		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(100f);

		PdfPCell h1 = new PdfPCell(new Paragraph("Header 1", font));
		PdfPCell h2 = new PdfPCell(new Paragraph("Header 2", font));
		table.setHeaderRows(1);
		table.addCell(h1);
		table.addCell(h2);
		PdfPCell cell;
		for (int row = 1; row <= 2000; row++) {
			if (row % fragmentsize == fragmentsize - 1) {
				document.add(table);
				table.deleteBodyRows();
				table.setSkipFirstHeader(true);
			}
			cell = new PdfPCell(new Paragraph(String.valueOf(row), font));
			table.addCell(cell);
			cell = new PdfPCell(new Paragraph(
					"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Nulla mauris nibh, ultricies nec, adipiscing eget.",
					font));
			table.addCell(cell);
		}
		document.add(table);

		// step5
		document.close();
	}
}