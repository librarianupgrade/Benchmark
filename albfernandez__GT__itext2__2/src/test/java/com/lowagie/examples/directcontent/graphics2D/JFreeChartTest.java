/*
 * $Id: JFreeChartExample.java 3838 2009-04-07 18:34:15Z mstorer $
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
package com.lowagie.examples.directcontent.graphics2D;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.Test;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfTestBase;
import com.lowagie.text.pdf.PdfWriter;

/**
 * JFreeChart example.
 */
public class JFreeChartTest {

	/**
	 * Creates some PDFs with JFreeCharts.
	 * @param args no arguments needed
	 */
	@Test
	public void main() throws Exception {
		convertToPdf(getBarChart(), 400, 600, "barchart.pdf");
		convertToPdf(getPieChart(), 400, 600, "piechart.pdf");
		convertToPdf(getXYChart(), 400, 600, "xychart.pdf");
	}

	/**
	 * Converts a JFreeChart to PDF syntax.
	 * @param filename	the name of the PDF file
	 * @param chart		the JFreeChart
	 * @param width		the width of the resulting PDF
	 * @param height	the height of the resulting PDF
	 */
	public static void convertToPdf(JFreeChart chart, int width, int height, String filename) {
		// step 1
		Document document = new Document(new Rectangle(width, height));
		try {
			// step 2
			PdfWriter writer;
			writer = PdfWriter.getInstance(document, PdfTestBase.getOutputStream(filename));
			// step 3
			document.open();
			// step 4
			PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tp = cb.createTemplate(width, height);
			Graphics2D g2d = tp.createGraphics(width, height, new DefaultFontMapper());
			Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height);
			chart.draw(g2d, r2d);
			g2d.dispose();
			tp.sanityCheck();
			cb.addTemplate(tp, 0, 0);
			cb.sanityCheck();
		} catch (DocumentException de) {
			de.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// step 5
		document.close();
	}

	/**
	 * Gets an example barchart.
	 * @return a barchart
	 */
	public static JFreeChart getBarChart() {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		dataset.setValue(40, "hits/hour", "index.html");
		dataset.setValue(20, "hits/hour", "download.html");
		dataset.setValue(15, "hits/hour", "faq.html");
		dataset.setValue(8, "hits/hour", "links.html");
		dataset.setValue(31, "hits/hour", "docs.html");
		return ChartFactory.createBarChart("Popularity of iText pages", "Page", "hits/hour", dataset,
				PlotOrientation.VERTICAL, false, true, false);
	}

	/**
	 * Gets an example piechart.
	 * @return a piechart
	 */
	public static JFreeChart getPieChart() {
		DefaultPieDataset dataset = new DefaultPieDataset();
		dataset.setValue("iText", 60);
		dataset.setValue("cinema.lowagie.com", 10);
		dataset.setValue("tutorial", 30);
		return ChartFactory.createPieChart("Website popularity", dataset, true, true, false);
	}

	/**
	 * Gets an example XY chart
	 * @return an XY chart
	 */
	public static JFreeChart getXYChart() {
		XYSeries series = new XYSeries("XYGraph");
		series.add(1, 5);
		series.add(2, 7);
		series.add(3, 3);
		series.add(4, 5);
		series.add(5, 4);
		series.add(6, 5);
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series);
		return ChartFactory.createXYLineChart("XY Chart", "X-axis", "Y-axis", dataset, PlotOrientation.VERTICAL, true,
				true, false);
	}
}
