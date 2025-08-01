/*
 * $Id: LocalGoto.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.objects.anchors;

import java.awt.Color;

import org.junit.Test;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfTestBase;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Creates a document with a Local Goto and a Local Destination.
 * 
 * @author blowagie
 */

public class LocalGotoTest {

	/**
	 * Creates a document with a Local Goto and a Local Destination.
	 * 
	 */
	@Test
	public void main() throws Exception {

		// step 1: creation of a document-object
		Document document = new Document();

		// step 2:
		PdfWriter.getInstance(document, PdfTestBase.getOutputStream("LocalGoto.pdf"));

		// step 3: we open the document
		document.open();

		// step 4:

		// we make some content

		// a paragraph with a local goto
		Paragraph p1 = new Paragraph("We will do something special with this paragraph. If you click on ",
				FontFactory.getFont(FontFactory.HELVETICA, 12));
		p1.add(new Chunk("this word", FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, new Color(0, 0, 255)))
				.setLocalGoto("test"));
		p1.add(" you will automatically jump to another location in this document.");

		// some paragraph
		Paragraph p2 = new Paragraph("blah, blah, blah");

		// a paragraph with a local destination
		Paragraph p3 = new Paragraph("This paragraph contains a ");
		p3.add(new Chunk("local destination",
				FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, new Color(0, 255, 0)))
				.setLocalDestination("test"));

		// we add the content
		document.add(p1);
		document.add(p2);
		document.add(p2);
		document.add(p2);
		document.add(p2);
		document.add(p2);
		document.add(p2);
		document.add(p2);
		document.add(p3);

		// step 5: we close the document
		document.close();
	}
}