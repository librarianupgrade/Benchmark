/*
 * $Id: MultiColumnR2L.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.objects.columns;

import java.awt.Color;
import java.io.OutputStream;
import java.util.Random;

import org.junit.Test;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.MultiColumnText;
import com.lowagie.text.pdf.PdfTestBase;
import com.lowagie.text.pdf.PdfWriter;

/**
 * An example using MultiColumnText with irregular columns.
 */
public class MultiColumnR2LTest {

	/**
	 * An example using MultiColumnText with irregular columns.
	 */
	@Test
	public void main() throws Exception {
		Document document = new Document();
		OutputStream out = PdfTestBase.getOutputStream("multicolumnR2L.pdf");
		PdfWriter.getInstance(document, out);
		document.open();

		MultiColumnText mct = new MultiColumnText();
		mct.setColumnsRightToLeft(true);

		// set up 3 even columns with 10pt space between
		mct.addRegularColumns(document.left(), document.right(), 10f, 3);

		// Write some iText poems
		for (int i = 0; i < 30; i++) {
			mct.addElement(new Paragraph(String.valueOf(i + 1)));
			mct.addElement(newPara(randomWord(noun), Element.ALIGN_CENTER, Font.BOLDITALIC));
			for (int j = 0; j < 4; j++) {
				mct.addElement(newPara(poemLine(), Element.ALIGN_LEFT, Font.NORMAL));
			}
			mct.addElement(newPara(randomWord(adverb), Element.ALIGN_LEFT, Font.NORMAL));
			mct.addElement(newPara("\n\n", Element.ALIGN_LEFT, Font.NORMAL));
		}

		document.add(mct);
		document.close();

	}

	private static Element newPara(String text, int alignment, int type) {
		Font font = FontFactory.getFont("Helvetica", 10, type, Color.BLACK);
		Paragraph p = new Paragraph(text, font);
		p.setAlignment(alignment);
		p.setLeading(font.getSize() * 1.2f);
		return p;
	}

	static Random rand = new Random();

	static String[] verb = { "flows", "draws", "renders", "throws exception", "runs", "crashes", "downloads", "usurps",
			"vexes", "whispers", "boils", "capitulates", "crashes", "craves", "looks", "defies", "defers", "defines",
			"envelops", "entombs", "falls", "fails", "halts", "appears", "nags", "overflows", "burns", "dies", "writes",
			"flushes" };

	static String[] noun = { "ColumnText", "paragraph", "phrase", "chunk", "PdfContentByte", "PdfPTable", "iText",
			"color", "vertical alignment", "horizontal alignment", "PdfWriter", "ListItem", "PdfStamper", "PDF", "HTML",
			"XML", "column", "font", "table", "FDF", "field", "NullPointerException", "CJK font" };

	static String[] adjective = { "foul", "broken", "gray", "slow", "beautiful", "throbbing", "sharp", "stout",
			"soundless", "neat", "swift", "uniform", "upright", "vibrant", "dingy", "vestigal", "messy", "sloppy",
			"baleful", "boastful", "dark", "capricious", "concrete", "deliberate", "sharp", "drunken", "undisciplined",
			"perfect", "bloated" };

	static String[] adverb = { "randomly", "quickly", "triumphantly", "suggestively", "slowly", "angrily",
			"uncomfortably", "finally", "unexpectedly", "hysterically", "thinly", "dryly", "blazingly", "terribly",
			"bleakly", "irritably", "dazzlingly", "expectantly", "impersonally", "abruptly", "awfully", "caressingly",
			"completely", "undesirably", "drolly", "hypocritically", "blankly", "dimly" };

	private static String randomWord(String[] type) {
		return type[rand.nextInt(type.length)];
	}

	/**
	 * Generates a random poem line.
	 * 
	 * @return a poem that is generated with some keywords.
	 */
	public static String poemLine() {
		StringBuffer results = new StringBuffer(150);
		results.append(randomWord(adjective));
		results.append(' ');
		results.append(randomWord(noun));
		results.append(' ');
		results.append(randomWord(verb));
		results.append(' ');
		results.append(randomWord(adverb));
		results.append(", ");
		return results.toString();
	}

}
