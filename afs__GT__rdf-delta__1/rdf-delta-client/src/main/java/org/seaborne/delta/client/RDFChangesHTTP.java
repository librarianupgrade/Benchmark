/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.delta.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonException;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.WebContent;
import org.seaborne.delta.*;
import org.apache.jena.atlas.io.IOX;
import org.seaborne.delta.lib.JSONX;
import org.apache.jena.rdfpatch.RDFPatchConst;
import org.apache.jena.rdfpatch.text.RDFChangesWriterText;
import org.slf4j.Logger;

/** Collect the bytes of a change stream, then write to HTTP */
public class RDFChangesHTTP extends RDFChangesWriterText {
	// This should have control of text/binary.

	private static final Logger LOG = Delta.DELTA_HTTP_LOG;
	private final CloseableHttpClient httpClient = HttpClients.createDefault();
	private final ByteArrayOutputStream bytes;
	// Count to match up begin-commit.
	private int txnDepth = 0;
	private final Runnable resetAction;
	private final Supplier<String> urlSupplier;
	private final String destLabel;
	// Used to coordinate with reading patches in.
	private final Object syncObject;
	private StatusLine statusLine = null;
	private String response = null;
	private Node patchId = null;
	private Node previousId = null;

	/** Send changes to a specific URL */
	public RDFChangesHTTP(String urlstr) {
		this(urlstr, null, () -> urlstr, null);
	}

	/** Send changes to a specific URL */
	public RDFChangesHTTP(String label, String urlstr) {
		this(label, () -> urlstr, null);
	}

	// resetAction (on 401) not currently enabled.

	/** Send changes to a supplied URL, with an action a specific action  */
	/* unused outside */ private RDFChangesHTTP(String label, Supplier<String> urlSupplier) {
		this(label, urlSupplier, null);
	}

	/** Send changes to a supplied URL, with an action a specific action on any 401  */
	private RDFChangesHTTP(String label, Supplier<String> urlSupplier, Runnable resetAction) {
		this(label, null, urlSupplier, resetAction);
	}

	/** Send changes to a supplied URL, with an action a specific action on any 401 and sync'ed on a specific object  */
	private RDFChangesHTTP(String label, Object syncObject, Supplier<String> urlSupplier, Runnable resetAction) {
		this(label, syncObject, urlSupplier, resetAction, new ByteArrayOutputStream(100 * 1024));
	}

	private RDFChangesHTTP(String label, Object syncObject, Supplier<String> urlSupplier, Runnable resetAction,
			ByteArrayOutputStream out) {
		super(DeltaOps.tokenWriter(out));
		this.syncObject = (syncObject != null) ? syncObject : new Object();
		this.resetAction = resetAction;
		this.urlSupplier = urlSupplier;
		this.destLabel = label;
		this.bytes = out;
		reset();
	}

	@Override
	public void header(String field, Node value) {
		super.header(field, value);
		if (field.equals(RDFPatchConst.ID))
			patchId = value;
		if (field.equals(RDFPatchConst.PREV))
			previousId = value;
	}

	// Sent to the RDFChangesWriter
	//    @Override
	//    public void add(Node g, Node s, Node p, Node o) {
	//        super.add(g, s, p, o);
	//    }
	//
	//    @Override
	//    public void delete(Node g, Node s, Node p, Node o) {
	//        super.delete(g, s, p, o);
	//    }
	//
	//    @Override
	//    public void addPrefix(Node gn, String prefix, String uriStr) {
	//        super.addPrefix(gn, prefix, uriStr);
	//    }
	//
	//    @Override
	//    public void deletePrefix(Node gn, String prefix) {
	//        super.deletePrefix(gn, prefix);
	//    }

	@Override
	public void txnBegin() {
		if (txnDepth != 0) {
			LOG.warn("Nested transaction begin - ignored");
			return;
		}
		txnDepth++;
		super.txnBegin();
	}

	@Override
	public void txnCommit() {
		if (txnDepth == 0) {
			LOG.warn("Not in a transaction: commit ignored");
			return;
		}
		if (txnDepth > 1)
			LOG.warn("Nested transaction error.");
		// This adds the "TC"
		super.txnCommit();
		// This will throw an exception if the patch isn't current.
		// send does reset().
		// The exception passes up and DatasetGraphChanges turns the commit into an abort.
		send();
		//--txnDepth;
		// No nested transactions.
		txnDepth = 0;
	}

	@Override
	public void txnAbort() {
		if (txnDepth == 0) {
			LOG.warn("Not in a transaction: abort ignored");
			return;
		}
		if (txnDepth > 1)
			LOG.warn("Nested transaction error.");
		response = null;
		// Forget everything.
		reset();
		//--txnDepth;
		// No nested transactions.
		txnDepth = 0;
	}

