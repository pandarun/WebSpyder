import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class WebSpyder {
	
	static final Logger log =Logger.getLogger("main");
	static String LOG_PROPERTIES_FILE;
	static String URL_LIST;
	
	private static  Collection<String> urlToVisit = new LinkedList<String>();
	
	public static void main(String[] args) throws InterruptedException  {
		Application.Execute(args);
	}
	
	private  static class Application 
	{
		public static void Execute(String[] args) throws InterruptedException  {
			initializeApp(args);		
			
		}

		private static void initializeApp(String[] args) {

			LOG_PROPERTIES_FILE = (args!= null && args.length>0) ? args[0] : "/home/stanislav/git/WebSpyder/WebSpyder/lib/Log4J.properties";
			URL_LIST = (args!=null &&  args.length>1 ) ? args[1] : "/home/stanislav/git/WebSpyder/WebSpyder/lib/url.lst";
			
			initializeLogger();

			try {
				
				FileReader fr = new FileReader(URL_LIST);
				BufferedReader urlReader = new BufferedReader(fr);
				
				String url;
				while((url = urlReader.readLine()) != null)
				{
					urlToVisit.add(url);
				}
				urlReader.close();
				
			} catch (FileNotFoundException e) {
				log.fatal(e.getMessage());			
				System.exit(-1);
			} catch (IOException e) {
				log.fatal(e.getMessage());
				System.exit(-1);
			}
			
			initManager();
			
		}

		private static void initManager() {
			IndexDB.getInstance().InitDB();		
			GrabManager.GetInstance().Init(urlToVisit);
			GrabManager.GetInstance().run();
		}		

		private static void initializeLogger() {
			Properties logProperties = new Properties();

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

		
	}

	}
