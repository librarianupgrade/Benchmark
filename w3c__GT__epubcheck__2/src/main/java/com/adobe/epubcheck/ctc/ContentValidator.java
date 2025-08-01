package com.adobe.epubcheck.ctc;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.opf.DocumentValidator;

public interface ContentValidator {
	public enum ValidationType {
		TEXT, CONTENT, SPINE, NCX, NAV, SCRIPT, SPAN, STYLE, METADATA_V3, METADATA_V2, LANG, CSS_SEARCH, LINK,
		RENDITION, CFI, HTML_STRUCTURE, MULTIPLE_CSS, EPUB3_STRUCTURE, TOC, SVG
	}

	public DocumentValidator newInstance(Report report, ValidationType vt, EpubPackage epack);
}
