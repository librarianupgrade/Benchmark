/*
 * $Id: FontFactoryStyles.java 3373 2008-05-12 16:21:24Z xlv $
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

import java.io.File;

import org.junit.Test;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfTestBase;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Changing the style of a FontFactory Font.
 */
public class FontFactoryStylesTest {

	/**
	 * Changing the style of a FontFactory Font.
	 * 
	 * @param args
	 *            no arguments needed
	 */
	@Test
	public void main() throws Exception {

		// step 1: creation of a document-object
		Document document = new Document();

		// step 2: creation of the writer
		PdfWriter.getInstance(document, PdfTestBase.getOutputStream("fontfactorystyles.pdf"));

		// step 3: we open the document
		document.open();

		String fontPathBase = new File(PdfTestBase.RESOURCES_DIR + "liberation-fonts-ttf").getAbsolutePath();
		// step 4: we add some content
		FontFactory.register(fontPathBase + "/LiberationSans-Regular.ttf");
		FontFactory.register(fontPathBase + "/LiberationSans-Italic.ttf");
		FontFactory.register(fontPathBase + "/LiberationSans-Bold.ttf");
		FontFactory.register(fontPathBase + "/LiberationSans-BoldItalic.ttf");

		Phrase myPhrase = new Phrase("This is font family Liberation Sans ", FontFactory.getFont("LiberationSans", 8));
		myPhrase.add(new Phrase("italic ", FontFactory.getFont("Arial", 8, Font.ITALIC)));
		myPhrase.add(new Phrase("bold ", FontFactory.getFont("Arial", 8, Font.BOLD)));
		myPhrase.add(new Phrase("bolditalic", FontFactory.getFont("Arial", 8, Font.BOLDITALIC)));
		document.add(myPhrase);

		// step 5: we close the document
		document.close();
	}
}
