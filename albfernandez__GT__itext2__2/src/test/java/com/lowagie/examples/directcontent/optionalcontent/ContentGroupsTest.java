/*
 * $Id: ContentGroups.java 3838 2009-04-07 18:34:15Z mstorer $
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
package com.lowagie.examples.directcontent.optionalcontent;

import java.awt.Color;

import org.junit.Test;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfLayer;
import com.lowagie.text.pdf.PdfLayerMembership;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfOCProperties;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfTestBase;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Demonstrates how to group optional content.
 */
public class ContentGroupsTest {

	/**
	 * Demonstrates how to group optional content.
	 */
	@Test
	public void main() throws Exception {
		// step 1
		Document document = new Document(PageSize.A4, 50, 50, 50, 50);
		// step 2
		PdfWriter writer = PdfWriter.getInstance(document, PdfTestBase.getOutputStream("contentgroups.pdf"));
		writer.setPdfVersion(PdfWriter.VERSION_1_5);
		writer.setViewerPreferences(PdfWriter.PageModeUseOC);
		// step 3
		document.open();
		// step 4
		PdfContentByte cb = writer.getDirectContent();
		Phrase explanation = new Phrase("Layer grouping", new Font(Font.HELVETICA, 20, Font.BOLD, Color.red));
		ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, explanation, 50, 650, 0);
		PdfLayer l1 = new PdfLayer("Layer 1", writer);
		PdfLayer l2 = new PdfLayer("Layer 2", writer);
		PdfLayer l3 = new PdfLayer("Layer 3", writer);
		PdfLayerMembership m1 = new PdfLayerMembership(writer);
		m1.addMember(l2);
		m1.addMember(l3);
		Phrase p1 = new Phrase("Text in layer 1");
		Phrase p2 = new Phrase("Text in layer 2 or layer 3");
		Phrase p3 = new Phrase("Text in layer 3");
		cb.beginLayer(l1);
		ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, p1, 50, 600, 0);
		cb.endLayer();
		cb.beginLayer(m1);
		ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, p2, 50, 550, 0);
		cb.endLayer();
		cb.beginLayer(l3);
		ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, p3, 50, 500, 0);
		cb.endLayer();
		cb.sanityCheck();

		PdfOCProperties p = writer.getOCProperties();
		PdfArray order = new PdfArray();
		order.add(l1.getRef());
		PdfArray group = new PdfArray();
		group.add(new PdfString("A group of two", PdfObject.TEXT_UNICODE));
		group.add(l2.getRef());
		group.add(l3.getRef());
		order.add(group);
		PdfDictionary d = new PdfDictionary();
		d.put(PdfName.ORDER, order);
		p.put(PdfName.D, d);
		// step 5
		document.close();
	}
}