package wanghan.jsp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;



public class DBUtil {
	//加载驱动
 public static String db_driver="com.mysql.jdbc.Driver";
 public static String db_url="";
 public static String db_user="";
 public static String db_password="";
 public static String db_database="";
 
 public static Connection getConnection(){
	 Connection conn=null;
	 try{
	 Class.forName(db_driver);
	 conn=DriverManager.getConnection(db_url,db_user,db_password);
	 }
	 catch (SQLException e1) {
			e1.printStackTrace(); 
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}finally{
			
		}
	 return conn;
 }
 public static Connection getConnection(String _url,String _user,String _password){
	Connection conn=null;
	 try{
	 Class.forName(db_driver);
     conn=DriverManager.getConnection(_url,_user,_password);
	 }
	 catch (SQLException e1) {
			e1.printStackTrace(); 
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}finally{
			
		}
	 return conn;
 }

		 //关闭相关数据库连接
		public  static void close(Connection conn,Statement stmt) throws SQLException{
			
		if(stmt!=null){
			stmt.close();
		}
		if(conn!=null){
				
			conn.close();
			}
				
}

		public static void close(Connection conn,Statement stmt,ResultSet rs) throws SQLException{
			if(rs!=null){
				rs.close();
				if(stmt!=null){
					stmt.close();
					
				}
				if(conn!=null){
					
					conn.close();
				}		
			}
     }

		public static void close(Statement state, Connection conn) throws SQLException {
			
			if(state!=null){
				state.close();
			}
				if(conn!=null){
					
				conn.close();
				}
							
		}

		public static void close(ResultSet rs, Statement state, Connection conn) throws SQLException {
			if(rs!=null){
				rs.close();
				if(state!=null){
					state.close();
					
				}
				if(conn!=null){
					conn.close();
				}		
			}
			
		}
}


