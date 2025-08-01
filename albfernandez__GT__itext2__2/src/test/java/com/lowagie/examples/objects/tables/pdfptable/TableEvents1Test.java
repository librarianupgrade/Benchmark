/*
 * $Id: TableEvents1.java 3373 2008-05-12 16:21:24Z xlv $
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

import org.junit.Test;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPTableEvent;
import com.lowagie.text.pdf.PdfTestBase;
import com.lowagie.text.pdf.PdfWriter;

/**
 * General example using TableEvents.
 */
public class TableEvents1Test implements PdfPTableEvent {
	/**
	 * @see com.lowagie.text.pdf.PdfPTableEvent#tableLayout(com.lowagie.text.pdf.PdfPTable,
	 *      float[][], float[], int, int, com.lowagie.text.pdf.PdfContentByte[])
	 */
	public void tableLayout(PdfPTable table, float[][] width, float[] heights, int headerRows, int rowStart,
			PdfContentByte[] canvases) {

		// widths of the different cells of the first row
		float widths[] = width[0];

		PdfContentByte cb = canvases[PdfPTable.TEXTCANVAS];
		cb.saveState();
		// border for the complete table
		cb.setLineWidth(2);
		cb.setRGBColorStroke(255, 0, 0);
		cb.rectangle(widths[0], heights[heights.length - 1], widths[widths.length - 1] - widths[0],
				heights[0] - heights[heights.length - 1]);
		cb.stroke();

		// border for the header rows
		if (headerRows > 0) {
			cb.setRGBColorStroke(0, 0, 255);
			cb.rectangle(widths[0], heights[headerRows], widths[widths.length - 1] - widths[0],
					heights[0] - heights[headerRows]);
			cb.stroke();
		}
		cb.restoreState();

		cb = canvases[PdfPTable.BASECANVAS];
		cb.saveState();
		// border for the cells
		cb.setLineWidth(.5f);
		// loop over the rows
		for (int line = 0; line < heights.length - 1; ++line) {
			// loop over the columns
			for (int col = 0; col < widths.length - 1; ++col) {
				if (line == 0 && col == 0)
					cb.setAction(new PdfAction("http://www.lowagie.com/iText/"), widths[col], heights[line + 1],
							widths[col + 1], heights[line]);
				cb.setRGBColorStrokeF((float) Math.random(), (float) Math.random(), (float) Math.random());
				// horizontal borderline
				cb.moveTo(widths[col], heights[line]);
				cb.lineTo(widths[col + 1], heights[line]);
				cb.stroke();
				// vertical borderline
				cb.setRGBColorStrokeF((float) Math.random(), (float) Math.random(), (float) Math.random());
				cb.moveTo(widths[col], heights[line]);
				cb.lineTo(widths[col], heights[line + 1]);
				cb.stroke();
			}
		}
		cb.restoreState();
	}

	/**
	 * General example using table events.
	 */
	@Test
	public void main() throws Exception {
		// step1
		Document document = new Document(PageSize.A4, 50, 50, 50, 50);
		// step2
		PdfWriter writer = PdfWriter.getInstance(document, PdfTestBase.getOutputStream("TableEvents1.pdf"));
		// step3
		document.open();
		// step4
		PdfPTable table = new PdfPTable(4);
		table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		for (int k = 0; k < 24; ++k) {
			if (k != 0)
				table.addCell(String.valueOf(k));
			else
				table.addCell("This is an URL");
		}
		table.setTableEvent(this);

		// add the table with document add
		document.add(table);
		// add the table at an absolute position
		table.setTotalWidth(300);
		table.writeSelectedRows(0, -1, 100, 600, writer.getDirectContent());

		document.newPage();

		table = new PdfPTable(4);
		float fontSize = 12;
		BaseFont bf = BaseFont.createFont("Helvetica", "winansi", false);
		table.getDefaultCell().setPaddingTop(bf.getFontDescriptor(BaseFont.ASCENT, fontSize) - fontSize + 2);
		table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		for (int k = 0; k < 500 * 4; ++k) {
			if (k == 0)
				table.addCell(new Phrase("This is an URL", new Font(bf, fontSize)));
			else
				table.addCell(new Phrase(String.valueOf(k), new Font(bf, fontSize)));
		}
		table.setTableEvent(this);
		table.setHeaderRows(3);
		document.add(table);

		// step5
		document.close();
	}
}