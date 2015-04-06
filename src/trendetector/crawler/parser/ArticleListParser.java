package trendetector.crawler.parser;

import java.io.IOException;
import java.util.List;

import trendetector.crawler.Article;

public abstract class ArticleListParser {
	private String url;
	private String nextPageUrl;
	
	public ArticleListParser(String url) {
		this.setUrl(url);
		this.setNextPageUrl(null);
	}

	public String getUrl() {
		return url;
	}

	protected void setUrl(String url) {
		this.url = url;
	}

	public String getNextPageUrl() {
		return nextPageUrl;
	}

	protected void setNextPageUrl(String nextPageUrl) {
		this.nextPageUrl = nextPageUrl;
	}
	
	public boolean nextPage() {
		setUrl(getNextPageUrl());
		setNextPageUrl(null);

		return !(getUrl() == null || getUrl().trim().equals(""));
	}

	public abstract List<Article> parse(ArticleParseError parseError) throws IOException;

}
