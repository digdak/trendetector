package trendetector.test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import trendetector.crawler.FileURL;


public class Test {
	
	public static void main(String[] args) {
		try {
			String url = "http://www.dogdrip.net/dogdrip";
			Document doc = new Document(url);
			doc.html("<body><div><font>  1  </font></div></body>");
//			Elements items = doc.select(".boardList tbody tr");

			String strReplies = doc.select("font").text();
			if (!strReplies.isEmpty()) {
				System.out.println(Integer.parseInt(strReplies));
			}
			
			String str = "127/34";
			str = str.substring(0, str.indexOf("/"));
			System.out.println(str);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
