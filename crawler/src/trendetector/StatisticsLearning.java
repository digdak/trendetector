package trendetector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import kr.co.shineware.nlp.komoran.core.analyzer.Komoran;
import kr.co.shineware.util.common.model.Pair;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import trendetector.crawler.Article;
import trendetector.crawler.FileURL;
import trendetector.crawler.URLStringUtil;
import trendetector.crawler.parser.ArticleListParser;
import trendetector.crawler.parser.ArticleParseError;
import trendetector.crawler.parser.ContentsParserFactory;
import trendetector.mongodb.MongoDB;
import trendetector.nlp.ExtractingIndexTerms;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

public class StatisticsLearning {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		Komoran komoran = new Komoran("crawler/models-full/");
		komoran.setUserDic("crawler/models-full/dic.user");
		komoran.setFWDic("crawler/models-full/fwd.user");
		MongoDatabase db = MongoDB.create();
		
		// 주의!! 같은 데이터 소스로부터 학습을 2번이상 반복할 경우 통계가 잘못될 수 있음.
		String startUrl = "http://bbs2.ruliweb.daum.net/gaia/do/ruliweb/default/community/325/list?pageIndex=2155&bbsId=G005&itemId=143";
		String community = "RW";
//		int pagecnt = 100;
		ArticleListParser parser = ArticleListParserFactory.create(community, startUrl);

		ArticleParseError callbackParseError = (e, article) -> {
			System.out.println(article.getArticleNo());
			e.printStackTrace();
		};

		UpdateOptions opt = new UpdateOptions();
		opt.upsert(true);
		HashMap<Integer, Integer> already = new HashMap<Integer, Integer>();
		
