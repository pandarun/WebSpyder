import java.io.IOException;
import java.util.AbstractCollection;
import java.util.concurrent.BlockingQueue;
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
			// TODO : add logging here
		} catch (IOException e) {
			System.out.print(e.getMessage());
			// TODO : add logging here
		}

		return textResults;
	}

	public void addLinksToFrontier(BlockingQueue<String> frontier, AbstractCollection<String> visited) {		
		
		if(document == null) return;		
		
        Elements links = document.select("a[href]");
        for (Element element : links) {
        	
        	String url = element.attr("abs:href");    	
        	
        	if ( !url.equals("") && !visited.contains(url) ) {
        		frontier.add(url);       	
        		// TODO : add logging here
			}    	
		}
        
	}
}
