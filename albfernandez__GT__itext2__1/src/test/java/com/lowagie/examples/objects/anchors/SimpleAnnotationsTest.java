/*
 * $Id: SimpleAnnotations.java 3373 2008-05-12 16:21:24Z xlv $
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

import java.io.File;
import java.net.URL;

import org.junit.Test;

import com.lowagie.text.Annotation;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTestBase;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Creates two documents with different types of Annotations.
 * 
 * @author blowagie
 */

public class SimpleAnnotationsTest {

	/**
	 * Creates documents with some simple annotations.
	 * 
	 */
	@Test
	public void main() throws Exception {

		// step 1: creation of a document-object
		Document document1 = new Document(PageSize.A4, 10, 10, 10, 10);
		Document document2 = new Document(PageSize.A4, 10, 10, 10, 10);

		// step 2:
		PdfWriter writer1 = PdfWriter.getInstance(document1, PdfTestBase.getOutputStream("SimpleAnnotations1.pdf"));
		PdfWriter writer2 = PdfWriter.getInstance(document2, PdfTestBase.getOutputStream("SimpleAnnotations2.pdf"));
		// step 3:
		writer2.setPdfVersion(PdfWriter.VERSION_1_5);
		document1.open();
		document2.open();
		// step 4:
		document1.add(new Paragraph("Each square on this page represents an annotation."));
		// document1
		PdfContentByte cb1 = writer1.getDirectContent();
		Annotation a1 = new Annotation("authors",
				"Maybe it's because I wanted to be an author myself that I wrote iText.", 250f, 700f, 350f, 800f);
		document1.add(a1);
		Annotation a2 = new Annotation(250f, 550f, 350f, 650f, new URL("http://www.lowagie.com/iText/"));
		document1.add(a2);
		Annotation a3 = new Annotation(250f, 400f, 350f, 500f, "http://www.lowagie.com/iText");
		document1.add(a3);
		Image image = Image.getInstance(PdfTestBase.RESOURCES_DIR + "iText.gif");
		image.setAnnotation(a3);
		document1.add(image);
		Annotation a4 = new Annotation(250f, 250f, 350f, 350f, PdfAction.LASTPAGE);
		document1.add(a4);
		// draw rectangles to show where the annotations were added
		cb1.rectangle(250, 700, 100, 100);
		cb1.rectangle(250, 550, 100, 100);
		cb1.rectangle(250, 400, 100, 100);
		cb1.rectangle(250, 250, 100, 100);
		cb1.stroke();
		// more content
		document1.newPage();
		for (int i = 0; i < 5; i++) {
			document1.add(new Paragraph("blahblahblah"));
		}
		document1.add(new Annotation("blahblah", "Adding an annotation without specifying coordinates"));
		for (int i = 0; i < 3; i++) {
			document1.add(new Paragraph("blahblahblah"));
		}
		document1.newPage();
		document1.add(new Chunk("marked chunk").setLocalDestination("mark"));

		File videoFile = new File(PdfTestBase.RESOURCES_DIR + "cards.mpg");
		File license = new File("LICENSE");

		// document2
		document2.add(new Paragraph("Each square on this page represents an annotation."));
		PdfContentByte cb2 = writer2.getDirectContent();
		Annotation a5 = new Annotation(100f, 700f, 200f, 800f, videoFile.getAbsolutePath(), "video/mpeg", true);
		document2.add(a5);
		Annotation a6 = new Annotation(100f, 550f, 200f, 650f, "SimpleAnnotations1.pdf", "mark");
		document2.add(a6);
		Annotation a7 = new Annotation(100f, 400f, 200f, 500f, "SimpleAnnotations1.pdf", 2);
		document2.add(a7);
		Annotation a8 = new Annotation(100f, 250f, 200f, 350f, license.getAbsolutePath(), null, null, null);
		document2.add(a8);
		// draw rectangles to show where the annotations were added
		cb2.rectangle(100, 700, 100, 100);
		cb2.rectangle(100, 550, 100, 100);
		cb2.rectangle(100, 400, 100, 100);
		cb2.rectangle(100, 250, 100, 100);
		cb2.stroke();

		// step 5: we close the document
		document1.close();
		document2.close();
	}
}