		try {
			 do {
//				 if (pagecnt-- == 0) {
//					 System.out.println("끄읕~~~~~~~~~");
//					 System.exit(0);
//				 }
				System.out.println("[CRAWLE]\t" +parser.getUrl());
				List<Article> articleList = parser
						.parse(callbackParseError);

				if (articleList.isEmpty()) {
					System.out.println("EMPTTTTTTTTTTTTTTTTTTTTYYYYYYYYYYY!!!!!!!!!!!");
					break;
				}

				for (Article article : articleList) {
					if (already.get(article.getArticleNo()) != null) {
						continue;
					}
					already.put(article.getArticleNo(), 1);
					
					String subject = article.getSubject();
					String contents = ContentsParserFactory.create(
							community, article.getUrl()).parse();
					contents = Jsoup.parse(contents).text();
					subject = subject.replaceAll("[\\n|\\r|(|)|\\[|\\]|{|}|'|“|”|\"|`|.|,]", " @ ");
					contents = contents.replaceAll("[\\n|\\r|(|)|\\[|\\]|{|}|'|“|”|\"|`|.|,]", " @ ");
					
					if (contents.length() > 30000) {
						contents = contents.substring(0, 30000);
					}
					
					String strAnalyze = subject + " @ " + contents;
					
					HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
					List<List<Pair<String, String>>> result = null;
					result = komoran.analyze(strAnalyze);
					for (List<Pair<String, String>> eojeolResult : result) {
						Pair<List<Pair<String, String>>, List<Pair<String, String>>> keys
							= ExtractingIndexTerms.getKeywords(eojeolResult);
						
						for (Pair<String, String> mainKeyword : keys.getFirst()) {
							hashMap.put(mainKeyword.getFirst(), 1);
						}
						
						for (Pair<String, String> subKeyword : keys.getSecond()) {
							hashMap.put(subKeyword.getFirst(), 1);
						}
					}
					
				
					UpdateResult ur = db.getCollection("statistics").updateOne(
							new org.bson.Document("_id", 0),
							new org.bson.Document("$inc", new org.bson.Document("totalcnt", 1)),
							opt
					);
					
					if (ur.getMatchedCount() == 0 && ur.getModifiedCount() == 0 && ur.getUpsertedId() == null) {
						System.out.println("total count errorrrrrrrrrrrrrrr");
						System.out.println(article.getUrl());
						System.exit(1);
					}
					
					Iterator<String> keys = hashMap.keySet().iterator();
					
					while (keys.hasNext()) {
						String key = keys.next();
						if (key.length() < 2 || key.length() > 15) {
							continue;
						}
						
						UpdateResult ur2 = db.getCollection("statistics.keywords").updateOne(
								new org.bson.Document("_id", key),
								new org.bson.Document("$inc", new org.bson.Document("cnt", 1)),
								opt
						);
						
						if (ur2.getMatchedCount() == 0 && ur2.getModifiedCount() == 0 && ur2.getUpsertedId() == null) {
							System.out.println("keyword update errorrrrrrrrrrrrrr");
							System.out.println(article.getUrl());
							System.exit(1);
						}
					}
					
					System.out.println("[DONE]\t" + article.getUrl());
				}
				
			} while (parser.nextPage());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class ArticleListParserFactory {

	public static ArticleListParser create(String community, String url) {
		switch (community) {
		case "CL":
			return new ClienParser(url);
		case "SR":
			return new SLRClubParser(url);
		case "OU":
			return new TodayHumorParser(url);
		case "DD":
			return new DogDripParser(url);
		case "MP":
			return new MLBPARKParser(url);
		case "BD":
			return new BobaeDreamParser(url);
		case "PP":
			return new PpomPpuParser(url);
		case "RW":
			return new RuliwebParser(url);
		case "CK":
			return new cookParser(url);
		}
		return null;
	}
	
}

// 4월 1일까지 완료
class ClienParser extends ArticleListParser {
	public ClienParser(String url) {
		super(url);
	}

	public List<Article> parse(ArticleParseError parseError)
			throws IOException {
		List<Article> articleList = new ArrayList<Article>();
		Document doc = new Document(this.getUrl());
		doc.html(FileURL.getHtml(this.getUrl(), "UTF-8"));
		Elements items = doc.select(".board_main .mytr");

		/* set Next Page URL */
		this.setNextPageUrl(doc.select(".paging .cur_page + a").attr(
				"abs:href"));

		for (Element item : items) {
			Article article = new Article();

			try {
				Elements td = item.select("td");

				/* 관리자가 삭제한 게시글 예외처리 */
				if (td.get(3).select("span").attr("title")
						.isEmpty()) {
					continue;
				}

				article.setArticleNo(Integer.parseInt(td.get(0)
						.text()));
				article.setSubject(td.get(1).select("a").text());
				article.setUrl(td.get(1).select("a")
						.attr("abs:href"));

				articleList.add(article);

			} catch (Exception e) {
				parseError.callback(e, article);
			}
		}

		return articleList;
	}

}

// 4월 1일까지 완료
class SLRClubParser extends ArticleListParser {

	public SLRClubParser(String url) {
		super(url);
	}

	@Override
	public List<Article> parse(ArticleParseError parseError)
			throws IOException {
		List<Article> articleList = new ArrayList<Article>();
		Document doc = new Document(this.getUrl());
		doc.html(FileURL.getHtml(this.getUrl(), "UTF-8"));
		Elements items = doc.select("#bbs_list tbody tr");

		/* set Next Page URL */
		try {
			int page = Integer.parseInt(doc.select(
					".list_num #actpg").text());
			page++;
			this.setNextPageUrl(URLStringUtil.urlAddQuery(
					this.getUrl(), "page", page + ""));
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

				article.setArticleNo(Integer.parseInt(item
						.select(".list_num").text()));
				article.setSubject(item.select(".sbj a").text());
				article.setUrl(item.select(".sbj a").attr(
						"abs:href"));

				articleList.add(article);

			} catch (Exception e) {
				parseError.callback(e, article);
			}
		}

		return articleList;
	}

}

// 4월 1일까지 완료
class TodayHumorParser extends ArticleListParser {

	public TodayHumorParser(String url) {
		super(url);
	}

