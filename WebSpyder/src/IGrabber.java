import java.util.Collection;

public interface IGrabber {
	public String grab(String url);
	public Collection<String> links(); 	
}
