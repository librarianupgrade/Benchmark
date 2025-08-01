/*
 * $Id: Vertical.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.fonts.styles;

import java.awt.Color;

import org.junit.Test;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTestBase;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.VerticalText;

/**
 * Writing Vertical Text.
 */
public class VerticalTest {

	static String texts[] = { "Some very long text to check if it wraps (or not).", " In blue.",
			"And now in orange another very long text.", "", "", "" };

	static String encs[] = { "UniJIS-UCS2-V", "Identity-V" };

	/**
	 * @param text
	 * @return converted text
	 */
	public static String convertCid(String text) {
		char cid[] = text.toCharArray();
		for (int k = 0; k < cid.length; ++k) {
			char c = cid[k];
			if (c == '\n')
				cid[k] = '\uff00';
			else
				cid[k] = (char) (c - ' ' + 8720);
		}
		return new String(cid);
	}

	/**
	 * Writing vertical text.
	 */
	@Test
	public void main() throws Exception {
		Document document = new Document(PageSize.A4, 50, 50, 50, 50);
		texts[3] = convertCid(texts[0]);
		texts[4] = convertCid(texts[1]);
		texts[5] = convertCid(texts[2]);
		PdfWriter writer = PdfWriter.getInstance(document, PdfTestBase.getOutputStream("vertical.pdf"));
		int idx = 0;
		document.open();
		PdfContentByte cb = writer.getDirectContent();
		for (int j = 0; j < 2; ++j) {
			BaseFont bf = BaseFont.createFont("KozMinPro-Regular", encs[j], false);
			cb.setRGBColorStroke(255, 0, 0);
			cb.setLineWidth(0);
			float x = 400;
			float y = 700;
			float height = 400;
			float leading = 30;
			int maxLines = 6;
			for (int k = 0; k < maxLines; ++k) {
				cb.moveTo(x - k * leading, y);
				cb.lineTo(x - k * leading, y - height);
			}
			cb.rectangle(x, y, -leading * (maxLines - 1), -height);
			cb.stroke();
			VerticalText vt = new VerticalText(cb);
			vt.setVerticalLayout(x, y, height, maxLines, leading);
			vt.addText(new Chunk(texts[idx++], new Font(bf, 20)));
			vt.addText(new Chunk(texts[idx++], new Font(bf, 20, 0, Color.blue)));
			vt.go();
			vt.setAlignment(Element.ALIGN_RIGHT);
			vt.addText(new Chunk(texts[idx++], new Font(bf, 20, 0, Color.orange)));
			vt.go();
			document.newPage();
		}
		document.close();

	}

}
