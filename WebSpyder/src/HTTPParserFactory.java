public class HTTPParserFactory implements IParserFactory {

	private static HTTPParserFactory _instance;

	public static synchronized HTTPParserFactory getInstance() {
		if (_instance == null) {
			_instance = new HTTPParserFactory();
		}
		return _instance;
	}

	private HTTPParserFactory() {
	}

	@Override
	public IParser createParser() {

		return new HTTPParser();
	}

}
