import java.io.IOException;
import java.io.FileWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;

public class WebSpyder {
	
	private static  AbstractMap<String,Boolean> urlToVisit = new HashMap<String,Boolean>();	
	
	public static void main(String[] args) {

		Execute(args);
	}

	private static void Execute(String[] args) {
		// command line args...
		String outputFile = args[0];
		String url = args[1];

		// page parsing here...
		urlToVisit.put(url,false);
		GrabManager manager = new GrabManager(urlToVisit);
		manager.run();		
	}

	@Deprecated
	private static void singleThreadSpyder(String outputFile, String url) {
		IGrabber grabber = HTTPGrabberFactory.getInstance().createGrabber();
		IParser parser = HTTPParserFactory.getInstance().createParser();

		String textRestuls = grabber.grab(url);

		TreeMap<String, Integer> pageWordCount = (TreeMap<String, Integer>) parser
				.parse(textRestuls);

		try {
			saveResultsToFile(outputFile, pageWordCount);
		} catch (IOException e) {
			System.out.print(e.getMessage());
		}
	}
	@Deprecated
	private static void saveResultsToFile(String outputFile,
			AbstractMap<String, Integer> pageWordCount) throws IOException {
		// word - frequency file output
		FileWriter writer = new FileWriter(outputFile);
		for (String key : pageWordCount.keySet()) {

			writer.write(key + " " + pageWordCount.get(key) + "\n");
		}

		writer.close();
	}
}
