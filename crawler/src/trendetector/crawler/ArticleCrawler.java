package trendetector.crawler;

import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import trendetector.crawler.parser.ArticleListParser;
import trendetector.crawler.parser.ArticleListParserFactory;
import trendetector.crawler.parser.ArticleParseError;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;


public class ArticleCrawler implements Runnable {
	private ObjectId board_id;
	private String community;
	private String url;
	private MongoDatabase db;
	
	public ArticleCrawler(MongoDatabase db, ObjectId board_id, String community, String url) throws Exception {
		this.db = db;
		this.board_id = board_id;
		this.community = community;
		this.url = url;
	}

	@Override
	public void run() {
		Document doc = new Document();
		doc.append("community", community)
			.append("board_id", board_id);
		
		Document where = new Document();
		where.append("board_id", board_id);
		
		Document update = new Document("$set", doc);
		
		UpdateOptions opt = new UpdateOptions();
		opt.upsert(true);
		
		long now = new Date().getTime();
		ArticleListParser parser = ArticleListParserFactory.create(community, url);
		ArticleParseError callbackParseError = (e, article) -> {
			if (article != null)
				System.err.println(article.getArticleNo());
			e.printStackTrace();
		};
		
		try {
			parserLoop: do {
				List<Article> articleList = parser.parse(callbackParseError);
				
				if (articleList.isEmpty()) {
					break;
				}
				
				for (Article article : articleList) {
					if (now - article.getDate().getTime() > 24 * 60 * 60 * 1000) {
						break parserLoop;
					}
					
					doc.append("article_no", article.getArticleNo())
						.append("subject", article.getSubject())
						.append("author", article.getAuthor())
						.append("replies", article.getReplies())
						.append("hit", article.getHit())
						.append("votes", article.getVotes())
						.append("date", article.getDate())
						.append("url", article.getUrl());
					
					where.append("article_no", article.getArticleNo());
					
					if (this.db.getCollection("article").count(where) > 0) {
						doc.remove("date");
					}
					
					UpdateResult ur = this.db.getCollection("article")
							.updateOne(
								where, update, opt
							);
					
					if (ur.getMatchedCount() == 0) {
						System.out.println(
							new Date() + "\t[DONE] "
							+ ur.getUpsertedId().asObjectId().getValue()
							+ "\t" + article.getSubject()
						);
					}
				}
			} while (parser.nextPage());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
