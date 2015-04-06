package trendetector;

import java.sql.ResultSet;
import java.util.Date;

import trendetector.crawler.ArticleCrawler;
import beom.api.connect.db.mysql.MySqlDBConnection;


public class ListCrawlEngine {

	public static void main(String[] args) throws ClassNotFoundException, InterruptedException {
		MySqlDBConnection mysql = new MySqlDBConnection();

		while (true) {
			try {
				mysql.connect("127.0.0.1", 3306, "trendetector", "root", "root12!#");
				
				while (true) {
					ResultSet rs = mysql.execute("SELECT id, community, name, url FROM board_list");
					
					while (rs.next()) {
						try {
							int board_id = rs.getInt("id");
							String community = rs.getString("community");
							String name = rs.getString("name");
							String url = rs.getString("url");
							
							System.out.println(new Date() + "\t[Crawl..] " + board_id + "\t" + community + "\t" + name + "\t" + url);
							new ArticleCrawler(board_id, community, url).run();
							
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
					
					rs.close();
					
					Thread.sleep(1000);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				mysql.close();
				Thread.sleep(5000);
			}
		}
	}

}