	private void reset() {
		patchId = null;
		bytes.reset();
	}

	private byte[] collected() {
		return bytes.toByteArray();
	}

	public void send() {
		synchronized (syncObject) {
			try {
				send$();
			} finally {
				reset();
			}
		}
	}

	/** Get the protocol response - may be null if the change was aborted.  */
	public String getResponse() {
		return response;
	}

	private static AtomicLong counter = new AtomicLong(0);

	private void send$() {
		long number = counter.incrementAndGet();

		byte[] bytes = collected();
		String idStr;

		if (patchId != null)
			idStr = Id.str(patchId);
		else
			idStr = Long.toString(number);
		FmtLog.info(LOG, "Send patch %s (%d bytes) -> %s", idStr, bytes.length, destLabel);

		if (false) {
			if (LOG.isDebugEnabled()) {
				String s = new String(bytes, StandardCharsets.UTF_8);
				LOG.debug("== Sending ...");
				// Do NOT close!
				try (IndentedWriter w = IndentedWriter.stdout.clone();) {
					String x = w.getLinePrefix();
					w.setLinePrefix(">> ");
					w.print(s);
					w.setLinePrefix(x);
					if (!s.endsWith("\n"))
						w.println();
					w.flush();
					LOG.debug("== ==");
				}
			}
		}

		int attempts = 0;
		for (;;) {
			HttpPost postRequest = new HttpPost(urlSupplier.get());
			postRequest.setEntity(new ByteArrayEntity(bytes));

			try (CloseableHttpResponse r = httpClient.execute(postRequest)) {
				attempts++;
				statusLine = r.getStatusLine();
				response = readResponse(r);
				int sc = r.getStatusLine().getStatusCode();
				if (sc >= 200 && sc <= 299)
					return;
				if (sc >= 300 && sc <= 399) {
					FmtLog.info(LOG, "Send patch %s HTTP %d", idStr, sc);
					throw new DeltaHttpException(sc, "HTTP Redirect");
				}
				if (sc == 400) {
					// Is there a JSON error response?
					if (response != null) {
						// This includes being out of sync with the patch log due to a concurrent update.
						Header ct = r.getEntity().getContentType();
						if (ct != null && WebContent.contentTypeJSON.equals(ct.getValue())) {
							JsonObject object;
							try {
								object = JSON.parse(response);
							} catch (JsonException ex) {
								FmtLog.error(LOG, "Failed to parse JSON error response: " + ex.getMessage());
								throw new DeltaBadRequestException("Bad JSON request: " + ex.getMessage());
							}

							String errStr = JSONX.getStrOrNull(object, DeltaConst.F_ERROR);
							JsonObject infoObj = object.getObj(DeltaConst.F_LOG_INFO);
							if (errStr == null || infoObj == null) {
								// XXX
								FmtLog.warn(LOG, "Bad error from patch %s : HTTP bad request: %s", idStr,
										r.getStatusLine().getReasonPhrase());
								throw new DeltaBadPatchException(r.getStatusLine().getReasonPhrase());
							}
							PatchLogInfo patchLogInfo = PatchLogInfo.fromJson(infoObj);
							String prevStr = (previousId == null) ? "none" : Id.str(previousId);
							// XXX Don't need two log messages
							FmtLog.warn(LOG, "Append conflict: patch=[%s,%s] log=%s", idStr, prevStr, patchLogInfo);
							throw new DeltaPatchVersionException(errStr, object);
						}
					}

					// Bad request - not a delta error body.
					FmtLog.warn(LOG, "Patch %s : HTTP bad request: %s", idStr, r.getStatusLine().getReasonPhrase());
					throw new DeltaBadPatchException(r.getStatusLine().getReasonPhrase());
				}
				if (sc == 401 && attempts == 1 && resetAction != null) {
					resetAction.run();
					continue;
				}
				if (sc >= 400 && sc <= 499)
					throw new DeltaHttpException(sc, r.getStatusLine().getReasonPhrase());
				if (sc >= 500)
					throw new DeltaHttpException(sc, r.getStatusLine().getReasonPhrase());
				break;
			} catch (DeltaHttpException ex) {
				throw ex;
			} catch (IOException e) {
				throw IOX.exception(e);
			}
		}
	}

	private static String readResponse(HttpResponse resp) {
		HttpEntity e = resp.getEntity();
		if (e != null) {
			try (InputStream ins = e.getContent()) {
				return IO.readWholeFileAsUTF8(ins);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}
}
