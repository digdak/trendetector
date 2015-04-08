package trendetector.test;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Test {

	public static void main(String[] args) {
		try {
			String url = "http://www.todayhumor.co.kr/board/list.php?table=humorbest";
			
			String html = "<html><table><tr><td class=\"subject\"><font> [4]</font></td></tr></table></body></html>";
			Document doc = new Document(url);
			doc.html(html);
			Elements items = doc.select(".subject");
			for (Element item : items) {
				
				Element font = item.select("font").first();
				String strReplies = ( font != null ? font.text() : "");
				System.out.println(strReplies.isEmpty());
				System.out.println(strReplies);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
