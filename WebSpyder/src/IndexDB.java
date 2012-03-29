// STEP 1 : import requierd packages
import java.beans.PropertyVetoException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class IndexDB {
	
	private static final String DB_CONFIG_FILE="/home/stanislav/git/WebSpyder/WebSpyder/lib/indexdb.xml";
	
	private ComboPooledDataSource cpds = null;
	
	private static final String SQL_STATEMENT = "insert into indexdb.Spyder(token,frequency,url) values(?,?,?)";
	
	private static IndexDB _instance = null;
	
	public static IndexDB getInstance() 
	{
		if(_instance == null) _instance = new IndexDB();
		return _instance;
	}
	
	private boolean IsInitialized = false;
	private Logger log;
	
	private IndexDB()
	{
		 cpds = new ComboPooledDataSource();
		 log = Logger.getLogger("main");
	}
	
	synchronized public void InitDB()
	{
		if(this.IsInitialized) return;
		configureDB();
	}

	
	private void configureDB() {
		Properties properties = new Properties();
		try {
			properties.loadFromXML(new FileInputStream(DB_CONFIG_FILE));
		} catch (InvalidPropertiesFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			
			e.printStackTrace();
			System.exit(-1);
		}
		
		String jdbc_drivername = properties.getProperty("jdbc_drivername");
		String db_url = properties.getProperty("db_url");
		String user = properties.getProperty("user");
		String pass = properties.getProperty("pass");
		int min_pool_size = Integer.parseInt(properties.getProperty("min_pool_size"));
		int acquire_increment = Integer.parseInt(properties.getProperty("acquire_increment"));
		int max_pool_size = Integer.parseInt(properties.getProperty("max_pool_size"));
		
		
		try {
			cpds.setDriverClass(jdbc_drivername);
		} catch (PropertyVetoException e) {
			log.error(e.getMessage());			
		}

		cpds.setJdbcUrl( db_url ); 
		cpds.setUser(user); 
		cpds.setPassword(pass);  
		cpds.setMinPoolSize(min_pool_size); 
		cpds.setAcquireIncrement(acquire_increment); 
		cpds.setMaxPoolSize(max_pool_size);	
		
		this.IsInitialized = true;
		log.info("DB initialized");
		
		
	}

	public void save(String url,AbstractMap<String, Integer> pageWordCount) 
	{		
		PreparedStatement pstmt = null;
		Connection conn = null;
		
		try {			
			
			conn= cpds.getConnection();
			conn.setAutoCommit(false);
			
			pstmt = conn.prepareStatement(SQL_STATEMENT);

			for (String	word : pageWordCount.keySet()) {
				pstmt.setString(1, word);
				pstmt.setInt(2, pageWordCount.get(word));
				pstmt.setString(3, url);
				pstmt.addBatch();
			}			
			
			pstmt.executeBatch();
			conn.commit();
			
		} catch (SQLException e) {
			log.error(e.getMessage());
		}
		finally
		{
			try {				
				pstmt.close();
				
			} catch (SQLException e) {
				log.error(e.getMessage());				
			}
		}
	}
	
	// returns collection of urls sorted by keyword frequency
	public Collection<String> search(String keywordString) throws InterruptedException
	{		
		PreparedStatement pstmt = null;
		Collection<String> searchResults = new LinkedList<String>();
		
		// split keyword string via spaces
		StringTokenizer stk = new StringTokenizer(keywordString, " ", false);
		List<String> keywords = new LinkedList<String>();
		
		while (stk.hasMoreElements()) {
			String token = stk.nextToken();
			keywords.add(token);			
		}

		// return collection of url sorted by keyword frequency
		StringBuilder stb = new StringBuilder("(select url " +
											   "from indexdb.Spyder " +
					 						   "where token = ? "); 
		try {
			
			Connection conn = cpds.getConnection();			
			
			// adding keyword alternative for 'where' part of the query 			
			for (int i = 0; i< keywords.size() -1 ; i++) {
				stb.append(" or ");
				stb.append(" token = ? ");
			}		
			
			stb.append("order by frequency) ");
			
			pstmt = conn.prepareStatement(stb.toString());

			int index = 1;
			for (String keyword : keywords) {
				pstmt.setString(index++, keyword);
			}
			
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				searchResults.add(rs.getString("url"));
			}
			
		} catch (SQLException e) {
			log.error(e.getMessage());
		}
			
		return searchResults; 
	}
	
	public void StopDB()
	{
		if(!IsInitialized) return;
		cpds.close();
		this.IsInitialized = false;
		
	}

	public boolean isInitialized() {
		return this.IsInitialized;
	}
	
}
