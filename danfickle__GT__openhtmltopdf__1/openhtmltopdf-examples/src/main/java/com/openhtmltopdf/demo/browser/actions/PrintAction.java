package com.openhtmltopdf.demo.browser.actions;

import com.openhtmltopdf.demo.browser.BrowserStartup;
import com.openhtmltopdf.simple.XHTMLPrintable;
import com.openhtmltopdf.util.Uu;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

public class PrintAction extends AbstractAction {
	protected BrowserStartup root;

	public PrintAction(BrowserStartup root, ImageIcon icon) {
		super("Print", icon);
		this.root = root;
	}

	public void actionPerformed(ActionEvent evt) {
		Uu.p("printing");
		final PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setPrintable(new XHTMLPrintable(root.panel.view));

		if (printJob.printDialog()) {
			new Thread(new Runnable() {
				public void run() {
					try {
						Uu.p("starting printing");
						printJob.print();
						Uu.p("done printing");
					} catch (PrinterException ex) {
						Uu.p(ex);
					}
				}
			}).start();
		}

	}
}
