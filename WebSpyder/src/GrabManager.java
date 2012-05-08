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
	private BlockingQueue<String> frontier;
	private AbstractSet<String> visited;	
	private ExecutorService threadPool;	
	private Logger log;
	private AtomicInteger generation;	
	private boolean isInitialized;
	
	private GrabManager(){}	
	
	public void Init(Collection<String> urltToVisit)
	{
		if(this.isInitialized) return;
		this.log = Logger.getLogger("main");		
		this.visited = new HashSet<String>();
		this.isInitialized = false;		
		this.frontier = new LinkedBlockingDeque<String>(urltToVisit);
		configureManager();		
	}

	private void configureManager() {
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(manager_properties));
		} catch (FileNotFoundException e) {
			log.fatal(e.getMessage());
			System.exit(-1);
			
		} catch (IOException e) {
			log.fatal(e.getMessage());
			System.exit(-1);
		}
		
		int number_of_workers = Integer.parseInt(prop.getProperty("number_of_workers"));	
		int max_generation = Integer.parseInt(prop.getProperty("max_generation"));		
		
		threadPool = Executors.newFixedThreadPool(number_of_workers);
		this.generation = new AtomicInteger(max_generation);
		log.info("manager is initialized");
		
	}

	@Override	
	public void run() {	
		if(this.isInitialized) return;
		this.isInitialized = true;
		
		while (!threadPool.isShutdown() && !Thread.currentThread().isInterrupted()) {		
			try {								
				String nonVisitedUrl = frontier.poll(3000,TimeUnit.MILLISECONDS);	

				if(nonVisitedUrl!= null) 
				{   
					visited.add(nonVisitedUrl);								
					threadPool.execute(new SpyderTask(nonVisitedUrl));
				}
				else
				{
					shutdownAndAwaitTermination(threadPool);
				}

			} catch (InterruptedException e) {								
				log.error(e.getMessage());
				shutdownAndAwaitTermination(threadPool);
			}
		}
		shutdownAndAwaitTermination(threadPool);				
		log.info("manager stopped");
	}
	
	public void stop()
	{	
		if(!this.isInitialized) return;
		this.isInitialized = false;
		shutdownAndAwaitTermination(threadPool);
	}

	public void shutdownAndAwaitTermination(ExecutorService pool)
	{
		pool.shutdown();
		try {
			if (!pool.awaitTermination(20, TimeUnit.SECONDS)) {
				pool.shutdownNow();
				if (!pool.awaitTermination(20, TimeUnit.SECONDS)) {
					log.error("Pool didn't terminate");
				}
			}			
		} catch (InterruptedException e) {			
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
			
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);			
			log.info("job started :" + url);
			String htmlResults = grabber.grab(url);

			if (generation.get() !=0) {
				generation.decrementAndGet();
				grabber.addLinksToFrontier(frontier,visited);				
			}			
			
			try {
				pageWordCount = parser.parse(htmlResults);
			} catch (NullPointerException e) {
				log.error(e.getMessage());
				Thread.currentThread().interrupt();
			}
			
			if(url!=null && IndexDB.getInstance().isInitialized() && pageWordCount !=null)
			{
				IndexDB.getInstance().save(url, pageWordCount);
			}

		}
	}
	
	
}
