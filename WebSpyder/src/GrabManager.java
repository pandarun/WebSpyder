import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.*;

public class GrabManager implements Runnable{	 
	
	// url frontier to visit
	private ConcurrentMap<String, Boolean> frontier;	
	
	// amount of threads to create
	private final int numberOfWorkers = 20;
	
	// timeout between requests
	private final int timeout = 3000;
	
	// thread pool to create SpyderTask instances
	private ExecutorService threadPool;

	public GrabManager(Map<String,Boolean> urltToVisit)
	{
		this.frontier = new ConcurrentHashMap<String,Boolean>(urltToVisit);
		threadPool = Executors.newFixedThreadPool(numberOfWorkers);
	}	

	@Override
	public void run() {	
		
		// repeat until exists nonmarked url  
		while (frontier.containsValue(false)) {
			
			// get first non marked url
			String notMarkedUrl = getFirstKeyByValue(frontier, false);
			
			System.out.println(notMarkedUrl);
			
			// mark link as visited
			frontier.replace(notMarkedUrl, false, true);
			
			// create another thread with new url to crawl
			threadPool.execute(new SpyderTask(notMarkedUrl));
			try {
				
				// wait between requests
				Thread.sleep(timeout);				
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());				
				e.printStackTrace();
			}
		}	
		
		// end of crawling
		shutdownAndAwaitTermination(threadPool);		
	}
	
	// return first matched key by value
	public static <T, E> T getFirstKeyByValue(Map<T, E> map, E value) {
	     for (Entry<T, E> entry : map.entrySet()) {
	         if (value.equals(entry.getValue())) {
	             return entry.getKey();
	         }
	     }
	     return null;
	}
	
	// Thread pool shutdown routine
	public void shutdownAndAwaitTermination(ExecutorService pool)
	{
		// disable new task from being submitted
		pool.shutdown();
		try {
			// wait a while for existing task to terminate
			if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
				// cancel currently executing tasks
				pool.shutdownNow();
				if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
					System.err.println("Pool did not terminate");
				}
			}			
		} catch (InterruptedException e) {			
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
	
	class SpyderTask implements Runnable
	{
		private IGrabber grabber;
		private IParser  parser;
		private String	 url;
		private AbstractMap<String, Integer> pageWordCount;		
		
		public SpyderTask(String urlToVisit) {
			grabber = HTTPGrabberFactory.getInstance().createGrabber();
			parser = HTTPGrabberFactory.getInstance().createParser();
			
			this.url = urlToVisit;
		}

		@Override
		public void run() {
			// get html string from grabber
			String htmlResults = grabber.grab(url);

			// find all links on page
			grabber.addLinksToFrontier(frontier);
			
			// count words from html string					
			pageWordCount = parser.parse(htmlResults);
		}
	}
	
	
}
