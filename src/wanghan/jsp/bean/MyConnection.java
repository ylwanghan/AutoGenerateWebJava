package wanghan.jsp.bean;

public class MyConnection {

	private String TABLE_CAT;
	
	private String hostname;
   
    private String user;
    
    private String password;
    
    private String database;
    
    private String tablename;
   
    private String port;
    
    private boolean passtest;
   
    private State constate;
    
    public void setTABLE_CAT(String TABLE_CAT){
    	this.TABLE_CAT = TABLE_CAT;
    }
    public String getTABLE_CAT(){
    	return TABLE_CAT;
    }
    
    public void setHostname(String hostname){
    	this.hostname=hostname;
    }
    public String getHostname(){
    	return hostname;
    }
    public void setUser(String user){
    	this.user=user;
    }
    public String getUser(){
    	return user;
    }
    public void setPassword(String password){
    	this.password=password;
    }
    public String getPassword(){
    	return password;
    }
    public void setDatabase(String database){
    	this.database=database;
    }
    public String getDatabase(){
    	return database;
    }
    public void setTablename(String tablename){
    	this.tablename = tablename;
    }
    public String getTablename(){
    	return tablename;
    }
    public void setPort(String port){
    	this.port=port;
    }
    public String getPort(){
    	return port;
    }
    public void setPassTest(boolean passtest){
    	this.passtest=passtest;
    }
    public boolean getPassTest(){
    	return passtest;
    }
    public void setConstate(State constate){
    	this.constate=constate;
    }
    public State getConstate(){
    	return constate;
    }
    

public enum State
{
    ready,
    open,
    close,
    broken,
    error
}
}
