/*
 * $Id: RegisterFont.java 3373 2008-05-12 16:21:24Z xlv $
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

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.Iterator;

import org.junit.Test;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfTestBase;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Registering Fonts with the FontFactory.
 */
public class RegisterFontTest {

	/**
	 * Registering fonts with the fontfactory.
	 */
	@Test
	public void main() throws Exception {

		String liberationPath = "src/test/resources/liberation-fonts-ttf/";
		FontFactory.register(liberationPath + "LiberationMono-Regular.ttf");
		FontFactory.register(liberationPath + "LiberationSans-Regular.ttf");
		FontFactory.register(liberationPath + "LiberationSerif-Regular.ttf");

		// step 1: creation of a document-object
		Document document = new Document();

		// step 2: creation of the writer
		PdfWriter.getInstance(document, PdfTestBase.getOutputStream("registerfont.pdf"));

		// step 3: we open the document
		document.open();

		// step 4: we add content to the document
		Font font0 = FontFactory.getFont(BaseFont.HELVETICA, BaseFont.WINANSI, 12);
		String text0 = "This is the quite popular built in font '" + BaseFont.HELVETICA + "'.";
		document.add(new Paragraph(text0, font0));
		Font font1 = FontFactory.getFont("LiberationMono", BaseFont.WINANSI, 12);
		String text1 = "This is the quite popular True Type font 'LiberationMono'.";
		document.add(new Paragraph(text1, font1));
		Font font2 = FontFactory.getFont("LiberationSans-Bold", BaseFont.WINANSI, 12);
		String text2 = "This is the quite popular True Type font 'LiberationSans-Bold'.";
		document.add(new Paragraph(text2, font2));
		Font font3 = FontFactory.getFont("LiberationSerif", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12);
		String text3 = "\u5951\u7d04\u8005\u4f4f\u6240\u30e9\u30a4\u30f3\uff11";
		document.add(new Paragraph(text3, font3));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(PdfTestBase.getOutputStream("registered.txt")));
		out.write("These fonts were registered at the FontFactory:\r\n");
		for (Iterator i = FontFactory.getRegisteredFonts().iterator(); i.hasNext();) {
			out.write((String) i.next());
			out.write("\r\n");
		}
		out.write("\r\n\r\nThese are the families these fonts belong to:\r\n");
		for (Iterator i = FontFactory.getRegisteredFamilies().iterator(); i.hasNext();) {
			out.write((String) i.next());
			out.write("\r\n");
		}
		out.flush();
		out.close();

		// step 5: we close the document
		document.close();

	}
}