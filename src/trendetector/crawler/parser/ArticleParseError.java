package trendetector.crawler.parser;

import trendetector.crawler.Article;

public interface ArticleParseError {
	public void callback(Exception e, Article article) ;
}
