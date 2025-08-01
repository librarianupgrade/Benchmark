/*
 * $Id: Transparency.java 3838 2009-04-07 18:34:15Z mstorer $
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
package com.lowagie.examples.directcontent.colors;

import java.awt.Color;

import org.junit.Test;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfTestBase;
import com.lowagie.text.pdf.PdfTransparencyGroup;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Demonstrates transparency and images.
 */
public class TransparencyTest {

	/**
	 * Demonstrates the Transparency functionality.
	 */
	@Test
	public void main() throws Exception {
		// step 1: creation of a document-object
		Document document = new Document(PageSize.A4, 50, 50, 50, 50);
		// step 2: creation of a writer
		PdfWriter writer = PdfWriter.getInstance(document, PdfTestBase.getOutputStream("transparency.pdf"));
		// step 3: we open the document
		document.open();
		// step 4: content
		PdfContentByte cb = writer.getDirectContent();
		float gap = (document.getPageSize().getWidth() - 400) / 3;

		pictureBackdrop(gap, 500, cb);
		pictureBackdrop(200 + 2 * gap, 500, cb);
		pictureBackdrop(gap, 500 - 200 - gap, cb);
		pictureBackdrop(200 + 2 * gap, 500 - 200 - gap, cb);

		pictureCircles(gap, 500, cb);
		cb.saveState();
		PdfGState gs1 = new PdfGState();
		gs1.setFillOpacity(0.5f);
		cb.setGState(gs1);
		pictureCircles(200 + 2 * gap, 500, cb);
		cb.restoreState();

		PdfTemplate tp = cb.createTemplate(200, 200);
		cb.saveState();
		pictureCircles(0, 0, tp);
		PdfTransparencyGroup group = new PdfTransparencyGroup();
		tp.setGroup(group);
		tp.sanityCheck();
		cb.setGState(gs1);
		cb.addTemplate(tp, gap, 500 - 200 - gap);
		cb.restoreState();

		tp = cb.createTemplate(200, 200);
		cb.saveState();
		PdfGState gs2 = new PdfGState();
		gs2.setFillOpacity(0.5f);
		gs2.setBlendMode(PdfGState.BM_SOFTLIGHT);
		tp.setGState(gs2);
		tp.sanityCheck();
		pictureCircles(0, 0, tp);
		tp.setGroup(group);
		cb.addTemplate(tp, 200 + 2 * gap, 500 - 200 - gap);
		cb.restoreState();

		cb.resetRGBColorFill();
		ColumnText ct = new ColumnText(cb);
		Phrase ph = new Phrase("Ungrouped objects\nObject opacity = 1.0");
		ct.setSimpleColumn(ph, gap, 0, gap + 200, 500, 18, Element.ALIGN_CENTER);
		ct.go();

		ph = new Phrase("Ungrouped objects\nObject opacity = 0.5");
		ct.setSimpleColumn(ph, 200 + 2 * gap, 0, 200 + 2 * gap + 200, 500, 18, Element.ALIGN_CENTER);
		ct.go();

		ph = new Phrase("Transparency group\nObject opacity = 1.0\nGroup opacity = 0.5\nBlend mode = Normal");
		ct.setSimpleColumn(ph, gap, 0, gap + 200, 500 - 200 - gap, 18, Element.ALIGN_CENTER);
		ct.go();

		ph = new Phrase("Transparency group\nObject opacity = 0.5\nGroup opacity = 1.0\nBlend mode = SoftLight");
		ct.setSimpleColumn(ph, 200 + 2 * gap, 0, 200 + 2 * gap + 200, 500 - 200 - gap, 18, Element.ALIGN_CENTER);
		ct.go();

		cb.sanityCheck();

		// step 5: we close the document
		document.close();
	}

	/**
	 * Prints a square and fills half of it with a gray rectangle.
	 * 
	 * @param x
	 * @param y
	 * @param cb
	 * @throws Exception
	 */
	private static void pictureBackdrop(float x, float y, PdfContentByte cb) throws Exception {
		cb.setColorStroke(Color.black);
		cb.setColorFill(Color.gray);
		cb.rectangle(x, y, 100, 200);
		cb.fill();
		cb.setLineWidth(2);
		cb.rectangle(x, y, 200, 200);
		cb.stroke();
	}

	/**
	 * Prints 3 circles in different colors that intersect with eachother.
	 * 
	 * @param x
	 * @param y
	 * @param cb
	 * @throws Exception
	 */
	private static void pictureCircles(float x, float y, PdfContentByte cb) throws Exception {
		cb.setColorFill(Color.red);
		cb.circle(x + 70, y + 70, 50);
		cb.fill();
		cb.setColorFill(Color.yellow);
		cb.circle(x + 100, y + 130, 50);
		cb.fill();
		cb.setColorFill(Color.blue);
		cb.circle(x + 130, y + 70, 50);
		cb.fill();
	}

}
