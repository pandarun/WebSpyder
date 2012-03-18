import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HTTPGrabber implements IGrabber {

	public String grab(String url) {

		String textResults = null;

		try {
			Document document = Jsoup.connect(url).get();
			textResults = document.body().text();
		} catch (IOException e) {
			System.out.print(e.getMessage());
		}

		return textResults;
	}

}
