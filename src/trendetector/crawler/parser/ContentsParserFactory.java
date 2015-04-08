package trendetector.crawler.parser;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import trendetector.crawler.FileURL;

public class ContentsParserFactory {
	private static Whitelist whitelist;
	
	static {
		whitelist = Whitelist.basic();
		whitelist.addTags("div");
		whitelist.addAttributes("span", "style");
		whitelist.addAttributes("img", "src", "uploaded", "width", "height");
		whitelist.addProtocols("img", "src", "http", "https");
		whitelist.addAttributes("iframe", "src", "width", "height", "type");
		whitelist.addAttributes("embed", "src", "width", "height", "type");
		whitelist.addAttributes("object", "data", "width", "height", "type");
	}
	
	public static ContentsParser create(String community, String url) {
		
		switch (community) {
		case "CL":
			return new ContentsParser(url) {
				@Override
				public String parse() throws IOException {
					Document doc = new Document(this.getUrl());
					doc.html(FileURL.getHtml(this.getUrl()));
					
					Elements content = doc.select(".resContents");
					content.select(".signature").remove();	// 서명란 제거
					Elements uploadedImages = content.select(".attachedImage img");
					
					for (Element image : uploadedImages) {
						image.attr("uploaded", "true");
					}
					
					return Jsoup.clean(content.html(), url, whitelist);
				}
			};
			
		case "SR":
			return new ContentsParser(url) {
				@Override
				public String parse() throws IOException {
					Document doc = new Document(this.getUrl());
					doc.html(FileURL.getHtml(this.getUrl()));
					
					Elements content = doc.select("#userct");
					Elements uploadedImages = content.select("img[alt]");
					
					for (Element image : uploadedImages) {
						image.attr("uploaded", "true");
					}
					
					return Jsoup.clean(content.html(), url, whitelist);
				}
			};
			
		case "OU":
			return new ContentsParser(url) {
				@Override
				public String parse() throws IOException {
					Document doc = new Document(this.getUrl());
					doc.html(FileURL.getHtml(this.getUrl()));
					
					Elements content = doc.select(".viewContent");
					
					return Jsoup.clean(content.html(), url, whitelist);
				}
			};
		}
		
		return null;
	}
	
}

