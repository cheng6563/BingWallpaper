package org.leicheng.bingwallpaper.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.leicheng.bingwallpaper.exception.GetWebPageException;

public abstract class HttpUtil {
	protected abstract String getIndexUrl();

	private static Map<String, String> cookie = new HashMap<String, String>();
	private static String previousUrl = null;

	public static byte[] getHtmlByte(String urlStr) throws ClientProtocolException, IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		InputStream html = getHtmlInputStream(urlStr);
		int ch;
		while ((ch = html.read()) >= 0) {
			bos.write(ch);
		}
		html.close();
		return bos.toByteArray();
	}

	public static String getHtmlString(String urlStr) throws ClientProtocolException, IOException {
		return new String(getHtmlByte(urlStr));
	}

	public static String getHtmlString(String urlStr, String charset) throws ClientProtocolException, IOException {
		
		return new String(getHtmlByte(urlStr), charset);
		
	}

	public static InputStream getHtmlInputStream(String urlStr) throws ClientProtocolException, IOException {
		System.out.println("Url: " + urlStr);
		StringBuffer cookieStr = new StringBuffer();
		for (String key : cookie.keySet()) {
			if (cookie.get(key) != null) {
				cookieStr.append(key + "=" + cookie.get(key) + ";");
			}
		}
		if (cookieStr.length() > 0) {
			cookieStr.deleteCharAt(cookieStr.length() - 1);
		}
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(urlStr);
		request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36");
		if (cookieStr != null && cookieStr.toString().trim().length() > 0)
			request.addHeader("Cookie", cookieStr.toString());
		request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		request.addHeader("Accept-Encoding", "gzip,deflate,sdch");
		request.addHeader("Connection", "keep-alive");
		String previousUrl = null;
		if (HttpUtil.previousUrl == null) {
			previousUrl = "http://cn.bing.com/";
		} else {
			previousUrl = HttpUtil.previousUrl;
		}

		System.out.println("Referer " + previousUrl);
		System.out.println("Cookie " + cookieStr);
		request.addHeader("Referer", previousUrl);
		HttpResponse response = client.execute(request);
		
		Header[] cookieHeaders = response.getHeaders("Set-Cookie");
		for (Header item : cookieHeaders) {
			String cookieStrs[] = item.getValue().split(";");
			for (String cookieStrItem : cookieStrs) {
				String cookies[] = cookieStrItem.split("=");
				if(cookies.length>1){
					cookie.put(cookies[0], cookies[1]);
				}else {
					cookie.put(cookies[0], cookies[0]);
				}
			}
		}
		if (response.getStatusLine().getStatusCode() == 200) {
			InputStream contentInputStream = response.getEntity().getContent();
			if (response.getFirstHeader("Content-Encoding") != null && response.getFirstHeader("Content-Encoding").getValue().equals("gzip")) {
				contentInputStream = new GZIPInputStream(contentInputStream);
			}
			HttpUtil.previousUrl = urlStr;
			return contentInputStream;
		} else {
			throw new GetWebPageException(urlStr, response.getStatusLine().getStatusCode());
		}
	}
}
