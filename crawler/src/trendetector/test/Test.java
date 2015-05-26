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
			String host = "127.0.0.1";
			int port = 27017;
			String database = "trendetector";
			switch (args.length) {
			case 3:  database = args[2];
			case 2: port = Integer.parseInt(args[1]);
			case 1: host = args[0];
			}
			MongoDatabase db = MongoDB.create(host, port, database);
			
			Komoran komoran = new Komoran("crawler/models-full/");
			komoran.setUserDic("crawler/models-full/dic.user");
			komoran.setFWDic("crawler/models-full/fwd.user");
			
			Document where = new Document();
			
			where.append("_id", new ObjectId("5563e52048bd2f9891bf0e35"));

			FindIterable<Document> iter = 
					db.getCollection("article").find(where).limit(100);
			
			iter.forEach( (Document doc) -> {
				String subject = doc.getString("subject");
//				String contents = doc.getString("contents");
				
//				System.out.println(subject);
//				System.out.println(contents);
//				System.out.println(doc.getString("url"));
				
				System.out.println(komoran.analyze(subject));
			});
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
