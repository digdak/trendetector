package trendetector.crawler.parser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import trendetector.crawler.Article;
import trendetector.crawler.FileURL;
import trendetector.crawler.URLStringUtil;

public class ArticleListParserFactory {

	public static ArticleListParser create(String community, String url) {
		switch (community) {
		case "CL":
			return new ClienParser(url);
		case "SR":
			return new SLRClubParser(url);
		case "OU":
			return new TodayHumorParser(url);
		}
		
		return null;
	}
	
}


class ClienParser extends ArticleListParser {
	private Whitelist whitelist;	// 작성자가 text인 경우와 img인 경우 처리
	private SimpleDateFormat strToDateFormat;	// 작성일 parsing
	
	public ClienParser(String url) {
		super(url);
		this.whitelist = new Whitelist();
		this.whitelist.addAttributes("img", "src");
		this.whitelist.addProtocols("img", "src", "http", "https");
		this.strToDateFormat  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	public List<Article> parse(ArticleParseError parseError) throws IOException {
		List<Article> articleList = new ArrayList<Article>();
		Document doc = new Document(this.getUrl());
		doc.html(FileURL.getHtml(this.getUrl()));
		Elements items = doc.select(".board_main .mytr");
		
		/* set Next Page URL */
		this.setNextPageUrl(doc.select(".paging .cur_page + a").attr("abs:href"));
		
		for (Element item : items) {
			Article article = new Article();
			
			try {
				Elements td = item.select("td");
				
				/* 관리자가 삭제한 게시글 예외처리 */
				if (td.get(3).select("span").attr("title").isEmpty()) {
					continue;
				}
				
				article.setArticleNo(Integer.parseInt(td.get(0).text()));
				article.setSubject(td.get(1).select("a").text());
				
				/* 작성자가 img인 경우 src를 절대경로로 변경 */
				article.setAuthor(Jsoup.clean(td.get(2).html(), this.getUrl(), whitelist));
				
				/* replies가 있는 경우 [%d] 형태를 정규식으로 안에 숫자값만 검출 */
				String strReplies = td.get(1).select("span").text();
				if (!strReplies.isEmpty()) {
					article.setReplies(Integer.parseInt(strReplies.replaceAll("[\\[\\]]", "")));
				}
				
				article.setHit(Integer.parseInt(td.get(4).text()));
				article.setDate(strToDateFormat.parse(td.get(3).select("span").attr("title")));
				article.setUrl(td.get(1).select("a").attr("abs:href"));
				
				articleList.add(article);
				
			} catch (Exception e) {
				parseError.callback(e, article);
			}
		}
		
		return articleList;
	}
	
}


class SLRClubParser extends ArticleListParser {
	private Date lastDate;	// 마지막 파싱한 글의 시간
	private SimpleDateFormat strToDateFormat;	// String -> Date 변경 포멧
	private SimpleDateFormat dateToStrFormat;	// Date -> String 변경 포멧
	
	public SLRClubParser(String url) {
		super(url);
		this.lastDate = null;
		this.strToDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		this.dateToStrFormat = new SimpleDateFormat("yyyy/MM/dd");
	}

