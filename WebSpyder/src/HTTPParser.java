import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.StringTokenizer;

public class HTTPParser implements IParser {

	private AbstractMap<String, Integer> _pageWordCount;

	public HTTPParser() {
		this._pageWordCount = new HashMap<String, Integer>();
	}

	@Override
	public AbstractMap<String, Integer> parse(String text) {

		StringTokenizer stk = new StringTokenizer(text,
				" \t\n\r\f.,:-–_?!№—«»“©@!#$%^&~`'\"0123456789()[]{}*/←→↓",
				false);

		String token = null;
		Integer frequency = 0;
		while (stk.hasMoreTokens()) {

			token = stk.nextToken();			

			if (this._pageWordCount.containsKey(token)) {
				frequency = this._pageWordCount.get(token);
				this._pageWordCount.put(token, ++frequency); 
			} else {
				this._pageWordCount.put(token, 1); 
			}

		}
		return this._pageWordCount;
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
