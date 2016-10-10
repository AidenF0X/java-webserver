package au.id.deejay.webserver.io;

import au.id.deejay.webserver.api.HttpMethod;
import au.id.deejay.webserver.api.HttpVersion;
import au.id.deejay.webserver.api.Request;
import au.id.deejay.webserver.exception.RequestException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import static au.id.deejay.webserver.MessageConstants.CRLF;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author David Jessup
 */
public class RequestReaderTest {

	private RequestReader requestReader;

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test
	public void testReadIsPassedThroughToUnderlyingStream() throws Exception {
		InputStream inputStream = mock(InputStream.class);
		requestReader = new RequestReader(inputStream);
		requestReader.read(new char[]{}, 0, 0);

		verify(inputStream).read(any(), anyInt(), anyInt());
	}

	@Test
	public void testCloseIsPassedThroughToUnderlyingStream() throws Exception {
		InputStream inputStream = mock(InputStream.class);
		requestReader = new RequestReader(inputStream);
		requestReader.close();

		verify(inputStream).close();
	}

	@Test
	public void testReadValidRequestWithoutEntityBody() throws Exception {
		String rawRequest = "GET /index.html HTTP/1.1" + CRLF
				+ "Connection: keep-alive" + CRLF
				+ CRLF;

		InputStream inputStream = new ByteArrayInputStream(rawRequest.getBytes(UTF_8));

		requestReader = new RequestReader(inputStream);

		Request request = requestReader.readRequest();

		assertThat(request.method(), is(HttpMethod.GET));
		assertThat(request.uri(), is(equalTo(new URI("/index.html"))));
		assertThat(request.version(), is(HttpVersion.HTTP_1_1));
	}

	@Test(expected = RequestException.class)
	public void testReadRequestWithInvalidHeaderThrowsException() throws Exception {
		String rawRequest = "GET /index.html HTTP/1.1" + CRLF
				+ "Bad-Header" + CRLF
				+ CRLF;

		InputStream inputStream = new ByteArrayInputStream(rawRequest.getBytes(UTF_8));

		requestReader = new RequestReader(inputStream);

		Request request = requestReader.readRequest();
	}

}