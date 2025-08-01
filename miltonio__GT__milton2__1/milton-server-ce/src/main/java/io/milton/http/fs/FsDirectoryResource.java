/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.milton.http.fs;

import io.milton.http.*;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.*;
import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.milton.http.ResponseStatus.SC_FORBIDDEN;

/**
 * Represents a directory in a physical file system.
 *
 */
public class FsDirectoryResource extends FsResource
		implements MakeCollectionableResource, PutableResource, CopyableResource, DeletableResource, MoveableResource,
		PropFindableResource, LockingCollectionResource, GetableResource {

	private static final Logger log = LoggerFactory.getLogger(FsDirectoryResource.class);

	private final FileContentService contentService;

	public FsDirectoryResource(String host, FileSystemResourceFactory factory, File dir,
			FileContentService contentService) {
		super(host, factory, dir);
		this.contentService = contentService;
		if (!dir.exists()) {
			throw new IllegalArgumentException("Directory does not exist: " + dir.getAbsolutePath());
		}
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("Is not a directory: " + dir.getAbsolutePath());
		}
	}

	@Override
	public CollectionResource createCollection(String name) {
		File fnew = new File(file, name);
		boolean ok = fnew.mkdir();
		if (!ok) {
			throw new RuntimeException("Failed to create: " + fnew.getAbsolutePath());
		}
		factory.getWsManager().ifPresent(wsManager -> wsManager.notifyCreated(factory.toResourcePath(fnew)));
		return new FsDirectoryResource(host, factory, fnew, contentService);
	}

	@Override
	public Resource child(String name) {
		File fchild = new File(file, name);
		return factory.resolveFile(this.host, fchild);

	}

	@Override
	public List<? extends Resource> getChildren() {
		ArrayList<FsResource> list = new ArrayList<>();
		File[] files = Arrays.stream(Optional.ofNullable(this.file.listFiles()).orElse(new File[0]))
				.sorted(Comparator.comparing(File::isDirectory).reversed().thenComparing(File::getName))
				.toArray(File[]::new);
		for (File fchild : files) {
			FsResource res = factory.resolveFile(this.host, fchild);
			if (res != null) {
				list.add(res);
			} else {
				log.error("Couldnt resolve file {}", fchild.getAbsolutePath());
			}
		}
		return list;
	}

	/**
	 * Will redirect if a default page has been specified on the factory
	 *
	 * @param request
	 * @return
	 */
	@Override
	public String checkRedirect(Request request) {
		if (factory.getDefaultPage() != null) {
			final boolean hasSlash = request.getAbsoluteUrl().endsWith("/");
			return (hasSlash ? request.getAbsoluteUrl() : request.getAbsoluteUrl() + "/") + factory.getDefaultPage();
		} else {
			return null;
		}
	}

	@Override
	public Resource createNew(String name, InputStream in, Long length, String contentType) throws IOException {
		File dest = new File(this.getFile(), name);
		contentService.setFileContent(dest, in);
		factory.getWsManager().ifPresent(wsManager -> wsManager.notifyCreated(factory.toResourcePath(dest)));
		return factory.resolveFile(this.host, dest);

	}

	@Override
	protected void doCopy(File dest) throws NotAuthorizedException {
		try {
			if (isRecursive(dest)) {
				throw new NotAuthorizedException("Cannot copy to subfolder", this, SC_FORBIDDEN);
			}
			FileUtils.copyDirectory(this.getFile(), dest);
		} catch (IOException ex) {
			throw new RuntimeException("Failed to copy to:" + dest.getAbsolutePath(), ex);
		}
	}

	private boolean isRecursive(File dest) throws IOException {
		return dest.getCanonicalPath().startsWith(this.file.getCanonicalPath());
	}

	@Override
	public LockToken createAndLock(String name, LockTimeout timeout, LockInfo lockInfo) throws NotAuthorizedException {
		File dest = new File(this.getFile(), name);
		createEmptyFile(dest);
		FsFileResource newRes = new FsFileResource(host, factory, dest, contentService);
		LockResult res = newRes.lock(timeout, lockInfo);
		return res.getLockToken();
	}

	private void createEmptyFile(File file) {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(file);
			factory.getWsManager().ifPresent(wsManager -> wsManager.notifyCreated(factory.toResourcePath(file)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(fout);
		}
	}

	/**
	 * Will generate a listing of the contents of this directory, unless the
	 * factory's allowDirectoryBrowsing has been set to false.
	 *
	 * If so it will just output a message saying that access has been disabled.
	 *
	 * @param out
	 * @param range
	 * @param params
	 * @param contentType
	 * @throws IOException
	 * @throws NotAuthorizedException
	 */
	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType)
			throws IOException, NotAuthorizedException {
		//String uri = "/" + factory.getContextPath() + subpath;
		XmlWriter w = new XmlWriter(out);
		w.open("html");
		w.open("head");
		w.writeText("" + "<script type=\"text/javascript\" language=\"javascript1.1\">\n" + "    var fNewDoc = false;\n"
				+ "  </script>\n" + "  <script LANGUAGE=\"VBSCRIPT\">\n" + "    On Error Resume Next\n"
				+ "    Set EditDocumentButton = CreateObject(\"SharePoint.OpenDocuments.3\")\n"
				+ "    fNewDoc = IsObject(EditDocumentButton)\n" + "  </script>\n"
				+ "  <script type=\"text/javascript\" language=\"javascript1.1\">\n"
				+ "    var L_EditDocumentError_Text = \"The edit feature requires a SharePoint-compatible application and Microsoft Internet Explorer 4.0 or greater.\";\n"
				+ "    var L_EditDocumentRuntimeError_Text = \"Sorry, couldnt open the document.\";\n"
				+ "    function editDocument(strDocument) {\n"
				+ "      strDocument = 'http://192.168.1.2:8080' + strDocument; " + "      if (fNewDoc) {\n"
				+ "        if (!EditDocumentButton.EditDocument(strDocument)) {\n"
				+ "          alert(L_EditDocumentRuntimeError_Text + ' - ' + strDocument); \n" + "        }\n"
				+ "      } else { \n" + "        alert(L_EditDocumentError_Text + ' - ' + strDocument); \n"
				+ "      }\n" + "    }\n" + "  </script>\n");

		w.close("head");
		w.open("body");
		w.begin("h1").open().writeText(this.getName()).close();
		w.open("table");
		for (Resource r : Optional.ofNullable(getChildren()).orElse(List.of())) {
			w.open("tr");

			w.open("td");
			String path = buildHref(getFile().getCanonicalPath()
					.substring(factory.getRoot().getCanonicalPath().length()).replace('\\', '/'), r.getName());
			w.begin("a").writeAtt("href", path).open().writeText(r.getName()).close();

			w.begin("a").writeAtt("href", "#").writeAtt("onclick", "editDocument('" + path + "')").open()
					.writeText("(edit with office)").close();

			w.close("td");

			w.begin("td").open().writeText(r.getModifiedDate() + "").close();
			w.close("tr");
		}
		w.close("table");
		w.close("body");
		w.close("html");
		w.flush();
	}

	@Override
	public Long getMaxAgeSeconds(Auth auth) {
		return null;
	}

	@Override
	public String getContentType(String accepts) {
		return "text/html";
	}

	@Override
	public Long getContentLength() {
		return null;
	}

	private String buildHref(String uri, String name) {
		String abUrl = uri;

		if (!abUrl.endsWith("/")) {
			abUrl += "/";
		}
		if (ssoPrefix == null) {
			return abUrl + name;
		} else {
			// This is to match up with the prefix set on SimpleSSOSessionProvider in MyCompanyDavServlet
			String s = insertSsoPrefix(abUrl, ssoPrefix);
			return s += name;
		}
	}

	public static String insertSsoPrefix(String abUrl, String prefix) {
		// need to insert the ssoPrefix immediately after the host and port
		int pos = abUrl.indexOf("/", 8);
		String s = abUrl.substring(0, pos) + "/" + prefix;
		s += abUrl.substring(pos);
		return s;
	}
}
