package trendetector.crawler.parser;

import java.io.IOException;


public abstract class ContentsParser {
	private String url;
	
	public ContentsParser(String url) {
		this.setUrl(url);
	}
	
	public String getUrl() {
		return url;
	}
	
	protected void setUrl(String url) {
		this.url = url;
	}

	public abstract String parse() throws IOException;
}
