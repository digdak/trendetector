package trendetector.test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;


public class Test {
	private static Whitelist whitelist;
	
	static {
		whitelist = Whitelist.basic();
		whitelist.addTags("div");
		whitelist.addAttributes("iframe", "src", "width", "height", "type");
		whitelist.addAttributes("embed", "src", "width", "height", "type");
		whitelist.addAttributes("object", "data", "width", "height", "type");
		whitelist.addAttributes(":all", "style");
	}

	public static void main(String[] args) {
		try {
//			
			String html = "<html><body><div class=\"abc\"><img style=\"display:none\" src=\"abc.jpg\"/><span>a</span><p>c</p><span style=\"type:bold\">b</span></div></body></html>";
			System.out.println(Jsoup.clean(html, whitelist));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
