import java.io.Console;
import java.util.Collection;
import java.util.LinkedList;

public class WebSpyder {
	
	private static  Collection<String> urlToVisit = new LinkedList<String>();	
	
	public static void main(String[] args) {

		Execute(args);
	}

	private static void Execute(String[] args) {
		
		// get command line args...		
		String url = args[0];

		// add url to visit list and mark it as nonvisited.
		urlToVisit.add(url);
		
		GrabManager manager = new GrabManager(urlToVisit);
		manager.run();		
		processSearch();
		manager.stop();		
	}

	private static void processSearch() {
		String input = null;		
		Collection<String > results = new LinkedList<String>();
		
		Console console = System.console();
		if (console == null) {
			System.err.println("error! no console provided");
			System.exit(-1);
		}
		
		while (true) {
			System.out.println("enter phrase : ");
			
			input = console.readLine();
			if(input == ":exit") return;
			
			try {
				results = IndexDB.getInstance().search(input);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (String result : results) {
				System.out.println(result);
			}
		}
		
		
	}
}
