package wanghan.jsp.bean;

import java.util.List;


public class TableDetail {
	
	        public static List<TableDetail> tb_details;
	        private String tablename;//所属表
	        private String title;//列对应的标题
	        private String columnname;//列名
	        private String columntype;//列类型	     	        
	        private String isnull;//是否为空	      
	        private String key_type;//主键	       
	        private String columndefault;//默认值
	        private String isAutoInctement;//是否自动递增
	        public void setTablename(String tablename){
	        	this.tablename = tablename;
	        }
	        public String getTablename(){
	        	return tablename;
	        }
	        public void setTitle(String title){
	        	this.title = title;
	        }
	        public String getTitle(){
	        	return title;
	        }
	        public void setColumnname(String columnname){
	        	this.columnname = columnname;
	        }
	        public String getColumnname(){
	        	return columnname;
	        }
	        public void setColumntype(String columntype){
	        	this.columntype = columntype;
	        }
	        public String getColumntype(){
	        	return columntype;
	        }
	        public void setIsnull(String isnull){
	        	this.isnull = isnull;
	        }
	        public String getIsnull(){
	        	return isnull;
	        }
	        public void setKey_type(String key_type){
	        	this.key_type = key_type;
	        }
	        public String getKey_type(){
	        	return key_type;
	        }
	        public void setColumndefault(String columndefault){
	        	this.columndefault = columndefault;
	        }
	        public String getColumndefault(){
	        	return columndefault;
	        } 
	        public void setIsAutoInctement(String isAutoInctement){
	        	this.isAutoInctement = isAutoInctement;
	        }
	        public String getIsAutoInctement(){
	        	return isAutoInctement;
	        }
	   	

}
