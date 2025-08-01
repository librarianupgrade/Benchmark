/*
 * $Id: HelloSystemOut.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.general;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfTestBase;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Generates a simple 'Hello World' PDF file.
 * 
 * @author blowagie
 */

public class HelloSystemOutTest {

	/**
	 * Generates a PDF file with the text 'Hello World'
	 * 
	 */
	@Test
	public void main() throws Exception {

		// step 1: creation of a document-object
		Document document = new Document();
		// step 2:
		// we create a writer that listens to the document
		// and directs a PDF-stream to System.out (and a txt file)
		//		PdfWriter w = PdfWriter.getInstance(document, System.out);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfWriter w = PdfWriter.getInstance(document, baos);
		w.setCloseStream(false); // System.out should not be closed
		PdfWriter.getInstance(document, PdfTestBase.getOutputStream("HelloWorldOut.pdf"));

		// step 3: we open the document
		document.open();
		// step 4: we add a paragraph to the document
		document.add(new Paragraph("Hello World"));

		// step 5: we close the document
		document.close();
	}
}
