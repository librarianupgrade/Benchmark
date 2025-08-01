/*
 * $Id: ChunkColor.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.objects.chunk;

import java.awt.Color;

import org.junit.Test;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTestBase;
import com.lowagie.text.pdf.PdfWriter;

/**
 * How to change the color of a font.
 * 
 * @author blowagie
 */

public class ChunkColorTest {

	/**
	 * Changing Font colors
	 * 
	 */
	@Test
	public void main() throws Exception {
		// step 1: creation of a document-object
		Document document = new Document();
		// step 2:
		// we create a writer that listens to the document
		PdfWriter writer = PdfWriter.getInstance(document, PdfTestBase.getOutputStream("ChunkColor.pdf"));

		// step 3: we open the document
		document.open();
		// step 4:
		Font red = FontFactory.getFont(FontFactory.HELVETICA, Font.DEFAULTSIZE, Font.BOLD, new Color(0xFF, 0x00, 0x00));
		Font blue = FontFactory.getFont(FontFactory.HELVETICA, Font.DEFAULTSIZE, Font.ITALIC,
				new Color(0x00, 0x00, 0xFF));
		Paragraph p;
		p = new Paragraph("Roses are ");
		p.add(new Chunk("red", red));
		document.add(p);
		p = new Paragraph("Violets are ");
		p.add(new Chunk("blue", blue));
		document.add(p);
		BaseFont bf = FontFactory.getFont(FontFactory.COURIER).getCalculatedBaseFont(false);
		PdfContentByte cb = writer.getDirectContent();
		cb.beginText();
		cb.setColorFill(new Color(0x00, 0xFF, 0x00));
		cb.setFontAndSize(bf, 12);
		cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "Grass is green", 250, 700, 0);
		cb.endText();

		// step 5: we close the document
		document.close();
	}
}