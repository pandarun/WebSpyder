import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;
import java.lang.Thread;

import org.apache.log4j.Logger;

public class GrabManager implements Runnable{	 
	
	// url frontier to visit	
	private BlockingQueue<String> frontier;
	
	// set of visited urls
	private AbstractSet<String> visited;
	
	// amount of threads to create
	private final int numberOfWorkers = Runtime.getRuntime().availableProcessors()*2;
	
	// timeout between requests
	private final int timeout = 500;
	
	// thread pool to create SpyderTask instances
	private ExecutorService threadPool;
	
	// url generation
	private AtomicInteger generation = new AtomicInteger(0);
	
	private Logger log;

	public GrabManager(Collection<String> urltToVisit)
	{
		log = Logger.getLogger("main");
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
						
						IndexDB.getInstance().InitDB();
						
						// repeat until exists not visited url  
						while (!threadPool.isShutdown()) {		

							try {

								// block until url arrives
								String nonVisitedUrl = frontier.take();	
								if(frontier.isEmpty()) log.info("frontier is empty");

								log.info("visiting "+nonVisitedUrl);
								
								visited.add(nonVisitedUrl);								

								Thread.sleep(timeout);

								// create another thread with new url to crawl
								threadPool.execute(new SpyderTask(nonVisitedUrl));							

							} catch (InterruptedException e) {								
								log.error(e.getMessage());
								shutdownAndAwaitTermination(threadPool);
							}
						}							
						
						IndexDB.getInstance().StopDB();
					}
				}).start();	
		log.info("manager started");
	}
	
	// stop GrabManager 
	public void stop()
	{
		shutdownAndAwaitTermination(threadPool);		
	}
		
	// Thread pool shutdown routine
	public void shutdownAndAwaitTermination(ExecutorService pool)
	{
		// disable new task from being submitted
		pool.shutdown();
		try {
			// wait a while for existing task to terminate
			if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
				// cancel currently executing tasks
				pool.shutdownNow();
				if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
					System.err.println("Pool did not terminate");
				}
			}			
		} catch (InterruptedException e) {			
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
	
	// utility class to carry out grabber and parser tasks
	class SpyderTask implements Runnable
	{
		private IGrabber grabber;
		private IParser  parser;
		private String	 url;
		private AbstractMap<String, Integer> pageWordCount;
		private Logger log;
		
		public SpyderTask(String urlToVisit) {
			
			grabber = HTTPGrabberFactory.getInstance().createGrabber();
			parser = HTTPGrabberFactory.getInstance().createParser();
			
			this.url = urlToVisit;
			
			log = Logger.getLogger("main");
		}

		@Override
		public void run() {
			// get html string from grabber
			log.info("job started:" + url);
			String htmlResults = grabber.grab(url);

			// if generation not null : find all links on page and add to frontier
			if (generation.get() !=0) {
				generation.decrementAndGet();
				grabber.addLinksToFrontier(frontier,visited);				
			}			
			
			// count words from html string					
			pageWordCount = parser.parse(htmlResults);
			
			IndexDB.getInstance().save(url, pageWordCount);

		}
	}
	
	
}
