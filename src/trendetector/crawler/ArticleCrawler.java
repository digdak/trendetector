package trendetector.crawler;

import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import trendetector.crawler.parser.ArticleListParser;
import trendetector.crawler.parser.ArticleListParserFactory;
import trendetector.crawler.parser.ArticleParseError;
import trendetector.mongodb.MongoDB;

import com.mongodb.client.model.UpdateOptions;


public class ArticleCrawler implements Runnable {
	private ObjectId board_id;
	private String community;
	private String url;
	
	public ArticleCrawler(ObjectId board_id, String community, String url) throws Exception {
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
//			System.out.println(article.getArticleNo());
			e.printStackTrace();
		};
		
		try {
			parserLoop: do {
				List<Article> articleList = parser.parse(callbackParseError);
				System.out.println(new Date() + "\t\tdone: " + parser.getUrl());
				
				if (articleList.isEmpty()) {
					break;
				}
				
				for (Article article : articleList) {
					if (now - article.getDate().getTime() > 3 * 60 * 60 * 1000) {
						break parserLoop;
					}
					
					doc.append("article_no", article.getArticleNo())
						.append("subject", article.getSubject())
						.append("author", article.getAuthor())
						.append("replies", article.getReplies())
						.append("hit", article.getHit())
						.append("date", article.getDate())
						.append("url", article.getUrl());
					
					where.append("article_no", article.getArticleNo());
					
					MongoDB.db.getCollection("article").updateOne(where, update, opt);
				}
			} while (parser.nextPage());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
