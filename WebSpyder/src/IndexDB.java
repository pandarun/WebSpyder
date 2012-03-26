// STEP 1 : import requierd packages
import java.beans.PropertyVetoException;
import java.sql.*;
import java.util.AbstractMap;
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
	
	private IndexDB()
	{
		 cpds = new ComboPooledDataSource();
	}
	
	public void InitDB()
	{
		 
		try {
			cpds.setDriverClass(JDBC_DRIVERNAME);
		} catch (PropertyVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
		cpds.setJdbcUrl( DB_URL ); 
		cpds.setUser(USER); 
		cpds.setPassword(PASS); // the settings below are optional -- c3p0 can work with defaults 
		cpds.setMinPoolSize(MIN_POOL_SIZE); 
		cpds.setAcquireIncrement(ACQUIRE_INCREMENT); 
		cpds.setMaxPoolSize(MAX_POOL_SIZE);	
	}

	
	public void save(String url,AbstractMap<String, Integer> pageWordCount) 
	{		
		PreparedStatement pstmt = null;
		
		try {
			
			System.out.println("Creating statement...");
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
	
	public void StopDB()
	{
		cpds.close();
	}
	
}
