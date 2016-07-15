package org.leicheng.bingwallpaper.exception;

public class GetWebPageException extends RuntimeException {
	private static final long serialVersionUID = 5750210177623606370L;

	private String url;
	private int responseCode;
	private String message;

	public GetWebPageException(String url, int responseCode) {
		super();
		this.url = url;
		this.responseCode = responseCode;
	}

	public GetWebPageException(String url,String message) {
		super();
		this.url = url;
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message == null ? "url: " + url + " ,  responseCode: "
				+ responseCode : message;
	}
}
