package trendetector;

import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoDatabase;

import trendetector.crawler.ArticleCrawler;
import trendetector.mongodb.MongoDB;


public class ListCrawlEngine {

	public static void main(String[] args) throws InterruptedException {
		Document where = new Document("active", true);
		MongoDatabase db = MongoDB.create();
		
		while (true) {
			try {
				db.getCollection("board").find(where).forEach((Document doc) -> {
					ObjectId board_id = doc.getObjectId("_id");
					String community = doc.getString("community");
					String name = doc.getString("name");
					String url = doc.getString("url");
					
					System.out.println(new Date() + "\t[Crawl]\t" + community + "\t" + name + "\t" + url);
					try {
						new ArticleCrawler(board_id, community, url).run();
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				
				System.out.println("[DONE] sleep...5000ms");
				Thread.sleep(5000);
				
			} catch (Exception e) {
				e.printStackTrace();
				Thread.sleep(5000);
			}
		}
		
	}

}
