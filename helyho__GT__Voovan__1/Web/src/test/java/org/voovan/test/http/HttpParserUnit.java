package org.voovan.test.http;

import junit.framework.TestCase;
import org.voovan.http.message.HttpParser;
import org.voovan.http.message.HttpStatic;
import org.voovan.http.message.Request;
import org.voovan.http.message.Response;
import org.voovan.tools.buffer.ByteBufferChannel;
import org.voovan.tools.TEnv;

import java.io.IOException;
import java.nio.ByteBuffer;

public class HttpParserUnit extends TestCase {

	static final String httpRequestGet = "GET /test/t?name=helyho HTTP/1.1\r\n" + "Connection: keep-alive\r\n"
			+ "UserAgent: Jakarta Commons-HttpClient/3.1\r\n" + "Host: 127.0.0.1:1031\r\n"
			+ "Content-Type: multipart/form-data; boundary=ujjLiiJBznFt70fG1F4EUCkIupn7H4tzm\r\n"
			+ "Content-Length: 329\r\n" + "User-Agent: Jakarta Commons-HttpClient/3.1\r\n"
			+ "Cookie: BAIDUID=57939E50D6B2A0B23D20CA330C89E290:FG=1; BAIDUPSID=57939E50D6B2A0B23D20CA330C89E290;\r\n"
			+ "Host: 127.0.0.1:1031\r\n" + "\r\n";

	String httpRequestPostSimple =

			"POST /test/t HTTP/1.1\r\n" + "Connection: keep-alive\r\n"
					+ "Content-Type: application/x-www-form-urlencoded\r\n" + "Content-Length: 34\r\n"
					+ "User-Agent: Jakarta Commons-HttpClient/3.1\r\n" + "Host: 127.0.0.1:1031\r\n" + "\r\n"
					+ "name=helyho&age=32%3D&address=wlmq\r\n" + "\r\n";

	String httpRequestPostComplex = "POST /test/t HTTP/1.1\r\n" + "Connection: keep-alive\r\n"
			+ "Content-Type: multipart/form-data; boundary=ujjLiiJBznFt70fG1F4EUCkIupn7H4tzm\r\n"
			+ "Content-Length: 329\r\n" + "User-Agent: Jakarta Commons-HttpClient/3.1\r\n"
			+ "Cookie: BAIDUID=57939E50D6B2A0B23D20CA330C89E290:FG=1; BAIDUPSID=57939E50D6B2A0B23D20CA330C89E290;\r\n"
			+ "Host: 127.0.0.1:1031\r\n" + "\r\n" + "--ujjLiiJBznFt70fG1F4EUCkIupn7H4tzm\r\n"
			+ "Content-Disposition: form-data; name=\"name\"; \r\n" + "\r\n" + "helyho\r\n"
			+ "--ujjLiiJBznFt70fG1F4EUCkIupn7H4tzm\r\n" + "Content-Disposition: form-data; name=\"age\"\r\n" + "\r\n"
			+ "32%3D\r\n" + "--ujjLiiJBznFt70fG1F4EUCkIupn7H4tzm\r\n"
			+ "Content-Disposition: form-data; name=\"address\"\r\n" + "\r\n" + "wlmq\r\n"
			+ "--ujjLiiJBznFt70fG1F4EUCkIupn7H4tzm\r\n"
			+ "Content-Disposition: form-data; name=\"upload\" filename=\"1.jpg\"\r\n"
			+ "Content-Transfer-Encoding: binary\r\n" + "\r\n" + "wlmq filecontent1\r\n"
			+ "--ujjLiiJBznFt70fG1F4EUCkIupn7H4tzm--\r\n\r\n";

	public HttpParserUnit(String name) {
		super(name);
	}

	public void testGet() throws IOException {
		ByteBufferChannel b = new ByteBufferChannel();
		b.writeEnd(ByteBuffer.wrap(httpRequestGet.getBytes()));
		Request request = HttpParser.parseRequest(null, b, 30000, -1);
		assertEquals(request.header().size(), 6);
		assertEquals(request.protocol().getPath(), "/test/t");
		assertEquals(request.protocol().getMethod(), "GET");
		assertEquals(request.getQueryString("UTF-8"), "name=helyho");
	}

	public void testPostSimple() throws IOException {
		ByteBufferChannel b = new ByteBufferChannel();
		b.writeEnd(ByteBuffer.wrap(httpRequestPostSimple.getBytes()));
		Request request = HttpParser.parseRequest(null, b, 30000, -1);
		assertEquals(request.header().size(), 5);
		assertEquals(request.protocol().getPath(), "/test/t");
		assertEquals(request.protocol().getMethod(), "POST");
		assertEquals(request.getQueryString("UTF-8"), "name=helyho&age=32=&address=wlmq");
	}

	public void testPostComplex() throws IOException {
		ByteBufferChannel b = new ByteBufferChannel();
		b.writeEnd(ByteBuffer.wrap(httpRequestPostComplex.getBytes()));
		Request request = HttpParser.parseRequest(null, b, 30000, -1);
		request.toString();
		request.parts().get(3).body().write("\r\n helyho");
		request.parts().get(3).saveAsFile("/Users/helyho/Downloads/helyho.txt");
		assertEquals(request.header().size(), 6);
		assertEquals(request.protocol().getPath(), "/test/t");
		assertEquals(request.protocol().getMethod(), "POST");
		assertEquals(request.getQueryString("UTF-8"), "name=helyho&age=32=&address=wlmq");
	}

	public void testResposne() throws IOException {
		ByteBufferChannel b = new ByteBufferChannel();
		Response response = HttpParser.parseResponse(null, b, 30000);
		System.out.println(response);
	}

	public static void main(String[] args) {

		System.out.println(TEnv.measure(() -> {
			for (int i = 0; i < 1000000; i++) {
				ByteBufferChannel b = new ByteBufferChannel();
				b.writeEnd(ByteBuffer.wrap(httpRequestGet.getBytes()));
				try {
					Request request = HttpParser.parseRequest(null, b, 30000, -1);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return null;
		}));

		System.out.println(TEnv.measure(() -> {
			for (int i = 0; i < 1000000; i++) {
				ByteBufferChannel b = new ByteBufferChannel();
				b.writeEnd(ByteBuffer.wrap(httpRequestGet.getBytes()));
				b.indexOf(HttpStatic.BODY_MARK.getBytes());
				b.getByteBuffer().hashCode();
			}

			return null;
		}));

	}
}
