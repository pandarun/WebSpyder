import java.util.AbstractCollection;
import java.util.concurrent.BlockingQueue;

public interface IGrabber {
	
	// get html string from page
	public String grab(String url);
	
	// add bulk of links to frontier
	public void addLinksToFrontier(BlockingQueue<String> frontier,AbstractCollection<String> visited);		
}