	public List<Article> parse(ArticleParseError parseError)
			throws IOException {
		List<Article> articleList = new ArrayList<Article>();
		Document doc = new Document(this.getUrl());
		doc.html(FileURL.getHtml(this.getUrl(), "UTF-8"));
		Elements items = doc.select(".table_list tbody .view");
		items.remove();

		/* set Next Page URL */
		this.setNextPageUrl(doc.select(".table_list tbody tr tbody a")
				.last().attr("abs:href"));

		for (Element item : items) {
			Article article = new Article();

			try {
				article.setArticleNo(Integer.parseInt(item
						.select(".no").text()));
				article.setSubject(item.select(".subject a")
						.text());
				article.setUrl(item.select(".subject a").attr(
						"abs:href"));

				articleList.add(article);

			} catch (Exception e) {
				parseError.callback(e, article);
			}
		}

		return articleList;
	}

}

// 4월 1일까지 완료
class DogDripParser extends ArticleListParser {

	public DogDripParser(String url) {
		super(url);
	}

	public List<Article> parse(ArticleParseError parseError)
			throws IOException {
		List<Article> articleList = new ArrayList<Article>();
		Document doc = new Document(this.getUrl());
		doc.html(FileURL.getHtml(this.getUrl(), "UTF-8"));
		Elements items = doc.select(".boardList tbody tr");

		/* set Next Page URL */
		this.setNextPageUrl(doc.select(".pagination strong + a").attr(
				"abs:href"));

		for (Element item : items) {
			Article article = new Article();

			try {
				String url = item.select(".title a").attr(
						"abs:href");
				article.setArticleNo(Integer.parseInt(url
						.substring(url.lastIndexOf("/") + 1)));
				article.setSubject(item.select(".title a")
						.text());
				article.setUrl(url);

				articleList.add(article);

			} catch (Exception e) {
				parseError.callback(e, article);
			}
		}

		return articleList;
	}

}

// 미완
class MLBPARKParser extends ArticleListParser {

	public MLBPARKParser(String url) {
		super(url);
	}

	@Override
	public List<Article> parse(ArticleParseError parseError)
			throws IOException {
		List<Article> articleList = new ArrayList<Article>();

		Document doc = new Document(this.getUrl());
		doc.html(FileURL.getHtml(this.getUrl(), "EUC-KR"));
		Elements items = doc
				.select("#container tbody tbody tr[height]");

		/* set Next Page URL */
		try {
			int page = Integer.parseInt(doc.select(".paging font")
					.text());
			page++;
			this.setNextPageUrl(URLStringUtil.urlAddQuery(
					this.getUrl(), "cpage", page + ""));
		} catch (Exception e) {
			this.setNextPageUrl(null);
		}

		for (Element item : items) {
			Article article = new Article();

			try {
				Elements tds = item.select("tbody tr td");

				article.setArticleNo(Integer.parseInt(tds
						.get(0).text()));
				article.setSubject(tds.get(1).select("a")
						.get(0).text());
				article.setUrl(tds.get(1).select("a").get(0)
						.attr("abs:href"));

				articleList.add(article);

			} catch (Exception e) {
				parseError.callback(e, article);
			}
		}

		return articleList;
	}

}

// 미완
class BobaeDreamParser extends ArticleListParser {

	public BobaeDreamParser(String url) {
		super(url);
	}

	@Override
	public List<Article> parse(ArticleParseError parseError)
			throws IOException {
		List<Article> articleList = new ArrayList<Article>();
		Document doc = new Document(this.getUrl());
		doc.html(FileURL.getHtml(this.getUrl(), "UTF-8"));
		Elements items = doc.select(".cList table tbody tr[itemtype]");

		/* set Next Page URL */
		try {
			int page = Integer.parseInt(doc.select(".current")
					.text());
			page++;
			this.setNextPageUrl(URLStringUtil.urlAddQuery(
					this.getUrl(), "page", page + ""));
		} catch (Exception e) {
			this.setNextPageUrl(null);
		}

		for (Element item : items) {
			Article article = new Article();

			try {
				String url = item.select(".pl14 a").get(0).attr("abs:href");
				article.setArticleNo(Integer.parseInt(url.split("&No=")[1].split("&")[0]));
				article.setSubject(item.select(".pl14 .bsubject").text());
				article.setUrl(url);
				
				articleList.add(article);

			} catch (Exception e) {
				parseError.callback(e, article);
			}
		}
		return articleList;
	}
}

// 미완
class PpomPpuParser extends ArticleListParser {

