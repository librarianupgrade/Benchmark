/*
 * $Id: ArabicText.java 3838 2009-04-07 18:34:15Z mstorer $
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
package com.lowagie.examples.directcontent.graphics2D;

import org.junit.Test;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTestBase;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Draws arabic text using java.awt.Graphics2D
 */
public class ArabicTextTest {

	/**
	 * Draws arabic text using java.awt.Graphics2D.
	 */
	@Test
	public void main() throws Exception {
		// step 1
		Document document = new Document(PageSize.A4, 50, 50, 50, 50);
		try {
			// step 2
			PdfWriter writer = PdfWriter.getInstance(document, PdfTestBase.getOutputStream("arabictext.pdf"));
			// step 3
			document.open();
			// step 4
			String text1 = "This text has \u0634\u0627\u062f\u062c\u0645\u0647\u0648\u0631 123,456 \u0645\u0646 (Arabic)";
			java.awt.Font font = new java.awt.Font("arial", 0, 18);
			PdfContentByte cb = writer.getDirectContent();
			java.awt.Graphics2D g2 = cb.createGraphicsShapes(PageSize.A4.getWidth(), PageSize.A4.getHeight());
			g2.setFont(font);
			g2.drawString(text1, 100, 100);
			g2.dispose();
			cb.sanityCheck();
			// step 5
			document.close();
		} catch (Exception de) {
			de.printStackTrace();
		}
	}

}