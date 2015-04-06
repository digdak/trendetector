package trendetector.crawler;

import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import trendetector.crawler.parser.ArticleListParser;
import trendetector.crawler.parser.ArticleListParserFactory;
import trendetector.crawler.parser.ArticleParseError;
import beom.api.connect.db.mysql.MySqlDBConnection;


public class ArticleCrawler implements Runnable {
	private int board_id;
	private String community;
	private String url;
	private SimpleDateFormat dateToStrFormat;
	
	public ArticleCrawler(int board_id, String community, String url) throws Exception {
		this.board_id = board_id;
		this.community = community;
		this.url = url;
		this.dateToStrFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	@Override
	public void run() {
		MySqlDBConnection mysql = null;
		PreparedStatement pstmt  = null;
		
		try {
			mysql = new MySqlDBConnection();
			mysql.connect("127.0.0.1", 3306, "trendetector", "root", "root12!#");
			
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		try {
			pstmt = mysql.createPreparedStatement(
					"INSERT INTO article_list (board_id, article_no, subject, author, replies, hit, date, url) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
					"replies = ?, hit = ?"
					);
			
		} catch (Exception e) {
			e.printStackTrace();
			mysql.close();
			return;
		}
		
		long now = new Date().getTime();
		ArticleListParser parser = ArticleListParserFactory.create(community, url);
		ArticleParseError callbackParseError = (e, article) -> {
			System.out.println(article.getArticleNo());
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
					if (now - article.getDate().getTime() > 1 * 60 * 60 * 1000) {
						break parserLoop;
					}
					
					pstmt.setInt(1, board_id);
					pstmt.setInt(2,  article.getArticleNo());
					pstmt.setString(3, article.getSubject());
					pstmt.setString(4,  article.getAuthor());
					pstmt.setInt(5,  article.getReplies());
					pstmt.setInt(6, article.getHit());
					pstmt.setString(7, dateToStrFormat.format(article.getDate()));
					pstmt.setString(8, article.getUrl());
					pstmt.setInt(9,  article.getReplies());
					pstmt.setInt(10, article.getHit());
					
					mysql.executeUpdate(pstmt);
				}
				
			} while (parser.nextPage());
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try { pstmt.close(); } catch (Exception e) { }
		mysql.close();
	}
	
}
