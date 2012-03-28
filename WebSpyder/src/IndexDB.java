// STEP 1 : import requierd packages
import java.beans.PropertyVetoException;
import java.sql.*;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class IndexDB {
	
	// JDBC driver name and DATABASE URL
	private static final String JDBC_DRIVERNAME = "com.mysql.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://chernykh.dyndns.org:3306/indexdb";

	// Database credentials
	
	private static final String USER = "spyder";
	private static final String PASS = "spyder123";
	
	private ComboPooledDataSource cpds = null;
	
	private static final int MIN_POOL_SIZE = 5;
	private static final int ACQUIRE_INCREMENT = 5;
	private static final int MAX_POOL_SIZE = 20;
	
	private static final String SQL_STATEMENT = "insert into indexdb.Spyder(token,frequency,url) values(?,?,?)";
	
	private static IndexDB _instance = null;
	
	public static IndexDB getInstance() 
	{
		if(_instance == null) _instance = new IndexDB();
		return _instance;
	}
	
	private boolean IsInited = false;
	
	private IndexDB()
	{
		 cpds = new ComboPooledDataSource();
	}
	
	synchronized public void InitDB()
	{
		if(this.IsInited) return;
		
		try {
			cpds.setDriverClass(JDBC_DRIVERNAME);
		} catch (PropertyVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  

		cpds.setJdbcUrl( DB_URL ); 
		cpds.setUser(USER); 
		cpds.setPassword(PASS);  
		cpds.setMinPoolSize(MIN_POOL_SIZE); 
		cpds.setAcquireIncrement(ACQUIRE_INCREMENT); 
		cpds.setMaxPoolSize(MAX_POOL_SIZE);	
		
		this.IsInited = true;
	}

	
	public void save(String url,AbstractMap<String, Integer> pageWordCount) 
	{		
		PreparedStatement pstmt = null;
		
		try {			
			
			Connection  conn= cpds.getConnection();
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
			// TODO: handle exception
			e.printStackTrace();
		}
		finally
		{
			try {
				pstmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	// returns collection of urls sorted by keyword frequency
	public Collection<String> search(String keywordString) throws InterruptedException
	{
		while (!IsInited) {			
			Thread.sleep(300);
		}
		
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
			// TODO Auto-generated catch block
			// TODO : Add logging here...
			e.printStackTrace();
		}
			
		return searchResults; 
	}
	
	public void StopDB()
	{
		if(!IsInited) return;
		cpds.close();
		this.IsInited = false;
	}

	public boolean isInited() {
		return this.IsInited;
	}
	
}
