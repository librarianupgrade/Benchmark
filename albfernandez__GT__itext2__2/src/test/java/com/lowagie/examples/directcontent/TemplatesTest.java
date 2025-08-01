/*
 * $Id: Templates.java 3838 2009-04-07 18:34:15Z mstorer $
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
package com.lowagie.examples.directcontent;

import org.junit.Test;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfTestBase;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Demonstrates the use of PdfTemplate.
 */
public class TemplatesTest {

	/**
	
	 */
	@Test
	public void main() throws Exception {

		// step 1: creation of a document-object
		Document document = new Document();

		// step 2:
		// we create a writer that listens to the document
		// and directs a PDF-stream to a file
		PdfWriter writer = PdfWriter.getInstance(document, PdfTestBase.getOutputStream("templates.pdf"));

		// step 3: we open the document
		document.open();

		// step 4: we grab the ContentByte and do some stuff with it
		PdfContentByte cb = writer.getDirectContent();

		// we create a PdfTemplate
		PdfTemplate template = cb.createTemplate(500, 200);

		// we add some graphics
		template.moveTo(0, 200);
		template.lineTo(500, 0);
		template.stroke();
		template.setRGBColorStrokeF(255f, 0f, 0f);
		template.circle(250f, 100f, 80f);
		template.stroke();

		// we add some text
		template.beginText();
		BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
		template.setFontAndSize(bf, 12);
		template.setTextMatrix(100, 100);
		template.showText("Text at the position 100,100 (relative to the template!)");
		template.endText();
		template.sanityCheck();

		// we add the template on different positions
		cb.addTemplate(template, 0, 0);
		cb.addTemplate(template, 0, 1, -1, 0, 500, 200);
		cb.addTemplate(template, .5f, 0, 0, .5f, 100, 400);

		// we go to a new page
		document.newPage();
		cb.addTemplate(template, 0, 400);
		cb.addTemplate(template, 2, 0, 0, 2, -200, 400);
		cb.sanityCheck();

		// step 5: we close the document
		document.close();
	}

}