import java.io.Console;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class WebSpyder {
	
	static final Logger log =Logger.getLogger("main");
	static final String LOG_PROPERTIES_FILE = "/home/stanislav/git/WebSpyder/WebSpyder/lib/Log4J.properties";
	
	private static  Collection<String> urlToVisit = new LinkedList<String>();	
	
	public static void main(String[] args) throws InterruptedException {

		Execute(args);
	}

	private static void Execute(String[] args) throws InterruptedException {
		
		initializeLogger();
		// get command line args...		
		String url = args[0];

		// add url to visit list and mark it as nonvisited.
		urlToVisit.add(url);
		
		GrabManager manager = new GrabManager(urlToVisit);
		manager.run();		
		processSearch();	
		manager.stop();		
	}

	private static void initializeLogger() {
		Properties logProperties = new Properties();
		
		// load our log4j properties / configuration file 
		try {			
			logProperties.load(new FileInputStream(LOG_PROPERTIES_FILE));			
			PropertyConfigurator.configure(logProperties);
			log.info("Logging initialized");
		} catch (FileNotFoundException e) {
			throw new RuntimeException("unable to load file:"+ LOG_PROPERTIES_FILE);
		} catch (IOException e) {
			throw new RuntimeException("Unable to load logging property "+ LOG_PROPERTIES_FILE); 
		}
		
	}

	private static void processSearch() {
		String input = null;		
		Collection<String > results = new LinkedList<String>();
		
		Console console = System.console();
		if (console == null) {
			System.err.println("error! no console provided");
			log.fatal("no console provided");
			System.exit(-1);
		}
		
		while (true) {
			System.out.println("enter phrase : ");
			
			input = console.readLine();
			if(input.contains("1")) return;
			
			try {
				results = IndexDB.getInstance().search(input);
			} catch (InterruptedException e) {
				log.error(e.getMessage());
				
			}
			for (String result : results) {
				System.out.println(result);
			}
		}
		
		
	}
}
