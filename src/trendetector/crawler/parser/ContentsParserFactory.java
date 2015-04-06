package trendetector.crawler.parser;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ContentsParserFactory {

	public static ContentsParser create(String community, String url) {
		switch (community) {
		case "CL":
			return new ContentsParser(url) {
				@Override
				public String parse() throws IOException {
					Document doc = Jsoup.connect(this.getUrl()).get();
					Elements content = doc.select(".resContents");
					content.select(".signature").remove();	// 서명란 제거
					Elements uploadedImages = content.select(".attachedImage img");
					
					for (Element image : uploadedImages) {
						image.attr("uploaded", "true");
					}
					
					return content.html();
				}
			};
			
		case "SR":
			return new ContentsParser(url) {
				@Override
				public String parse() throws IOException {
					Document doc = Jsoup.connect(this.getUrl()).get();
					Elements content = doc.select("#userct");
					Elements uploadedImages = content.select("img[alt]");
					
					for (Element image : uploadedImages) {
						image.attr("uploaded", "true");
					}
					
					return content.html();
				}
			};
		}
		
		return null;
	}
	
}

