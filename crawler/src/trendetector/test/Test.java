package trendetector.test;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.jsoup.nodes.Document;

import trendetector.crawler.FileURL;


public class Test {
	
	public static void main(String[] args) {
		try {
			String subject = "빅뱅 vs 엑소";
			System.out.println("안녕하세요?".length());
			subject = subject.replaceAll("[\\n|\\r|(|)|\\[|\\]|{|}|'|“|”|\"|`|.|,]", " @ ");
			System.out.println(subject);
////			Elements items = doc.select(".boardList tbody tr");
//
//			String strReplies = doc.select("font").text();
//			if (!strReplies.isEmpty()) {
//				System.out.println(Integer.parseInt(strReplies));
//			}
//			
//			String str = "<img src=\"http:www.naver.com\" alt=\"병신력\" style=\"margin: 0px\">123123</img>";
//			System.out.println(Jsoup.clean(str, url, Whitelist.none().addAttributes("img", "src", "style")));
//			
			
//			String contents = "<div>안녕<br>하세요</div>";
//			contents = contents.replaceAll("<", " <").replace(">", "> ");
			
//			SimpleDateFormat dateToStrFormat = new SimpleDateFormat("yy/MM/dd");
//			SimpleDateFormat strToDateFormat = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
//			Date now = new Date();
//			Date date = strToDateFormat.parse(dateToStrFormat.format(now) + " " + "23:03:00");
//			System.out.println(date.getTime());
//			System.out.println(now.getTime());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