	public PpomPpuParser(String url) {
		super(url);
	}

	public List<Article> parse(ArticleParseError parseError)
			throws IOException {
		List<Article> articleList = new ArrayList<Article>();
		Document doc = new Document(this.getUrl());
		doc.html(FileURL.getHtml(this.getUrl(), "EUC-KR"));
		Elements items = doc.select("#revolution_main_table tbody tr");

		/* set Next Page URL */
		try {
			int page = Integer.parseInt(doc.select(".page_inert")
					.text());
			page++;
			this.setNextPageUrl(URLStringUtil.urlAddQuery(
					this.getUrl(), "page", page + ""));
		} catch (Exception e) {
			this.setNextPageUrl(null);
		}

		for (Element item : items) {
			Article article = new Article();
			try {
				if (!(item.attr("class").equals("list0") || item
						.attr("class").equals("list1"))) {
					continue;
				}

				if (!item.select("strike").isEmpty()) {
					continue;
				}

				Elements td = item.select("td");

				article.setArticleNo(Integer.parseInt(td.get(0)
						.text()));
				article.setSubject(item.select(".list_title")
						.text());
				article.setUrl(td.get(2).select("a")
						.attr("abs:href"));

				articleList.add(article);

			} catch (Exception e) {
				parseError.callback(e, article);
			}
		}

		return articleList;
	}

}

// 5월 16일까지 완료
class RuliwebParser extends ArticleListParser {

	public RuliwebParser(String url) {
		super(url);
	}

	public List<Article> parse(ArticleParseError parseError)
			throws IOException {
		List<Article> articleList = new ArrayList<Article>();
		Document doc = new Document(this.getUrl());
		doc.html(FileURL.getHtml(this.getUrl(), "UTF-8"));
		Elements items = doc.select(".tbl tbody tr");
		Elements removes = items.select(".emph");
		items.removeAll(removes);

		/* set Next Page URL */
		try {
			Elements paging = doc
					.select(".paging_comm .num:has(.ir_pm)");
			int page = Integer.parseInt(paging.html().split(
					"</span>")[1]);
			page++;
			this.setNextPageUrl(URLStringUtil.urlAddQuery(
					this.getUrl(), "pageIndex", page + ""));
		} catch (Exception e) {
			this.setNextPageUrl(null);
		}

		for (Element item : items) {
			Article article = new Article();
			try {

				Elements subject = item.select(".subject a");
				article.setArticleNo(Integer.parseInt(subject
						.attr("id").split("_")[1]));
				article.setSubject(subject.attr("title"));
				article.setUrl(subject.attr("abs:href"));

				articleList.add(article);

			} catch (Exception e) {
				parseError.callback(e, article);
			}
		}

		return articleList;
	}

}

// 미완
class cookParser extends ArticleListParser {

	public cookParser(String url) {
		super(url);
	}

	public List<Article> parse(ArticleParseError parseError)
			throws IOException {
		List<Article> articleList = new ArrayList<Article>();
		Document doc = new Document(this.getUrl());
		doc.html(FileURL.getHtml(this.getUrl(), "UTF-8"));
		Elements items = doc
				.select("#column2 #list_table #bbs tbody tr:not(.noticeList)");

		/* set Next Page URL */
		try {
			this.setNextPageUrl(doc.select(".selected + a").attr(
					"abs:href"));
		} catch (Exception e) {
			this.setNextPageUrl(null);
		}

		for (Element item : items) {
			Article article = new Article();
			try {
				Elements td = item.select("td");

				article.setArticleNo(Integer.parseInt(td.get(0)
						.text()));
				article.setSubject(td.get(1).select("a").text());
				article.setUrl(td.get(1).select("a")
						.attr("abs:href"));

				articleList.add(article);

			} catch (Exception e) {
				parseError.callback(e, article);
			}
		}

		return articleList;
	}

}
