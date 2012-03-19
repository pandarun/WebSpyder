import java.io.IOException;
import java.util.Collection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HTTPGrabber implements IGrabber {
	
	private Document document;
	private Collection<String> urlList;
	
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

	public  Collection<String> links() {		
		
		if(document == null) return null;		
		
        Elements links = document.select("a[href]");
        for (Element element : links) {
			this.urlList.add(element.text());
		}
        return this.urlList;	
	}
}
