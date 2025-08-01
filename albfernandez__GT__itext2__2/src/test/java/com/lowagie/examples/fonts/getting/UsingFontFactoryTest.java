/*
 * $Id: UsingFontFactory.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.fonts.getting;

import java.util.Iterator;
import java.util.TreeSet;

import org.junit.Test;

import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfTestBase;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Special rendering of Chunks.
 * 
 * @author blowagie
 */

public class UsingFontFactoryTest {

	/**
	 * Special rendering of Chunks.
	 */
	@Test
	public void main() throws Exception {

		// step 1: creation of a document-object
		Document document = new Document();
		// step 2:
		// we create a writer that listens to the document
		PdfWriter.getInstance(document, PdfTestBase.getOutputStream("FontFactory.pdf"));

		// step 3: we open the document
		document.open();
		// step 4:
		String name;
		Paragraph p = new Paragraph("Font Families", FontFactory.getFont(FontFactory.HELVETICA, 16f));
		document.add(p);
		FontFactory.registerDirectories();
		TreeSet families = new TreeSet(FontFactory.getRegisteredFamilies());
		int c = 0;
		for (Iterator i = families.iterator(); i.hasNext() && c < 15;) {
			name = (String) i.next();
			p = new Paragraph(name);
			document.add(p);
			c++;
		}
		document.newPage();
		String quick = "quick brown fox jumps over the lazy dog";
		p = new Paragraph("Fonts", FontFactory.getFont(FontFactory.HELVETICA, 16f));
		for (Iterator i = families.iterator(); i.hasNext() && c > 0;) {
			name = (String) i.next();
			p = new Paragraph(name);
			document.add(p);
			try {
				p = new Paragraph(quick, FontFactory.getFont(name, BaseFont.WINANSI, BaseFont.EMBEDDED));
				document.add(p);
			} catch (Exception e) {
				document.add(new Paragraph(e.getMessage()));
			}
			c--;
		}

		// step 5: we close the document
		document.close();
	}
}