import java.util.AbstractMap;
import java.util.HashMap;
import java.util.StringTokenizer;

public class HTTPParser implements IParser {

	public HTTPParser() {}

	@Override
	public AbstractMap<String, Integer> parse(String text) {
		if (text == null ) { return null;}		
		return parsed(text);
	}

	private AbstractMap<String, Integer>  parsed(String text) {
		
		AbstractMap<String, Integer> _pageWordCount =  new HashMap<String, Integer>();;
		
		StringTokenizer stk = new StringTokenizer(	text,
													" \t\n\r\f.,:-–_?!№—«»“©@!#$%^&~`'\"0123456789()[]{}*/←→↓",
													false);

		String token = null;
		Integer frequency = 0;
		while (stk.hasMoreTokens()) {
			token = stk.nextToken();			

			if (_pageWordCount.containsKey(token)) {
				frequency = _pageWordCount.get(token);
				_pageWordCount.put(token, ++frequency); 
			} else {
				_pageWordCount.put(token, 1); 
			}
			
		}
		return _pageWordCount;
	}	
}
