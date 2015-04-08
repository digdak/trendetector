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
					System.out.println(new Date() + "\tNot data...");
					Thread.sleep(5000);
					continue;
				}
				
				iter.forEach( (Document doc) -> {
					try {
						ObjectId board_id = doc.getObjectId("board_id");
						
						String contents = ContentsParserFactory.create(
								doc.getString("community"), doc.getString("url")).parse();
						
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
								String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
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
						
					} catch (Exception e) {
						db.getCollection("article").updateOne(
								new Document("_id", doc.getObjectId("_id")),
								new Document("$set", new Document("contents", false))
							);
						e.printStackTrace();
					}
				});
					
			} catch (Exception e) {
				e.printStackTrace();
				Thread.sleep(5000);
			}
		}
		
	}
	
}

