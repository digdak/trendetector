package trendetector.test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;


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
			
			String str = "<img src=\"http:www.naver.com\" alt=\"병신력\" style=\"margin: 0px\">123123</img>";
			System.out.println(Jsoup.clean(str, url, Whitelist.none().addAttributes("img", "src", "style")));
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
