package trendetector.test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.bson.Document;

import trendetector.mongodb.MongoDB;


public class Test {

	public static void main(String[] args) {
		try {
//			String url = "http://www.todayhumor.co.kr/board/list.php?table=humorbest";
//			
//			String html = "<html><table><tr><td class=\"subject\"><font> [4]</font></td></tr></table></body></html>";
//			Document doc = new Document(url);
//			doc.html(html);
//			Elements items = doc.select(".subject");
//			for (Element item : items) {
//				
//				Element font = item.select("font").first();
//				String strReplies = ( font != null ? font.text() : "");
//				System.out.println(strReplies.isEmpty());
//				System.out.println(strReplies);
//			}
			Calendar cal = Calendar.getInstance();
			
			    
			Date d = new Date();
			cal.setTime(d); // sets calendar time/date
			cal.add(Calendar.HOUR_OF_DAY, 1); // adds one hour
			d = cal.getTime(); // returns new date object, one hour in the future
			System.out.println(d);
			Document doc = new Document("Date", d);
			MongoDB.db.getCollection("test").insertOne(doc);
			
			Date d2 = new Date();
			cal.setTime(d2); // sets calendar time/date
			cal.add(Calendar.HOUR_OF_DAY, 9); // adds one hour
			d2 = cal.getTime(); // returns new date object, one hour in the future
			System.out.println(d2);
			Document doc2 = new Document("Date", d2);
			MongoDB.db.getCollection("test").insertOne(doc2);
			
			
//			System.out.println(c.getTime());

			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
