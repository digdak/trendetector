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
		whitelist.addAttributes("img", "src", "uploaded", "width", "height");
		whitelist.addProtocols("img", "src", "http", "https");
		whitelist.addAttributes("iframe", "src", "width", "height", "type");
		whitelist.addAttributes("embed", "src", "width", "height", "type");
		whitelist.addAttributes("object", "data", "width", "height", "type");
		whitelist.addAttributes(":all", "style");
	}
	
	public static ContentsParser create(String community, String url) {
		
		switch (community) {
		case "CL":
			return new ContentsParser(url) {
				@Override
				public String parse() throws IOException {
					Document doc = new Document(this.getUrl());
					doc.html(FileURL.getHtml(this.getUrl(), "UTF-8"));
					
					Elements contents = doc.select(".resContents");
					contents.select(".signature").remove();	// 서명란 제거
					Elements uploadedImages = contents.select(".attachedImage img");
					
					for (Element image : uploadedImages) {
						image.attr("uploaded", "true");
					}
					
					return Jsoup.clean(contents.html(), url, whitelist);
				}
			};
			
		case "SR":
			return new ContentsParser(url) {
				@Override
				public String parse() throws IOException {
					Document doc = new Document(this.getUrl());
					doc.html(FileURL.getHtml(this.getUrl(), "UTF-8"));
					
					Elements contents = doc.select("#userct");
					Elements uploadedImages = contents.select("img[alt]");
					
					for (Element image : uploadedImages) {
						image.attr("uploaded", "true");
					}
					
					return Jsoup.clean(contents.html(), url, whitelist);
				}
			};
			
		case "OU":
			return new ContentsParser(url) {
				@Override
				public String parse() throws IOException {
					Document doc = new Document(this.getUrl());
					doc.html(FileURL.getHtml(this.getUrl(), "UTF-8"));
					
					Elements contents = doc.select(".viewContent");
					
					return Jsoup.clean(contents.html(), url, whitelist);
				}
			};
			
		case "DD":
			return new ContentsParser(url) {
				@Override
				public String parse() throws IOException {
					Document doc = new Document(this.getUrl());
					doc.html(FileURL.getHtml(this.getUrl(), "UTF-8"));
					
					Elements contents = doc.select("#article_1");
					contents.select(".document_popup_menu").remove();	// 팝업메뉴 제거
					contents.select("wgtRv").remove();	// 추천 비추천 버튼 제거
					
					return Jsoup.clean(contents.html(), url, whitelist);
				}
			};
			
		case "MP":
			return new ContentsParser(url) {
				@Override
				public String parse() throws IOException {
					Document doc = new Document(this.getUrl());
					doc.html(FileURL.getHtml(this.getUrl(), "EUC-KR"));
					
					Element contents = doc.select(".G13 tbody div").get(0);	
					Elements uploadedImages = contents.select("a[onclick] img");
					
					for (Element image : uploadedImages) {
						image.attr("uploaded", "true");
					}
					
					return Jsoup.clean(contents.html(), url, whitelist);
				}
			};
			
		case "BD":
			return new ContentsParser(url) {
				@Override
				public String parse() throws IOException {
					Document doc = new Document(this.getUrl());
					doc.html(FileURL.getHtml(this.getUrl(), "UTF-8"));
					
					Elements bodyCont = doc.select(".bodyCont");
					Elements print_area2 = doc.select("#print_area2");
					
					return Jsoup.clean(bodyCont.html() + "<br></br>" +  print_area2.html(), url, whitelist);
				}
			};
		case "PP":
			return new ContentsParser(url) {
				@Override
				public String parse() throws IOException {
					Document doc = new Document(this.getUrl());
					doc.html(FileURL.getHtml(this.getUrl(), "EUC-KR"));
					
					Element contents = doc.select(".han").get(2);
					Elements uploadedImages = contents.select("#dqResizedImage0");
					
					for (Element image : uploadedImages) {
						image.attr("uploaded", "true");
						image.attr("width", "880");
					}
					
					return Jsoup.clean(contents.html(), url, whitelist);
				}
			};
		}
			
		
		return null;
	}
	
}

