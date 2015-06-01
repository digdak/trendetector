package trendetector;

import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoDatabase;

import trendetector.crawler.ArticleCrawler;
import trendetector.mongodb.MongoDB;


public class ListCrawlEngine {

	public static void main(String[] args) throws InterruptedException {
		String host = "127.0.0.1";
		int port = 27017;
		String database = "trendetector";
		switch (args.length) {
		case 3:  database = args[2];
		case 2: port = Integer.parseInt(args[1]);
		case 1: host = args[0];
		}
		MongoDatabase db = MongoDB.create(host, port, database);
		
		Document where = new Document("active", true);
		while (true) {
			try {
				db.getCollection("board").find(where).forEach((Document doc) -> {
					ObjectId board_id = doc.getObjectId("_id");
					String community = doc.getString("community");
					String name = doc.getString("name");
					String url = doc.getString("url");
					
					System.out.println(new Date() + "\t[Crawl]\t" + community + "\t" + name + "\t" + url);
					try {
						new ArticleCrawler(db, board_id, community, url).run();
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				
				System.out.println(new Date() + "\t[SLEEP] ...5000ms");
				Thread.sleep(5000);
				
			} catch (Exception e) {
				e.printStackTrace();
				Thread.sleep(5000);
			}
		}
		
	}

}
