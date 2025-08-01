package io.jboot.components.http;

import java.util.HashMap;
import java.util.Map;

public class HttpMimeTypes {

	private static final Map<String, String> defaultMappings = new HashMap<>(200);

	static {
		defaultMappings.put("txt", "text/plain");
		defaultMappings.put("css", "text/css");
		defaultMappings.put("html", "text/html");
		defaultMappings.put("htm", "text/html");
		defaultMappings.put("gif", "image/gif");
		defaultMappings.put("jpg", "image/jpeg");
		defaultMappings.put("jpe", "image/jpeg");
		defaultMappings.put("jpeg", "image/jpeg");
		defaultMappings.put("bmp", "image/bmp");
		defaultMappings.put("js", "application/javascript");
		defaultMappings.put("png", "image/png");
		defaultMappings.put("java", "text/plain");
		defaultMappings.put("body", "text/html");
		defaultMappings.put("rtx", "text/richtext");
		defaultMappings.put("tsv", "text/tab-separated-values");
		defaultMappings.put("etx", "text/x-setext");
		defaultMappings.put("json", "application/json");
		defaultMappings.put("class", "application/java");
		defaultMappings.put("csh", "application/x-csh");
		defaultMappings.put("sh", "application/x-sh");
		defaultMappings.put("tcl", "application/x-tcl");
		defaultMappings.put("tex", "application/x-tex");
		defaultMappings.put("texinfo", "application/x-texinfo");
		defaultMappings.put("texi", "application/x-texinfo");
		defaultMappings.put("t", "application/x-troff");
		defaultMappings.put("tr", "application/x-troff");
		defaultMappings.put("roff", "application/x-troff");
		defaultMappings.put("man", "application/x-troff-man");
		defaultMappings.put("me", "application/x-troff-me");
		defaultMappings.put("ms", "application/x-wais-source");
		defaultMappings.put("src", "application/x-wais-source");
		defaultMappings.put("zip", "application/zip");
		defaultMappings.put("bcpio", "application/x-bcpio");
		defaultMappings.put("cpio", "application/x-cpio");
		defaultMappings.put("gtar", "application/x-gtar");
		defaultMappings.put("shar", "application/x-shar");
		defaultMappings.put("sv4cpio", "application/x-sv4cpio");
		defaultMappings.put("sv4crc", "application/x-sv4crc");
		defaultMappings.put("tar", "application/x-tar");
		defaultMappings.put("ustar", "application/x-ustar");
		defaultMappings.put("dvi", "application/x-dvi");
		defaultMappings.put("hdf", "application/x-hdf");
		defaultMappings.put("latex", "application/x-latex");
		defaultMappings.put("bin", "application/octet-stream");
		defaultMappings.put("oda", "application/oda");
		defaultMappings.put("pdf", "application/pdf");
		defaultMappings.put("ps", "application/postscript");
		defaultMappings.put("eps", "application/postscript");
		defaultMappings.put("ai", "application/postscript");
		defaultMappings.put("rtf", "application/rtf");
		defaultMappings.put("nc", "application/x-netcdf");
		defaultMappings.put("cdf", "application/x-netcdf");
		defaultMappings.put("cer", "application/x-x509-ca-cert");
		defaultMappings.put("exe", "application/octet-stream");
		defaultMappings.put("gz", "application/x-gzip");
		defaultMappings.put("Z", "application/x-compress");
		defaultMappings.put("z", "application/x-compress");
		defaultMappings.put("hqx", "application/mac-binhex40");
		defaultMappings.put("mif", "application/x-mif");
		defaultMappings.put("ico", "image/x-icon");
		defaultMappings.put("ief", "image/ief");
		defaultMappings.put("tiff", "image/tiff");
		defaultMappings.put("tif", "image/tiff");
		defaultMappings.put("ras", "image/x-cmu-raster");
		defaultMappings.put("pnm", "image/x-portable-anymap");
		defaultMappings.put("pbm", "image/x-portable-bitmap");
		defaultMappings.put("pgm", "image/x-portable-graymap");
		defaultMappings.put("ppm", "image/x-portable-pixmap");
		defaultMappings.put("rgb", "image/x-rgb");
		defaultMappings.put("xbm", "image/x-xbitmap");
		defaultMappings.put("xpm", "image/x-xpixmap");
		defaultMappings.put("xwd", "image/x-xwindowdump");
		defaultMappings.put("au", "audio/basic");
		defaultMappings.put("snd", "audio/basic");
		defaultMappings.put("aif", "audio/x-aiff");
		defaultMappings.put("aiff", "audio/x-aiff");
		defaultMappings.put("aifc", "audio/x-aiff");
		defaultMappings.put("wav", "audio/x-wav");
		defaultMappings.put("mp3", "audio/mpeg");
		defaultMappings.put("mpeg", "video/mpeg");
		defaultMappings.put("mpg", "video/mpeg");
		defaultMappings.put("mpe", "video/mpeg");
		defaultMappings.put("qt", "video/quicktime");
		defaultMappings.put("mov", "video/quicktime");
		defaultMappings.put("avi", "video/x-msvideo");
		defaultMappings.put("movie", "video/x-sgi-movie");
		defaultMappings.put("avx", "video/x-rad-screenplay");
		defaultMappings.put("wrl", "x-world/x-vrml");
		defaultMappings.put("mpv2", "video/mpeg2");
		defaultMappings.put("jnlp", "application/x-java-jnlp-file");

		defaultMappings.put("eot", "application/vnd.ms-fontobject");
		defaultMappings.put("woff", "application/font-woff");
		defaultMappings.put("woff2", "application/font-woff2");
		defaultMappings.put("ttf", "application/x-font-ttf");
		defaultMappings.put("otf", "application/x-font-opentype");
		defaultMappings.put("sfnt", "application/font-sfnt");

		/* Add XML related MIMEs */

		defaultMappings.put("xml", "application/xml");
		defaultMappings.put("xhtml", "application/xhtml+xml");
		defaultMappings.put("xsl", "application/xml");
		defaultMappings.put("svg", "image/svg+xml");
		defaultMappings.put("svgz", "image/svg+xml");
		defaultMappings.put("wbmp", "image/vnd.wap.wbmp");
		defaultMappings.put("wml", "text/vnd.wap.wml");
		defaultMappings.put("wmlc", "application/vnd.wap.wmlc");
		defaultMappings.put("wmls", "text/vnd.wap.wmlscript");
		defaultMappings.put("wmlscriptc", "application/vnd.wap.wmlscriptc");

		/**
		 * 视频相关
		 */
		defaultMappings.put("asf", "video/x-ms-asf");
		defaultMappings.put("asx", "video/x-ms-asf");
		defaultMappings.put("flv", "video/x-flv");
		defaultMappings.put("mp4", "video/mp4");
		defaultMappings.put("mps", "video/x-mpeg");
		defaultMappings.put("mpv", "video/mpg");
		defaultMappings.put("mpa", "video/x-mpg");
		defaultMappings.put("m4e", "video/mpeg4");
		defaultMappings.put("m2v", "video/x-mpeg");
		defaultMappings.put("wmv", "video/x-ms-wmv");
		defaultMappings.put("3gp", "video/3gpp");
		defaultMappings.put("ts", "video/MP2T");

		/**
		 * 音频相关
		 */
		defaultMappings.put("mp2", "audio/mp2");
		defaultMappings.put("m3u", "audio/x-mpegurl");
		defaultMappings.put("m3u8", "audio/x-mpegurl");
		defaultMappings.put("mpga", "audio/rn-mpeg");
		defaultMappings.put("ra", "audio/vnd.rn-realaudio");
		defaultMappings.put("ram", "audio/x-pn-realaudio");
		defaultMappings.put("wax", "audio/x-ms-wax");
		defaultMappings.put("wma", "audio/x-ms-wma");

		/**
		 * 文档相关
		 */
		defaultMappings.put("doc", "application/msword");
		defaultMappings.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		defaultMappings.put("xls", "application/vnd.ms-excel");
		defaultMappings.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		defaultMappings.put("pot", "application/vnd.ms-powerpoint");
		defaultMappings.put("ppt", "application/vnd.ms-powerpoint");
		defaultMappings.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");

	}

	public static String getMimeType(String file) {
		String lower = file.toLowerCase();
		int pos = lower.lastIndexOf('.');
		if (pos == -1) {
			return null; //no extension
		}
		return defaultMappings.get(lower.substring(pos + 1));
	}

}
