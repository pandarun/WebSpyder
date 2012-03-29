import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.Thread;

import org.apache.log4j.Logger;

public class GrabManager implements Runnable {	 
	
	private static GrabManager _instance;
	
	public static GrabManager GetInstance()
	{
		if(_instance == null)
		{
			_instance = new GrabManager();
		}
		return _instance;
	}
	
	private static final String manager_properties = "/home/stanislav/git/WebSpyder/WebSpyder/lib/manager.properties";

	// url frontier to visit	
	private BlockingQueue<String> frontier;
	
	// set of visited urls
	private AbstractSet<String> visited;	
	
	// thread pool to create SpyderTask instances
	private ExecutorService threadPool;	
	
	private Logger log;

	private AtomicInteger generation;
	
	private int timeout;
	
	private boolean isInitialized;
	
	private GrabManager()
	{
		log = Logger.getLogger("main");		
		this.visited = new HashSet<String>();
		this.isInitialized = false;
		configureManager();
		
	}	
	
	public void Init(Collection<String> urltToVisit)
	{
		if(this.isInitialized) return;
		this.frontier = new LinkedBlockingDeque<String>(urltToVisit);
	}

	private void configureManager() {
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(manager_properties));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int number_of_workers = Integer.parseInt(prop.getProperty("number_of_workers"));	
		int max_generation = Integer.parseInt(prop.getProperty("max_generation"));
		this.timeout = Integer.parseInt(prop.getProperty("timeout"));
		
		threadPool = Executors.newFixedThreadPool(number_of_workers);
		this.generation = new AtomicInteger(max_generation);
		
		
	}

	@Override
	
	public void run() {	
		if(!this.isInitialized) return;
		new Thread(
				new Runnable() {					
					@Override
					public void run() {
						
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
					}
				}).start();	
		log.info("manager started");
	}
	
	// stop GrabManager 
	public void stop()
	{	
		if(!this.isInitialized) return;
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
			
			if(IndexDB.getInstance().isInitialized())
			{
				IndexDB.getInstance().save(url, pageWordCount);
			}

		}
	}
	
	
}
