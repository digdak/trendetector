package trendetector.crawler;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

public class FileURL {
	private static RequestConfig config = RequestConfig.custom()
			  .setConnectTimeout(5 * 1000)
			  .setConnectionRequestTimeout(5 * 1000)
			  .setSocketTimeout(5 * 1000).build();
	
	private static HttpClient httpClient = HttpClientBuilder.create()
			.setDefaultRequestConfig(config).build();
	
	public static void download(String url, String path) throws IOException {
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet get = new HttpGet(url);
		get.addHeader("Referer", url);
		HttpResponse res = httpClient.execute(get);
		
		byte[] buf = new byte[1024];
		int byteRead;
		
		File file = new File(path);
		file.getParentFile().mkdirs();
		
		// TODO file path에 문제가 없도록 정규화시켜야 함.
		// TODO 또, 중복되는 이름의 파일이 있을 경우 처리를 넣어줘야 함.
		OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
		InputStream is = res.getEntity().getContent();
		
		while ((byteRead = is.read(buf)) != -1) {
			os.write(buf, 0, byteRead);
		}
		
		is.close();
		os.close();
	}
	
	public static String getHtml(String url) throws  IOException {
		HttpGet get = new HttpGet(url);
		get.addHeader("Referer", url);
		HttpResponse res = httpClient.execute(get);
		
		StringBuffer sb = new StringBuffer();
		InputStreamReader is = new InputStreamReader(res.getEntity().getContent(), "UTF-8");
		BufferedReader br = new BufferedReader(is);
		String readLine = null;
		
		while ((readLine = br.readLine()) != null) {
			sb.append(readLine);
		}
		
		is.close();
		return sb.toString();
	}
}
