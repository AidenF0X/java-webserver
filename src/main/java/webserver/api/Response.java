package webserver.api;

public interface Response extends HttpMessage {

	/**
	 * Gets the status of the response (e.g. 200 OK).
	 *
	 * @return Returns the response status.
	 */
	HttpStatus status();
}
