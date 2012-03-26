import java.io.Console;
import java.io.IOException;
import java.io.FileWriter;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.LinkedList;

public class WebSpyder {
	
	private static  Collection<String> urlToVisit = new LinkedList<String>();	
	
	public static void main(String[] args) {

		Execute(args);
	}

	private static void Execute(String[] args) {
		
		// get command line args...
		String outputFile = args[0];
		String url = args[1];

		// add url to visit list and mark it as nonvisited.
		urlToVisit.add(url);
		
		GrabManager manager = new GrabManager(urlToVisit);
		manager.run();		
				
		
		// manager.stop();		
	}
}
