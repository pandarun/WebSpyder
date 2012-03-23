import java.util.concurrent.*;
import java.util.*;
import java.lang.Thread;;

public class GrabManager implements Runnable{	 
	
	// url frontier to visit	
	private BlockingQueue<String> frontier;
	
	// set of visited urls
	private AbstractSet<String> visited;
	
	// amount of threads to create
	private final int numberOfWorkers = 20;
	
	// timeout between requests
	private final int timeout = 1000;
	
	// thread pool to create SpyderTask instances
	private ExecutorService threadPool;

	public GrabManager(Collection<String> urltToVisit)
	{
		this.frontier = new LinkedBlockingDeque<String>(urltToVisit);
		this.visited = new HashSet<String>();
		threadPool = Executors.newFixedThreadPool(numberOfWorkers);
	}	

	@Override
	public void run() {	

		new Thread(
				new Runnable() {					
					@Override
					public void run() {
						// repeat until exists nonvisited url  
						while (!threadPool.isShutdown()) {		

							// dequeue link from frontier
							
							try {
								
							String nonVisitedUrl = frontier.take();								

							System.out.println(nonVisitedUrl);			

							// mark link as visited
							visited.add(nonVisitedUrl);			

							// create another thread with new url to crawl
							Thread.sleep(timeout);
							threadPool.execute(new SpyderTask(nonVisitedUrl));
							
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								shutdownAndAwaitTermination(threadPool);
							}
						}							
					}
				}).start();	
	}
	
	public void stop()
	{
		shutdownAndAwaitTermination(threadPool);
		System.out.println("Service has been stopped");
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
			grabber.addLinksToFrontier(frontier,visited);
			
			// count words from html string					
			pageWordCount = parser.parse(htmlResults);
		}
	}
	
	
}
