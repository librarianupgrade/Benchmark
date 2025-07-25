/*
 * $Id: ListEncodings.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.fonts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.junit.Test;

import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfTestBase;

/**
 * Listing the encodings of font comic
 */
public class ListEncodingsTest {

	/**
	 * Listing the encodings of font comic.
	 * 
	 * @param args
	 *            no arguments needed
	 */
	@Test
	public void main() throws Exception {

		File font = new File(PdfTestBase.RESOURCES_DIR + "liberation-fonts-ttf/LiberationMono-Regular.ttf");
		BufferedWriter out = new BufferedWriter(new FileWriter(PdfTestBase.OUTPUT_DIR + "encodings.txt"));
		BaseFont bfComic = BaseFont.createFont(font.getAbsolutePath(), BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
		out.write("postscriptname: " + bfComic.getPostscriptFontName());
		out.write("\r\n\r\n");
		String[] codePages = bfComic.getCodePagesSupported();
		out.write("All available encodings:\n\n");
		for (int i = 0; i < codePages.length; i++) {
			out.write(codePages[i]);
			out.write("\r\n");
		}
		out.flush();
		out.close();

	}
}
