package trendetector;

import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import trendetector.crawler.FileURL;
import trendetector.crawler.parser.ContentsParserFactory;
import trendetector.mongodb.MongoDB;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;



public class ContentsCrawlEngine {
	
	public static void main(String[] args) throws ClassNotFoundException, InterruptedException {
		MongoDatabase db = MongoDB.create();
		
		Document where = new Document("contents", new Document("$exists", false));
		Document orderby = new Document("date", 1);
		
		while (true) {
			try {
				FindIterable<Document> iter = 
						db.getCollection("article").find(where).sort(orderby).limit(100);
				
				if (!iter.iterator().hasNext()) {
					System.out.println(new Date() + "\t[SLEEP] ...5000ms");
					Thread.sleep(5000);
					continue;
				}
				
				iter.forEach( (Document doc) -> {
					ObjectId board_id = doc.getObjectId("board_id");
					String community = doc.getString("community");

					try {
						
						String contents = ContentsParserFactory.create(
								community, doc.getString("url")).parse();
						
						Document board = db.getCollection("board")
							.find(new Document("_id", board_id))
							.projection(new Document("imagedown", true)).first();
						
						
						if (board == null || board.getBoolean("imagedown") == null) {
							// not exist board or not exist imagedown field
							
						} else if (board.getBoolean("imagedown") == false) {
							// set false imagedown flag
							
						} else if (board.getBoolean("imagedown") == true) {
							// image down
							org.jsoup.nodes.Document html = Jsoup.parse(
									contents
							);
							
							Elements uploadedImages = html.select("img[uploaded=true]");
							for (Element image : uploadedImages) {
								String imageUrl = image.attr("src");
								String filename = doc.getObjectId("_id") + "_" + image.hashCode()
										+ imageUrl.substring(imageUrl.lastIndexOf('.'));
								FileURL.download(imageUrl, "public\\images\\" + 
										board_id.toString() + "\\" + filename);
								image.select("img").attr("src", "/images/" +  board_id.toString() + "/" + filename);
							}
							
							contents = html.toString();
						}
						
						db.getCollection("article").updateOne(
							new Document("_id", doc.getObjectId("_id")),
							new Document("$set", new Document("contents", contents))
						);
						System.out.println(new Date() + "\t[DONE] " + doc.getObjectId("_id") + 
								"\t" + community + 
								"\t" + board_id);
						
					} catch (Exception e) {
						e.printStackTrace();
						db.getCollection("article").updateOne(
								new Document("_id", doc.getObjectId("_id")),
								new Document("$set", new Document("contents", false))
							);
						System.out.println(new Date() + "\t[FAIL] " + doc.getObjectId("_id") + 
								"\t" + community + 
								"\t" + board_id);
					}
				});
					
			} catch (Exception e) {
				e.printStackTrace();
				Thread.sleep(5000);
			}
		}
		
	}
	
}

