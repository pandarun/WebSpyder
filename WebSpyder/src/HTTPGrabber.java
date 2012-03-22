import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HTTPGrabber implements IGrabber {
	
	private Document document;
	
	public String grab(String url) {
				
		String textResults = null;

		try {
			this.document = Jsoup.connect(url).get();
			textResults = document.body().text();
		} catch (IOException e) {
			System.out.print(e.getMessage());
		}

		return textResults;
	}

	public void addLinksTo(ConcurrentMap<String, Boolean> frontier) {		
		
		if(document == null) return;		
		
        Elements links = document.select("a[href]");
        for (Element element : links) {
        	
        	String url = element.attr("abs:href");
        	frontier.putIfAbsent(url, false);       	
        	
		}
        
	}
}
