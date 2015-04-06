package trendetector.crawler;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class FileURL {
	public static void download(String url, String path) {
		OutputStream outStream = null;
		URLConnection uCon = null;
		InputStream is = null;
		try {
			URL Url = new URL(url);
			byte[] buf = new byte[1024];
			int byteRead;
			
			outStream = new BufferedOutputStream(new FileOutputStream(path));
			uCon = Url.openConnection();
			is = uCon.getInputStream();
			while ((byteRead = is.read(buf)) != -1) {
				outStream.write(buf, 0, byteRead);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
				outStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
