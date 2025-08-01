/*
 * $Id: Actions.java 3373 2008-05-12 16:21:24Z xlv $
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

import org.junit.Test;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfDestination;
import com.lowagie.text.pdf.PdfTestBase;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Creates a document with some goto actions.
 * 
 * @author blowagie
 */

public class ActionsTest {

	/**
	 * Creates a document with some goto actions.
	 * 
	 */
	@Test
	public void main() throws Exception {

		// step 1: creation of a document-object
		Document document = new Document();
		Document remote = new Document();

		// step 2:
		PdfWriter writer = PdfWriter.getInstance(document, PdfTestBase.getOutputStream("Actions.pdf"));
		PdfWriter.getInstance(remote, PdfTestBase.getOutputStream("remote.pdf"));
		// step 3:
		document.open();
		remote.open();
		// step 4: we add some content
		PdfAction action = PdfAction.gotoLocalPage(2, new PdfDestination(PdfDestination.XYZ, -1, 10000, 0), writer);
		writer.setOpenAction(action);
		document.add(new Paragraph("Page 1"));
		document.newPage();
		document.add(new Paragraph("Page 2"));
		document.add(new Chunk("goto page 1")
				.setAction(PdfAction.gotoLocalPage(1, new PdfDestination(PdfDestination.FITH, 500), writer)));
		document.add(Chunk.NEWLINE);
		document.add(new Chunk("goto another document")
				.setAction(PdfAction.gotoRemotePage("remote.pdf", "test", false, true)));
		remote.add(new Paragraph("Some remote document"));
		remote.newPage();
		Paragraph p = new Paragraph("This paragraph contains a ");
		p.add(new Chunk("local destination").setLocalDestination("test"));
		remote.add(p);

		// step 5: we close the document
		document.close();
		remote.close();
	}
}