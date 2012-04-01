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
	
	private static IndexDB _instance;
	public static IndexDB getInstance() 
	{
		if(_instance == null) _instance = new IndexDB();
		return _instance;
	}
	
	private boolean IsInitialized ;
	private ComboPooledDataSource cpds;
	private Logger log;
	private String db_name;
	private String table_name;
	
	private IndexDB() {}
	
	synchronized public void InitDB()
	{
		if(this.IsInitialized) return;
		this.cpds = new ComboPooledDataSource();
		this.log = Logger.getLogger("main");
		configureDB();
	}
	
	private void configureDB() {
		Properties properties = new Properties();
		
		try {
			properties.loadFromXML(new FileInputStream(DB_CONFIG_FILE));
		} catch (InvalidPropertiesFormatException e) {
			log.fatal(e.getMessage());
			System.exit(-1);
		} catch (FileNotFoundException e) {			
			log.fatal(e.getMessage());
			System.exit(-1);
		} catch (IOException e) {
			
			e.printStackTrace();
			System.exit(-1);
		}
		
		String jdbc_drivername = properties.getProperty("jdbc_drivername");
		
		try {
			cpds.setDriverClass(jdbc_drivername);
		} catch (PropertyVetoException e) {
			log.error(e.getMessage());			
		}
		
		
		String db_url = properties.getProperty("db_url");
		String user = properties.getProperty("user");
		String pass = properties.getProperty("pass");
		int min_pool_size = Integer.parseInt(properties.getProperty("min_pool_size"));
		int acquire_increment = Integer.parseInt(properties.getProperty("acquire_increment"));
		int max_pool_size = Integer.parseInt(properties.getProperty("max_pool_size"));
		
		configureConnPool(	db_url, 
							user, 
							pass, 
							min_pool_size, 
							acquire_increment,
							max_pool_size);

		this.db_name = properties.getProperty("db_name");
		this.table_name = properties.getProperty("table_name");
		
		this.IsInitialized = true;
		log.info("DB initialized");
	}

	private void configureConnPool(	String db_url, 
									String user, String pass,
									int min_pool_size, 
									int acquire_increment, 
									int max_pool_size) {
		
		cpds.setJdbcUrl( db_url ); 
		cpds.setUser(user); 
		cpds.setPassword(pass);  
		cpds.setMinPoolSize(min_pool_size); 
		cpds.setAcquireIncrement(acquire_increment); 
		cpds.setMaxPoolSize(max_pool_size);
	}

	public void save(String url,AbstractMap<String, Integer> pageWordCount) 
	{		
		PreparedStatement pstmt = null;
		Connection conn = null;
		
		try {			
			
			conn= cpds.getConnection();
			conn.setAutoCommit(false);			
			pstmt = prepareBatchInsert(url, pageWordCount, conn);			
			pstmt.executeBatch();
			conn.commit();
			log.info(" results obtained :" + url);
			
		} catch (SQLException e) {
			log.error(e.getMessage());
		}
		finally
		{
			try {			
				if(pstmt != null) pstmt.close();
				
			} catch (SQLException e) {
				log.error(e.getMessage());				
			}
		}
	}

	private PreparedStatement prepareBatchInsert(	String url,
													AbstractMap<String, Integer> pageWordCount, 
													Connection conn) throws SQLException {
		
		
		StringBuilder sqlStatement = new StringBuilder("insert into ");
		sqlStatement.append(	this.db_name+	"."	+	this.table_name	+	"(token,	frequency,	url) values(?,?,?)");
		
		PreparedStatement pstmt;
		pstmt = conn.prepareStatement(sqlStatement.toString());

		for (String	word : pageWordCount.keySet()) {
			
			pstmt.setString(1, word);
			pstmt.setInt(2, pageWordCount.get(word));
			pstmt.setString(3, url);
			pstmt.addBatch();
		}
		return pstmt;
	}
	
	public Collection<String> search(String keywordString) 
	{		
		Collection<String> searchResults = new LinkedList<String>();		

		List<String> keywords = prepareKeywords(keywordString);

		try {	

			PreparedStatement pstmt = prepareStatement(keywords);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				searchResults.add(rs.getString("url"));
			}
			
		} catch (SQLException e) {
			log.error(e.getMessage());
		}
			
		return searchResults; 
	}

	private PreparedStatement prepareStatement(List<String> keywords) throws SQLException {
		
		StringBuilder stb = new StringBuilder("(select url " +
											   "from " + this.db_name+"."+this.table_name +
					 						   " where token LIKE ? ");
		
		for (int i = 0; i< keywords.size() -1 ; i++) {
			stb.append(" or ");
			stb.append(" token LIKE ? ");
		}		
		
		stb.append("order by frequency) ");
		
		Connection conn = cpds.getConnection();			
		PreparedStatement pstmt = conn.prepareStatement(stb.toString());

		int index = 1;
		for (String keyword : keywords) {
			pstmt.setString(index++, keyword);
		}
		
		return pstmt;
	}

	private List<String> prepareKeywords(String keywordString) {
		StringTokenizer stk = new StringTokenizer(keywordString, " ", false);
		List<String> keywords = new LinkedList<String>();
		
		while (stk.hasMoreElements()) {
			String token = stk.nextToken();
			keywords.add(token);			
		}
		return keywords;
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
