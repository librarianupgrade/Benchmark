/*
 * $Id: PdfPages.java 3934 2009-05-27 11:23:23Z blowagie $
 *
 * Copyright 1999, 2000, 2001, 2002 Bruno Lowagie
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */

package com.lowagie.text.pdf;

import java.io.IOException;
import java.util.ArrayList;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.ExceptionConverter;

/**
 * <CODE>PdfPages</CODE> is the PDF Pages-object.
 * <P>
 * The Pages of a document are accessible through a tree of nodes known as the Pages tree.
 * This tree defines the ordering of the pages in the document.<BR>
 * This object is described in the 'Portable Document Format Reference Manual version 1.3'
 * section 6.3 (page 71-73)
 *
 * @see		PdfPage
 */

public class PdfPages {

	private ArrayList pages = new ArrayList();
	private ArrayList parents = new ArrayList();
	private int leafSize = 10;
	private PdfWriter writer;
	private PdfIndirectReference topParent;

	// constructors

	/**
	 * Constructs a <CODE>PdfPages</CODE>-object.
	 */

	PdfPages(PdfWriter writer) {
		this.writer = writer;
	}

	void addPage(PdfDictionary page) {
		try {
			if ((pages.size() % leafSize) == 0)
				parents.add(writer.getPdfIndirectReference());
			PdfIndirectReference parent = (PdfIndirectReference) parents.get(parents.size() - 1);
			page.put(PdfName.PARENT, parent);
			PdfIndirectReference current = writer.getCurrentPage();
			writer.addToBody(page, current);
			pages.add(current);
		} catch (Exception e) {
			throw new ExceptionConverter(e);
		}
	}

	PdfIndirectReference addPageRef(PdfIndirectReference pageRef) {
		try {
			if ((pages.size() % leafSize) == 0)
				parents.add(writer.getPdfIndirectReference());
			pages.add(pageRef);
			return (PdfIndirectReference) parents.get(parents.size() - 1);
		} catch (Exception e) {
			throw new ExceptionConverter(e);
		}
	}

	// returns the top parent to include in the catalog
	PdfIndirectReference writePageTree() throws IOException {
		if (pages.isEmpty())
			throw new IOException("The document has no pages.");
		int leaf = 1;
		ArrayList tParents = parents;
		ArrayList tPages = pages;
		ArrayList nextParents = new ArrayList();
		while (true) {
			leaf *= leafSize;
			int stdCount = leafSize;
			int rightCount = tPages.size() % leafSize;
			if (rightCount == 0)
				rightCount = leafSize;
			for (int p = 0; p < tParents.size(); ++p) {
				int count;
				int thisLeaf = leaf;
				if (p == tParents.size() - 1) {
					count = rightCount;
					thisLeaf = pages.size() % leaf;
					if (thisLeaf == 0)
						thisLeaf = leaf;
				} else
					count = stdCount;
				PdfDictionary top = new PdfDictionary(PdfName.PAGES);
				top.put(PdfName.COUNT, new PdfNumber(thisLeaf));
				PdfArray kids = new PdfArray();
				ArrayList internal = kids.getArrayList();
				internal.addAll(tPages.subList(p * stdCount, p * stdCount + count));
				top.put(PdfName.KIDS, kids);
				if (tParents.size() > 1) {
					if ((p % leafSize) == 0)
						nextParents.add(writer.getPdfIndirectReference());
					top.put(PdfName.PARENT, (PdfIndirectReference) nextParents.get(p / leafSize));
				} else {
					top.put(PdfName.ITXT, new PdfString(Document.getRelease()));
				}
				writer.addToBody(top, (PdfIndirectReference) tParents.get(p));
			}
			if (tParents.size() == 1) {
				topParent = (PdfIndirectReference) tParents.get(0);
				return topParent;
			}
			tPages = tParents;
			tParents = nextParents;
			nextParents = new ArrayList();
		}
	}

	PdfIndirectReference getTopParent() {
		return topParent;
	}

	void setLinearMode(PdfIndirectReference topParent) {
		if (parents.size() > 1)
			throw new RuntimeException("Linear page mode can only be called with a single parent.");
		if (topParent != null) {
			this.topParent = topParent;
			parents.clear();
			parents.add(topParent);
		}
		leafSize = 10000000;
	}

	void addPage(PdfIndirectReference page) {
		pages.add(page);
	}

	int reorderPages(int order[]) throws DocumentException {
		if (order == null)
			return pages.size();
		if (parents.size() > 1)
			throw new DocumentException(
					"Page reordering requires a single parent in the page tree. Call PdfWriter.setLinearMode() after open.");
		if (order.length != pages.size())
			throw new DocumentException("Page reordering requires an array with the same size as the number of pages.");
		int max = pages.size();
		boolean temp[] = new boolean[max];
		for (int k = 0; k < max; ++k) {
			int p = order[k];
			if (p < 1 || p > max)
				throw new DocumentException(
						"Page reordering requires pages between 1 and " + max + ". Found " + p + ".");
			if (temp[p - 1])
				throw new DocumentException("Page reordering requires no page repetition. Page " + p + " is repeated.");
			temp[p - 1] = true;
		}
		Object copy[] = pages.toArray();
		for (int k = 0; k < max; ++k) {
			pages.set(k, copy[order[k] - 1]);
		}
		return max;
	}
}