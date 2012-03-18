import java.util.AbstractMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class HTTPParser implements IParser {

	private TreeMap<String, Integer> _pageWordCount;

	public HTTPParser() {
		this._pageWordCount = new TreeMap<String, Integer>();
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
			System.out.println(token);

			if (this._pageWordCount.containsKey(token)) {
				frequency = this._pageWordCount.get(token);
				this._pageWordCount.put(token, ++frequency);
			} else {
				this._pageWordCount.put(token, 1); // added comment
			}

		}
		return this._pageWordCount;
	}

}
