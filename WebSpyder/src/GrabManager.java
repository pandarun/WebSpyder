import java.util.concurrent.*;
import java.util.*;

public class GrabManager implements Runnable{	
		
	// url frontier to visit
	private ConcurrentLinkedQueue<String> frontier;
	
	// amount of worker threads
	private final int numberOfWorkers = 20;
	
	private ExecutorService threadPool;

	public GrabManager(Collection<String> urltToVisit)
	{
		this.frontier = new ConcurrentLinkedQueue<String>(urltToVisit);
		threadPool = Executors.newFixedThreadPool(numberOfWorkers);
	}	

	@Override
	public void run() {
		for (Iterator<String> iterator = frontier.iterator(); iterator.hasNext();) {
			String urlToVist = (String) iterator.next();
			threadPool.execute(new SpyderTask(urlToVist));			
		}
	}
	
	class SpyderTask implements Runnable
	{
		private IGrabber grabber;
		private IParser  parser;
		private String	 url;
		private TreeMap<String, Integer> pageWordCount;
		
		public SpyderTask(String urlToVisit) {
			grabber = HTTPGrabberFactory.getInstance().createGrabber();
			parser = HTTPParserFactory.getInstance().createParser();			
		}

		@Override
		public void run() {
			String htmlResults = grabber.grab(url);
			frontier.addAll(grabber.links());			
			pageWordCount = (TreeMap<String, Integer>) parser.parse(htmlResults);			
		}
	}
	
	
}
