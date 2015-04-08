package trendetector.crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

public class URLStringUtil {
	public static String urlJoin(String baseUrl, String refurl) {
		try {
			return new URL(new URL(baseUrl), refurl).toString();
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	public static String urlAddQuery(String baseUrl, String name, String value) {
		if (Pattern.matches(".*[?|&]" + name + "=([^&]*|[^&]*&.*)", baseUrl)) {
			baseUrl = baseUrl.replaceAll("\\?" + name + "=[^&]*", "?" + name + "=" + value);
			baseUrl = baseUrl.replaceAll("&" + name + "=[^&]*", "&" + name + "=" + value);
			
			return baseUrl;
		}

		URL url = null;
		
		try {
			url = new URL(baseUrl);
		} catch (MalformedURLException e) {
			return null;
		}
		
		if (url.getQuery() == null) {
			return baseUrl + "?" + name + "=" + value;
		}
		
		if (url.getQuery().equals("") || baseUrl.charAt(baseUrl.length() - 1) == '&') {
			return baseUrl + name + "=" + value;
		}
		
		return baseUrl + "&" + name + "=" + value;
	}
	
}