	@Override
	public List<Article> parse(ArticleParseError parseError) throws IOException {
		List<Article> articleList = new ArrayList<Article>();
		Document doc = new Document(this.getUrl());
		doc.html(FileURL.getHtml(this.getUrl()));
		Elements items = doc.select("#bbs_list tbody tr");
		
		/* set Next Page URL */
		try {
			int page = Integer.parseInt(doc.select(".list_num #actpg").text());
			page++;
			this.setNextPageUrl(URLStringUtil.urlAddQuery(this.getUrl(), "page", page + ""));
		} catch (Exception e) {
			this.setNextPageUrl(null);
		}
		
		for (Element item : items) {
			Article article = new Article();
			
			try {
				/* 공지사항의 경우 게시글 번호가 없으므로 예외처리 */
				if (item.select(".list_num").text().isEmpty()) {
					continue;
				}
				
				/* 답변게시글의 경우 맨앞에 img가 있는지 여부로 판단하여 예외처리 */
				if (!item.select(".sbj img").isEmpty()) {
					continue;
				}
				
				/* SLR클럽 게시글 중 HIT수가 100 이상인 것만 가져옴 */
				article.setHit(Integer.parseInt(item.select(".list_click").text()));
				if (article.getHit() < 100) {
					continue;
				}
				
				/* SLR클럽은 24시간 이전글까지는 작성일을 HH:mm:ss로 표현하는데,
				 * 다른 패턴인 경우 예외처리 */
				String strDate = item.select(".list_date").text();
				if (!Pattern.matches("[0-2][0-9]:[0-5][0-9]:[0-5][0-9]", strDate)) {
					continue;
				}
				
				article.setArticleNo(Integer.parseInt(item.select(".list_num").text()));
				article.setSubject(item.select(".sbj a").text());
				article.setAuthor(item.select(".list_name").text());
				
				/* 댓글 수 추출 시 제거되는 태그이므로 반드시 먼저 해야 함 */
				article.setUrl(item.select(".sbj a").attr("abs:href"));
				
				/* 댓글 수를 추출하기 위해 제목에서 a 태그로 감싸진 부분을 제거하고 파싱 */
				item.select(".sbj a").remove();
				String strReplies = item.select(".sbj").text();
				if (!strReplies.isEmpty()) {
					article.setReplies(Integer.parseInt(strReplies.replaceAll("[\\[\\]]", "")));
				}
				
				/* 목록에서 날짜를 확인할 수 있는 방법이 없으므로 마지막 파싱한 글의 시간을
				 * 기록해 두고 큰 차이로 증가한 경우 하루 전 글이 된 시점으로 판단함 */
				Date date = null;
				if (lastDate == null) {
					Date now = new Date();
					date = strToDateFormat.parse(dateToStrFormat.format(now) + " " + strDate);
					
					if (date.getTime() - now.getTime() > 12 * 60 * 60 * 1000) {
						date = new Date(date.getTime() - 24 * 60 * 60 * 1000);
					} 
					else if (now.getTime() - date.getTime() > 12 * 60 * 60 * 1000) {
						date = new Date(date.getTime() + 24 * 60 * 60 * 1000);
					}
				}
				else {
					date = strToDateFormat.parse(dateToStrFormat.format(lastDate) + " " + strDate);
					
					if (date.getTime() - lastDate.getTime() > 12 * 60 * 60 * 1000) {
						date = new Date(date.getTime() - 24 * 60 * 60 * 1000);
					}
				}
				lastDate = date;
				article.setDate(date);
				
				articleList.add(article);
				
			} catch (Exception e) {
				parseError.callback(e, article);
			}
		}
		
		return articleList;
	}
	
}


class TodayHumorParser extends ArticleListParser {
	private SimpleDateFormat strToDateFormat;	// 작성일 parsing
	
	public TodayHumorParser(String url) {
		super(url);
		this.strToDateFormat  = new SimpleDateFormat("yy/MM/dd HH:mm");
	}

	public List<Article> parse(ArticleParseError parseError) throws IOException {
		List<Article> articleList = new ArrayList<Article>();
		Document doc = new Document(this.getUrl());
		doc.html(FileURL.getHtml(this.getUrl()));
		Elements items = doc.select(".table_list tbody .view");
		items.remove();
		
		/* set Next Page URL */
		this.setNextPageUrl(doc.select(".table_list tbody tr tbody a").last().attr("abs:href"));
		
		for (Element item : items) {
			Article article = new Article();
			
			try {
				article.setArticleNo(Integer.parseInt(item.select(".no").text()));
				article.setSubject(item.select(".subject a").text());
				article.setAuthor(item.select(".name").text());
				
				/* replies가 있는 경우 [%d] 형태를 정규식으로 안에 숫자값만 검출 */
				Element font = item.select(".subject font").first();
				String strReplies = ( font != null ? font.text() : "");
				if (!strReplies.isEmpty()) {
					article.setReplies(Integer.parseInt(strReplies.trim().replaceAll("[\\[\\]]", "")));
				}
				
				article.setHit(Integer.parseInt(item.select(".hits").text()));
				article.setDate(strToDateFormat.parse(item.select(".date").text()));
				article.setUrl(item.select(".subject a").attr("abs:href"));
				
				articleList.add(article);
				
			} catch (Exception e) {
				parseError.callback(e, article);
			}
		}
		
		return articleList;
	}
	
}

