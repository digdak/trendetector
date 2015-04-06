package trendetector;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import trendetector.crawler.FileURL;
import trendetector.crawler.parser.ContentsParserFactory;
import beom.api.connect.db.mysql.MySqlDBConnection;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;


public class ContentsCrawlEngine {
	
	public static void main(String[] args) throws ClassNotFoundException, InterruptedException {
		MySqlDBConnection mysql = new MySqlDBConnection();
		PreparedStatement pstmt = null;
		
		MongoCollection<org.bson.Document> mongoDbCollection = null;
		
		Gson gson = new Gson();
		
		String selectQuery = "SELECT board_list.community, article_list.board_id, article_list.article_no, "
				+ "article_list.subject, article_list.url " 
				+ "FROM article_list inner join board_list on board_list.id = article_list.board_id "
				+ "WHERE article_list.flag = 'D'  order by article_list.date desc limit 100";
		
		Whitelist whitelist = Whitelist.basic();
		whitelist.addTags("div");
		whitelist.addAttributes("img", "src", "uploaded", "width", "height");
		whitelist.addProtocols("img", "src", "http", "https");
		whitelist.addAttributes("iframe", "src", "width", "height", "type");
		whitelist.addAttributes("embed", "src", "width", "height", "type");
		whitelist.addAttributes("object", "data", "width", "height", "type");
		
		while (true) {
			try {
				mysql.connect("127.0.0.1", 3306, "trendetector", "root", "root12!#");
				pstmt = mysql.createPreparedStatement(
						"UPDATE article_list SET flag = ? WHERE board_id = ? and article_no = ?"
						);
				
				mongoDbCollection = new MongoClient("localhost", 27017).getDatabase("trendetector")
										.getCollection("contents");
				
				while (true) {
					ResultSet rs = mysql.execute(selectQuery);
					Contents contents = new Contents();
					
					if (!rs.next()) {
						System.out.println(new Date() + "\tNot data...");
						rs.close();
						Thread.sleep(5000);
						continue;
					}
					
					do {
						try {
							contents.setArticleNo(rs.getInt("article_list.article_no"));
							contents.setBoardId(rs.getInt("article_list.board_id"));
							contents.setCommunity(rs.getString("board_list.community"));
							contents.setUrl(rs.getString("article_list.url"));
							contents.setSubject(rs.getString("article_list.subject"));
							
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}
							
						try {
							
							Document doc = Jsoup.parse(
									Jsoup.clean(
									ContentsParserFactory.create(contents.getCommunity(), contents.getUrl()).parse(), 
									contents.getUrl(), whitelist)
							);
							
							Elements uploadedImages = doc.select("img[uploaded=true]");
							for (Element image : uploadedImages) {
								String imageUrl = image.attr("src");
								String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
								FileURL.download(imageUrl, "D:\\Beom\\workspace\\trendetector\\public\\images\\" + contents.getCommunity() + "\\" + 
										contents.getBoardId() + "\\" + filename);
								image.select("img").attr("src", "/images/" +  contents.getCommunity() + "/" + contents.getBoardId() + "/" + filename);
							}
							
							contents.setContents(doc.toString());
							
							mongoDbCollection.insertOne(org.bson.Document.parse(gson.toJson(contents)));
							pstmt.setString(1, "T");
							
						} catch (Exception e) {
							e.printStackTrace();
							pstmt.setString(1, "F");
						}
						
						pstmt.setInt(2, contents.getBoardId());
						pstmt.setInt(3, contents.getArticleNo());
						mysql.executeUpdate(pstmt);
						
					} while (rs.next());
					
					rs.close();
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				try { pstmt.close(); } catch (Exception e2) { }
				mysql.close();
				Thread.sleep(5000);
			}
			break;
		}
	}
	
}


class Contents {
	private int article_no;
	private int board_id;
	private String subject;
	private String url;
	private String community;
	private String contents;
	
	public int getArticleNo() {
		return article_no;
	}
	
	public void setArticleNo(int articleNo) {
		this.article_no = articleNo;
	}
	
	public int getBoardId() {
		return board_id;
	}
	
	public void setBoardId(int boardId) {
		this.board_id = boardId;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getCommunity() {
		return community;
	}
	
	public void setCommunity(String community) {
		this.community = community;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}
	
}
