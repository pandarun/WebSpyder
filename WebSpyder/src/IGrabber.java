import java.util.concurrent.ConcurrentMap;

public interface IGrabber {
	public String grab(String url);

	public void addLinksTo(ConcurrentMap<String, Boolean> frontier);		
}
