/*
 * Created on Jul 9, 2009
 * (c) 2009 Trumpet, Inc.
 *
 */
package com.lowagie.text.pdf.parser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ListIterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.lowagie.text.pdf.PRIndirectReference;
import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfTestBase;

public class PdfContentStreamProcessorTest {
	private DebugProcessor _processor;

	private static File resourceRoot;

	@BeforeClass
	public static void setUpClass() throws Exception {
		resourceRoot = new File(PdfTestBase.RESOURCES_DIR);
	}

	@Before
	public void setUp() throws Exception {
		_processor = new DebugProcessor();
	}

	// Replicates iText bug 2817030
	@Test
	public void testPositionAfterTstar() throws Exception {
		final byte[] pdfBytes = PdfTextExtractorTest.readDocument(new File(resourceRoot, "yaxiststar.pdf"));
		processBytes(pdfBytes, 1);
	}

	private void processBytes(final byte[] pdfBytes, final int pageNumber) throws IOException {
		final PdfReader pdfReader = new PdfReader(pdfBytes);

		final PdfDictionary pageDictionary = pdfReader.getPageN(pageNumber);

		final PdfDictionary resourceDictionary = pageDictionary.getAsDict(PdfName.RESOURCES);

		final PdfObject contentObject = pageDictionary.get(PdfName.CONTENTS);
		final byte[] contentBytes = readContentBytes(contentObject);
		_processor.processContent(contentBytes, resourceDictionary);
	}

	private byte[] readContentBytes(final PdfObject contentObject) throws IOException {
		final byte[] result;
		switch (contentObject.type()) {
		case PdfObject.INDIRECT:
			final PRIndirectReference ref = (PRIndirectReference) contentObject;
			final PdfObject directObject = PdfReader.getPdfObject(ref);
			result = readContentBytes(directObject);
			break;
		case PdfObject.STREAM:
			final PRStream stream = (PRStream) PdfReader.getPdfObject(contentObject);
			result = PdfReader.getStreamBytes(stream);
			break;
		case PdfObject.ARRAY:
			// Stitch together all content before calling processContent(),
			// because
			// processContent() resets state.
			final ByteArrayOutputStream allBytes = new ByteArrayOutputStream();
			final PdfArray contentArray = (PdfArray) contentObject;
			final ListIterator<?> iter = contentArray.listIterator();
			while (iter.hasNext()) {
				final PdfObject element = (PdfObject) iter.next();
				allBytes.write(readContentBytes(element));
			}
			result = allBytes.toByteArray();
			break;
		default:
			final String msg = "Unable to handle Content of type " + contentObject.getClass();
			throw new IllegalStateException(msg);
		}
		return result;
	}

	private class DebugProcessor extends PdfContentStreamProcessor {
		private float _lastY = Float.MAX_VALUE;

		@Override
		public void displayText(final String text, final Matrix nextTextMatrix) {
			final float y = nextTextMatrix.get(Matrix.I32);
			Assert.assertTrue("Test has jumpled back up the page", y <= _lastY);
			_lastY = y;
		}
	}

}
