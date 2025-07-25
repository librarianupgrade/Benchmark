/**
 * Copyright (c) 2015-2020, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.components.http;

import com.jfinal.log.Log;

import java.io.*;
import java.util.List;
import java.util.Map;

public class JbootHttpResponse {
	private static final Log log = Log.getLog(JbootHttpResponse.class);

	private String content;
	private OutputStream contentStream;
	private File file;
	private Throwable error;
	private Map<String, List<String>> headers;
	private int responseCode;
	private String contentType;

	public JbootHttpResponse() {
		this.contentStream = new ByteArrayOutputStream();
	}

	public JbootHttpResponse(File file) {
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		if (file.exists()) {
			file.delete();
		}

		try {
			this.file = file;
			this.contentStream = new FileOutputStream(file);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获取数据内容
	 *
	 * @return
	 */
	public String getContent() {
		if (content != null) {
			return content;
		}
		if (contentStream != null && contentStream instanceof ByteArrayOutputStream) {
			return new String(((ByteArrayOutputStream) contentStream).toByteArray());
		}
		return null;
	}

	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * 把 inputStream 写入response
	 *
	 * @param inputStream
	 */
	public void copyStream(InputStream inputStream) {
		try {
			byte[] buffer = new byte[1024];
			for (int len = 0; (len = inputStream.read(buffer)) > 0;) {
				contentStream.write(buffer, 0, len);
			}
		} catch (Throwable throwable) {
			log.error(throwable.toString(), throwable);
			setError(throwable);
		}
	}

	/**
	 * 结束response和释放资源
	 */
	public void close() {
		if (contentStream != null) {
			try {
				contentStream.flush();
				contentStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isNotError() {
		return !isError();
	}

	public boolean isError() {
		return error != null;
	}

	public Throwable getError() {
		return error;
	}

	public void setError(Throwable error) {
		this.error = error;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public String toString() {
		return "JbootHttpResponse{" + "\nfile=" + file + "\nheaders=" + headers + "\nresponseCode=" + responseCode
				+ "\ncontentType=" + contentType + "\ncontent=" + getContent() + "\n}";
	}
}
