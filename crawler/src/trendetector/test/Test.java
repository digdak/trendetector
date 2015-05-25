package trendetector.test;

import kr.co.shineware.nlp.komoran.core.analyzer.Komoran;

import org.bson.Document;
import org.bson.types.ObjectId;

import trendetector.mongodb.MongoDB;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;


public class Test {
	
	public static void main(String[] args) {
		try {
			String url = "http://mlbpark.donga.com/mbs/articleVC.php?mbsC=bullpen2&mbsIdx=2507711&cpage=1";
			String no = url.split("&mbsIdx=")[1].split("&")[0];
			System.out.println(no);
			System.out.println(url);
			
			Komoran komoran = new Komoran("crawler/models-full/");
			komoran.setUserDic("crawler/models-full/dic.user");
			komoran.setFWDic("crawler/models-full/fwd.user");
			MongoDatabase db = MongoDB.create();
			
			Document where = new Document();
			
			where.append("_id", new ObjectId("5562c3d78a34bd15383e512a"));

			FindIterable<Document> iter = 
					db.getCollection("article").find(where).limit(100);
			
			iter.forEach( (Document doc) -> {
				String subject = doc.getString("subject");
				String contents = doc.getString("contents");
				
				System.out.println(subject);
				System.out.println(contents);
				System.out.println(doc.getString("url"));
			});
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
