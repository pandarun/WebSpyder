public class HTTPGrabberFactory implements IGrabberFactory {

	private static HTTPGrabberFactory _instance = null;

	private HTTPGrabberFactory() {
	}

	public static synchronized HTTPGrabberFactory getInstance() {
		if (_instance == null) {
			_instance = new HTTPGrabberFactory();
		}
		return _instance;
	}

	@Override
	public IGrabber createGrabber() {
		return new HTTPGrabber();
	}

}
