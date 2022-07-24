package webserver.api;

import java.net.URI;

public interface Request extends HttpMessage {

	/**
	 * Gets the request method (e.g. GET).
	 *
	 * @return Returns the request method.
	 */
	HttpMethod method();

	/**
	 * Gets the URI being requested.
	 *
	 * @return Returns the request URI.
	 */
	URI uri();
}
