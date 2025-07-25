/*
 * $Id: TrueType.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.fonts;

import java.io.File;

import org.junit.Test;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfTestBase;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Using a True Type Font.
 */
public class TrueTypeTest {

	/**
	 * Using a True Type Font.
	 */
	@Test
	public void main() throws Exception {

		// step 1: creation of a document-object
		Document document = new Document();

		// step 2:
		// we create a writer that listens to the document
		// and directs a PDF-stream to a file
		PdfWriter.getInstance(document, PdfTestBase.getOutputStream("truetype.pdf"));

		// step 3: we open the document
		document.open();

		String f = new File(PdfTestBase.RESOURCES_DIR + "liberation-fonts-ttf/LiberationMono-Regular.ttf")
				.getAbsolutePath();
		// step 4: we add content to the document
		BaseFont bfComic = BaseFont.createFont(f, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
		Font font = new Font(bfComic, 12);
		String text1 = "This is the quite popular Liberation Mono.";
		document.add(new Paragraph(text1, font));

		// step 5: we close the document
		document.close();
	}
}
