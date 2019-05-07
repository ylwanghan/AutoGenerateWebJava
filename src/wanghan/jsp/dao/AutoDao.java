package wanghan.jsp.dao;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import wanghan.jsp.bean.CatPackName;
import wanghan.jsp.bean.MyConnection;
import wanghan.jsp.bean.TableDetail;
import wanghan.jsp.util.DBUtil;

import com.mysql.jdbc.DatabaseMetaData;
import com.mysql.jdbc.ResultSetMetaData;


public class AutoDao {
	Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;
	public boolean checkLogin(String _server, String _port,String _user,String _password) {
		DBUtil.db_url = "jdbc:mysql://"+_server+":"+_port;
		DBUtil.db_user = _user;
		DBUtil.db_password = _password;		
		try{			
			conn = DBUtil.getConnection(DBUtil.db_url,DBUtil.db_user,DBUtil.db_password);
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT `SCHEMA_NAME` FROM `information_schema`.`SCHEMATA`");
			if(rs.next()){
				return true;
			}else{
				return false;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;	
	}
	/**
	 * 查询所有数据库
	 * @return
	 */
	public List<MyConnection> checkLogin() {	
		List<MyConnection> list = new ArrayList<MyConnection>();

		try{			
			conn = DBUtil.getConnection(DBUtil.db_url,DBUtil.db_user,DBUtil.db_password);
			//stmt = conn.createStatement();
			//rs = stmt.executeQuery("SELECT `SCHEMA_NAME` FROM `information_schema`.`SCHEMATA`");
			DatabaseMetaData dmd = (DatabaseMetaData) conn.getMetaData();
			ResultSet rs = dmd.getCatalogs();
			while(rs.next()){
				MyConnection connection = new MyConnection();
				connection.setTABLE_CAT(rs.getString("TABLE_CAT"));
				list.add(connection);
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return list;
	}
	/**
	 * 查询数据库中所有表
	 * @param _database
	 * @return
	 * @throws SQLException
	 */
	public List<MyConnection> queryTable(String _database) throws SQLException{
		DBUtil.db_database = _database;	
		List<MyConnection> list = new ArrayList<MyConnection>();		
		conn = DBUtil.getConnection(DBUtil.db_url,DBUtil.db_user,DBUtil.db_password);
		String sql =String.format("select table_name from information_schema.tables where table_schema= '%s' and table_type='base table'",_database);
		stmt = conn.createStatement();
		rs = stmt.executeQuery(sql);
		while(rs.next()){
			MyConnection connection = new MyConnection();
			connection.setTablename(rs.getString("table_name"));
			list.add(connection);
		}
		return list;
	}

	/** 查询表中所有信息
	 * @throws SQLException 
	 */
	public List<TableDetail> queryDetail(String _tablename) throws SQLException{
		List<TableDetail> list = new ArrayList<TableDetail>();
		DBUtil.db_url = String.format("jdbc:mysql://localhost:3306/%s",DBUtil.db_database);
		conn = DBUtil.getConnection(DBUtil.db_url,DBUtil.db_user,DBUtil.db_password);
		String sql = String.format("select * from %s ",_tablename);
		stmt = conn.createStatement();
		rs = stmt.executeQuery(sql);
		if(rs.next()){
			ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
			for(int i = 1;i<=rsmd.getColumnCount();i++){
				TableDetail tableDetail = new TableDetail();
				String title = String.valueOf(rsmd.getColumnLabel(i));
				String name = String.valueOf(rsmd.getColumnName(i));
				String type = String.valueOf(rsmd.getColumnTypeName(i));
				String isnull = String.valueOf(rsmd.isCurrency(i));
				String isAutoInctement = String.valueOf(rsmd.isAutoIncrement(i));

				tableDetail.setTitle(title);
				tableDetail.setColumnname(name);
				tableDetail.setColumntype(type);
				tableDetail.setIsnull(isnull);
				tableDetail.setIsAutoInctement(isAutoInctement);								

				list.add(tableDetail);
			}			

		}
		return list;
	}

	/**
	 * 生成Bean层代码
	 * @throws SQLException 
	 */
	public String createBean(CatPackName catPackName, String _database, String _tablename){

		StringBuilder fields = new StringBuilder();
		StringBuilder methods = new StringBuilder();
		StringBuilder classInfo = new StringBuilder("import java.util.Date;\r\n");
		//如果目录为空，默认为D:\
		//给原始目录赋值
		catPackName.setCatnameold(catPackName.getCatname());
		if(catPackName.getCatname().trim() == ""){
			catPackName.setCatname("D://");
		}
		//生成Bean包结构的文件目录
		if(!catPackName.getBeanPackName().equals("")){
			String catPack = catPackName.getCatname().toString() + catPackName.getBeanPackName().toString();
			catPack = catPack.replace(".", "/");
			new File(catPack).mkdirs();
			catPackName.setCatname(catPack);			
			//获取列名和列类型
			for (int i = 0;i<TableDetail.tb_details.size();i++) {
				String field = TableDetail.tb_details.get(i).getColumnname();				
				String type = typeTrans(TableDetail.tb_details.get(i).getColumntype().toLowerCase());
				fields.append(getFieldStr(field, type));
				methods.append(getMethodStr(field, type));
			}		
			
			classInfo.append("\t/**\r\n\t*");
			classInfo.append("\t*@author wanghan");
			classInfo.append("\r\n\t*/\r\n\r\n");
			classInfo.append("public class ").append(upperFirstChar(_tablename)).append("Bean").append("{\r\n");
			classInfo.append("\r\n");
			classInfo.append("\tpublic static int pageSize = 8; //每页记录数\r\n");
			classInfo.append(fields);
			classInfo.append("\r\n");
			classInfo.append(methods);
			classInfo.append("\r\n");
			classInfo.append("}");
			//生成Bean层.java文件
			File file = new File(catPackName.getCatname(), upperFirstChar(_tablename)+"Bean" + ".java");
			try {			
				String packageinfo =  "package " + catPackName.getBeanPackName().toString() + ";\r\n\r\n";
				//转换编码格式，避免中文乱码	
				OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
				BufferedWriter writer=new BufferedWriter(write); 					
				//向文件中写入包名
				writer.write(packageinfo);							  
				//这样会出现中文乱码
				//FileWriter fw = new FileWriter(file);				
				//向文件中写入要生成的代码				
				writer.write(classInfo.toString());
				writer.flush();
				writer.close();
			} catch (IOException e) {			
				e.printStackTrace();
			}
			catPackName.setCatname(catPackName.getCatnameold());
		}
		return classInfo.toString();		
	}

	/**
	 * 生成Servlet层代码
	 * @throws SQLException
	 */
	public String createServlet(CatPackName catPackName, String _tablename){
		StringBuilder servletInfo = new StringBuilder();
		//给原始目录赋值
		catPackName.setCatnameold(catPackName.getCatname());
		//如果目录为空，默认为D:\
		if(catPackName.getCatname().trim() == ""){
			catPackName.setCatname("D://");
		}
		//生成Bean包结构的文件目录
		if(!catPackName.getServletPackName().equals("")){
			String catPack = catPackName.getCatname().toString() + catPackName.getServletPackName().toString();
			catPack = catPack.replace(".", "/");
			new File(catPack).mkdirs();
			catPackName.setCatname(catPack);

			servletInfo.append("import java.io.IOException;\r\n");
			servletInfo.append("import java.io.UnsupportedEncodingException;\r\n");
			servletInfo.append("import java.sql.SQLException;\r\n");
			servletInfo.append("import java.text.DateFormat;\r\n");
			servletInfo.append("import java.text.ParseException;\r\n");
			servletInfo.append("import java.text.SimpleDateFormat;\r\n");
			servletInfo.append("import java.util.Date;\r\n");
			servletInfo.append("import java.util.Calendar;\r\n");
			servletInfo.append("import java.util.List;\r\n");
			servletInfo.append("import javax.servlet.ServletException;\r\n");
			servletInfo.append("import javax.servlet.http.HttpServlet;\r\n");
			servletInfo.append("import javax.servlet.http.HttpServletRequest;\r\n");
			servletInfo.append("import javax.servlet.http.HttpServletResponse;\r\n");			
			servletInfo.append("import "+catPackName.getBeanPackName()+"."+upperFirstChar(_tablename)+"Bean;\r\n");
			servletInfo.append("import "+catPackName.getDaoPackName()+"."+upperFirstChar(_tablename)+"Dao;\r\n");
			servletInfo.append("\r\n");

			servletInfo.append("public class "+upperFirstChar(_tablename)+"Servlet extends HttpServlet {\r\n");
			servletInfo.append("\tSimpleDateFormat df = new SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss\");\r\n");
			servletInfo.append("\tCalendar cale = Calendar.getInstance();\r\n");
			servletInfo.append("\tDate tasktime=cale.getTime();\r\n");			
			servletInfo.append("\tprivate static final long serialVersionUID = 1L;\r\n");
			//为servlet层添加service()方法
			servletInfo.append("\tpublic void service(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {\r\n");
			servletInfo.append("\t\t//设置编码格式\r\n");
			servletInfo.append("\t\trequest.setCharacterEncoding(\"utf-8\");\r\n");
			servletInfo.append("\t\t//执行操作类型（登录，注销）\r\n");
			servletInfo.append("\t\tString method=request.getParameter(\"method\");\r\n");
			servletInfo.append("\t\tif(\"list\".equals(method)){\r\n");
			servletInfo.append("\t\t\t//查询操作\r\n");
			servletInfo.append("\t\t\ttry {\r\n");
			servletInfo.append("\t\t\t\tlist(request,response);\r\n");
			servletInfo.append("\t\t\t} catch (SQLException e) {\r\n");
			servletInfo.append("\t\t\t\te.printStackTrace(); \r\n");
			servletInfo.append("\t\t\t}\r\n");	
			servletInfo.append("\t\t}else if(\"addOrUpdate\".equals(method)){\r\n");				
			servletInfo.append("\t\t\t//添加操作\r\n");
			servletInfo.append("\t\t\ttry {\r\n");
			servletInfo.append("\t\t\t\taddOrUpdate(request,response);\r\n");
			servletInfo.append("\t\t\t} catch (SQLException e) {\r\n");
			servletInfo.append("\t\t\t\te.printStackTrace(); \r\n");
			servletInfo.append("\t\t\t} catch (ParseException e) {\r\n");
			servletInfo.append("\t\t\t\te.printStackTrace(); \r\n");
			servletInfo.append("\t\t\t}\r\n");				
			servletInfo.append("\t\t}else if(\"updateUI\".equals(method)){\r\n");				
			servletInfo.append("\t\t\t//修改操作\r\n");
			servletInfo.append("\t\t\ttry {\r\n");
			servletInfo.append("\t\t\t\tupdateUI(request,response);\r\n");
			servletInfo.append("\t\t\t} catch (SQLException e) {\r\n");
			servletInfo.append("\t\t\t\te.printStackTrace(); \r\n");
			servletInfo.append("\t\t\t}\r\n");	
			servletInfo.append("\t\t}else if(\"delete\".equals(method)){\r\n");				
			servletInfo.append("\t\t\t//删除操作\r\n");
			servletInfo.append("\t\t\ttry {\r\n");
			servletInfo.append("\t\t\t\tdelete(request,response);\r\n");
			servletInfo.append("\t\t\t} catch (SQLException e) {\r\n");
			servletInfo.append("\t\t\t\te.printStackTrace(); \r\n");
			servletInfo.append("\t\t\t}\r\n");	
			servletInfo.append("\t\t}else if(\"deleteBatch\".equals(method)){\r\n");				
			servletInfo.append("\t\t\t//批量删除操作\r\n");
			servletInfo.append("\t\t\ttry {\r\n");
			servletInfo.append("\t\t\t\tdeleteBatch(request,response);\r\n");
			servletInfo.append("\t\t\t} catch (SQLException e) {\r\n");
			servletInfo.append("\t\t\t\te.printStackTrace(); \r\n");
			servletInfo.append("\t\t\t}\r\n");	
			servletInfo.append("\t\t}\r\n");
			servletInfo.append("\t}\r\n");
			servletInfo.append("\r\n");
			//为Servlet层添加list()方法		
			servletInfo.append("\tpublic void list(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {\r\n");
			servletInfo.append("\t\tint currPage = 1;//当前页码\r\n");
			servletInfo.append("\t\tint pages = 0;//总页数\r\n");
			servletInfo.append("\t\tif(request.getParameter(\"page\") != null){\r\n");
			servletInfo.append("\t\t\tcurrPage = Integer.parseInt(request.getParameter(\"page\"));\r\n");
			servletInfo.append("\t\t}\r\n");
			servletInfo.append("\t\tif(request.getParameter(\"pageSize\") != null && !\"\".equals(request.getParameter(\"pageSize\"))){\r\n");
			servletInfo.append("\t\t\t"+upperFirstChar(_tablename) + "Bean.pageSize = Integer.parseInt(request.getParameter(\"pageSize\"));\r\n");
			servletInfo.append("\t\t}\r\n");
			servletInfo.append("\t\t"+upperFirstChar(_tablename) + "Dao " + lowerFirstChar(_tablename)+"Dao = new "+upperFirstChar(_tablename) + "Dao();\r\n");
			servletInfo.append("\t\tList<"+upperFirstChar(_tablename) + "Bean> "+lowerFirstChar(_tablename)+"Beans = "+lowerFirstChar(_tablename)+"Dao.findAll(currPage);\r\n");
			servletInfo.append("\t\trequest.setAttribute(\""+lowerFirstChar(_tablename)+"Beans\", "+lowerFirstChar(_tablename)+"Beans);\r\n");
			servletInfo.append("\t\tint count = " + lowerFirstChar(_tablename)+"Dao.findCount();\r\n");
			servletInfo.append("\t\tif(count % "+upperFirstChar(_tablename) + "Bean.pageSize == 0){\r\n");
			servletInfo.append("\t\t\tpages = count / "+upperFirstChar(_tablename) + "Bean.pageSize;\r\n");
			servletInfo.append("\t\t}else{\r\n");
			servletInfo.append("\t\t\tpages = count/"+upperFirstChar(_tablename) + "Bean.pageSize + 1;\r\n");
			servletInfo.append("\t\t}\r\n");
			servletInfo.append("\t\tStringBuilder sb = new StringBuilder();\r\n");
			servletInfo.append("\t\tfor(int i = 1 ; i<=pages ; i++){\r\n");
			servletInfo.append("\t\t\tif(i == currPage){\r\n");
			servletInfo.append("\t\t\t\tsb.append(\"[\"+i+\"] \");\r\n");
			servletInfo.append("\t\t\t}else{\r\n");
			servletInfo.append("\t\t\t\tsb.append(\" <a href = \\\"" + lowerFirstChar(_tablename)+"Servlet?method=list&pageSize=\"+"+upperFirstChar(_tablename) + "Bean.pageSize+\"&page=\"+i+\"\\\">\"+i+\"</a>\");//构建分页条\r\n");
			servletInfo.append("\t\t\t}\r\n");			
			servletInfo.append("\t\t\tsb.append(\" \");\r\n");
			servletInfo.append("\t\t}\r\n");
			servletInfo.append("\t\trequest.setAttribute(\"bar\", sb.toString());\r\n");
			servletInfo.append("\t\trequest.getRequestDispatcher(\""+catPackName.getJspListName()+"\").forward(request,response);\r\n");
			servletInfo.append("\t}\r\n");
			servletInfo.append("\r\n");
			//为Servert层添加updateUI()方法
			servletInfo.append("\tpublic void updateUI(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {\r\n");
			servletInfo.append("\t\trequest.setCharacterEncoding(\"utf-8\");\r\n");
			servletInfo.append("\t\t//接收前台数据\r\n");
			servletInfo.append("\t\tString ss_id = request.getParameter(\"ss_id\");\r\n");
			servletInfo.append("\t\t//查询实体\r\n");
			servletInfo.append("\t\t"+upperFirstChar(_tablename) + "Dao " + lowerFirstChar(_tablename)+"Dao = new "+upperFirstChar(_tablename) + "Dao();\r\n");
			servletInfo.append("\t\t"+upperFirstChar(_tablename) + "Bean "+lowerFirstChar(_tablename)+"Beans = "+lowerFirstChar(_tablename)+"Dao.getById(Integer.parseInt(ss_id));\r\n");
			servletInfo.append("\t\trequest.setAttribute(\""+lowerFirstChar(_tablename)+"Beans\", "+lowerFirstChar(_tablename)+"Beans);\r\n");
			servletInfo.append("\t\trequest.getRequestDispatcher(\""+catPackName.getJspAddName()+"\").forward(request,response);\r\n");
			servletInfo.append("\t}\r\n");
			servletInfo.append("\r\n");
			//为Servlet层添加delete()方法
			servletInfo.append("\tpublic void delete(HttpServletRequest request, HttpServletResponse response) throws NumberFormatException, IOException, SQLException, ServletException {\r\n");
			servletInfo.append("\t\trequest.setCharacterEncoding(\"utf-8\");\r\n");
			servletInfo.append("\t\t//接收前台数据\r\n");
			servletInfo.append("\t\tString ss_id = request.getParameter(\"ss_id\");\r\n");
			servletInfo.append("\t\t//查询实体\r\n");
			servletInfo.append("\t\t"+upperFirstChar(_tablename) + "Dao " + lowerFirstChar(_tablename)+"Dao = new "+upperFirstChar(_tablename) + "Dao();\r\n");
			servletInfo.append("\t\t"+lowerFirstChar(_tablename)+"Dao.delete(Integer.parseInt(ss_id));\r\n");
			servletInfo.append("\t\tresponse.sendRedirect(\""+lowerFirstChar(_tablename)+"Servlet?method=list&pageSize=\"+"+upperFirstChar(_tablename) + "Bean.pageSize);\r\n");
			servletInfo.append("\t}\r\n");
			servletInfo.append("\r\n");
			//为Servlet层添加deleteBatch()方法
			servletInfo.append("\tpublic void deleteBatch(HttpServletRequest request, HttpServletResponse response) throws NumberFormatException, IOException, SQLException, ServletException {\r\n");
			servletInfo.append("\t\trequest.setCharacterEncoding(\"utf-8\");\r\n");
			servletInfo.append("\t\t//接收前台数据\r\n");
			servletInfo.append("\t\tString[] arrId = request.getParameterValues(\"arrId\");\r\n");
			servletInfo.append("\t\t//查询实体\r\n");
			servletInfo.append("\t\t"+upperFirstChar(_tablename) + "Dao " + lowerFirstChar(_tablename)+"Dao = new "+upperFirstChar(_tablename) + "Dao();\r\n");
			servletInfo.append("\t\tint row = "+lowerFirstChar(_tablename)+"Dao.deleteBatch(arrId);\r\n");
			servletInfo.append("\t\tresponse.sendRedirect(\""+lowerFirstChar(_tablename)+"Servlet?method=list&pageSize=\"+"+upperFirstChar(_tablename) + "Bean.pageSize);\r\n");
			servletInfo.append("\t}\r\n");
			servletInfo.append("\r\n");			
			//为Servlet层添加addOrUpdate()方法
			servletInfo.append("\tpublic void addOrUpdate(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException , ParseException{\r\n");
			servletInfo.append("\t\trequest.setCharacterEncoding(\"utf-8\");\r\n");
			servletInfo.append("\t\t//接收前台数据\r\n");
			servletInfo.append("\t\tString ss_id = request.getParameter(\"ss_id\");\r\n");

			for (int i = 1;i<TableDetail.tb_details.size();i++) {
				servletInfo.append("\t\tString "+TableDetail.tb_details.get(i).getColumnname()+" = request.getParameter(\""+TableDetail.tb_details.get(i).getColumnname()+"\");\r\n");
			}
			servletInfo.append("\t\t//设置实体\r\n");
			servletInfo.append("\t\t"+upperFirstChar(_tablename) + "Bean "+lowerFirstChar(_tablename)+"Bean = new "+upperFirstChar(_tablename) + "Bean();\r\n");
			servletInfo.append("\t\tif(!\"\".equals(ss_id)){\r\n");
			if(TableDetail.tb_details.get(0).getColumntype().equals("INT")){
				servletInfo.append("\t\t\t"+lowerFirstChar(_tablename) + "Bean.set"+upperFirstChar(TableDetail.tb_details.get(0).getColumnname())+"(Integer.parseInt(ss_id));\r\n");
			}else{
				servletInfo.append("\t\t\t"+lowerFirstChar(_tablename) + "Bean.set"+upperFirstChar(TableDetail.tb_details.get(0).getColumnname())+"(ss_id);\r\n");
			}
			servletInfo.append("\t\t}\r\n");
			for (int i = 1;i<TableDetail.tb_details.size();i++) {
				if(TableDetail.tb_details.get(i).getColumntype().equals("INT")){
					servletInfo.append("\t\t"+lowerFirstChar(_tablename) + "Bean.set"+upperFirstChar(TableDetail.tb_details.get(i).getColumnname())+"(Integer.parseInt("+TableDetail.tb_details.get(i).getColumnname()+"));\r\n");
				}else if(TableDetail.tb_details.get(i).getColumntype().equals("DATETIME")){
					servletInfo.append("\t\t"+lowerFirstChar(_tablename) + "Bean.set"+upperFirstChar(TableDetail.tb_details.get(i).getColumnname())+"(df.format(tasktime));\r\n");
				}else{
					servletInfo.append("\t\t"+lowerFirstChar(_tablename) + "Bean.set"+upperFirstChar(TableDetail.tb_details.get(i).getColumnname())+"("+TableDetail.tb_details.get(i).getColumnname()+");\r\n");
				}
			}
			servletInfo.append("\t\t"+upperFirstChar(_tablename) + "Dao " + lowerFirstChar(_tablename)+"Dao = new "+upperFirstChar(_tablename) + "Dao();\r\n");

			servletInfo.append("\t\t//判断id是否存在\r\n");
			servletInfo.append("\t\tif(\"\".equals(ss_id) || ss_id == null){\r\n");
			servletInfo.append("\t\t\t//添加操作\r\n");
			servletInfo.append("\t\t\t"+lowerFirstChar(_tablename)+"Dao.add("+lowerFirstChar(_tablename)+"Bean);\r\n");
			servletInfo.append("\t\t} else {\r\n");
			servletInfo.append("\t\t\t//修改操作\r\n");
			servletInfo.append("\t\t\t"+lowerFirstChar(_tablename)+"Dao.update("+lowerFirstChar(_tablename)+"Bean);\r\n");
			servletInfo.append("\t\t}\r\n");
			servletInfo.append("\t\tresponse.sendRedirect(\""+lowerFirstChar(_tablename)+"Servlet?method=list&pageSize=\"+"+upperFirstChar(_tablename) + "Bean.pageSize);\r\n");
			servletInfo.append("\t}\r\n");
			servletInfo.append("}\r\n");		

			//生成Servlet层.java文件
			File file = new File(catPackName.getCatname(), upperFirstChar(_tablename)+"Servlet" + ".java");
			try {			
				String packageinfo =  "package " + catPackName.getServletPackName().toString() + ";\r\n\r\n";
				//转换编码格式，避免中文乱码	
				OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
				BufferedWriter writer=new BufferedWriter(write); 					
				//向文件中写入包名
				writer.write(packageinfo);							  
				//这样会出现中文乱码
				//FileWriter fw = new FileWriter(file);				
				//向文件中写入要生成的代码				
				writer.write(servletInfo.toString());
				writer.flush();
				writer.close();
			} catch (IOException e) {			
				e.printStackTrace();
			}

		}
		catPackName.setCatname(catPackName.getCatnameold());
		return servletInfo.toString();
	}

	/**
	 * 生成Dao层代码
	 * @throws SQLException
	 * @return
	 */
	public String createDao(CatPackName catPackName, String _database, String _tablename){

		StringBuilder daoInfo = new StringBuilder();
		//给原始目录赋值
		catPackName.setCatnameold(catPackName.getCatname());
		//如果目录为空，默认为D:\
		if(catPackName.getCatname().trim() == ""){
			catPackName.setCatname("D://");
		}
		//生成Dao包结构的文件目录
		if(!catPackName.getDaoPackName().equals("")){
			String catPack = catPackName.getCatname().toString() + catPackName.getDaoPackName().toString();
			catPack = catPack.replace(".", "/");
			new File(catPack).mkdirs();
			catPackName.setCatname(catPack); 	

			daoInfo.append("import java.sql.Connection;\r\n");
			daoInfo.append("import java.sql.SQLException;\r\n");
			daoInfo.append("import java.sql.PreparedStatement;\r\n");
			daoInfo.append("import java.sql.Statement;\r\n");
			daoInfo.append("import java.sql.ResultSet;\r\n");
			daoInfo.append("import java.util.ArrayList;\r\n");
			daoInfo.append("import java.util.List;\r\n");
			daoInfo.append("import "+catPackName.getBeanPackName()+"."+upperFirstChar(_tablename)+"Bean;\r\n");
			daoInfo.append("import "+catPackName.getUtilPackName()+".DBUtil;\r\n");
			daoInfo.append("\r\n");
			daoInfo.append("public class "+upperFirstChar(_tablename)+"Dao {\r\n");
			//生成Dao层findCount()方法
			daoInfo.append("\t/**\r\n");			
			daoInfo.append("\t* 查询总记录数\r\n");			
			daoInfo.append("\t\\* @throws SQLException\r\n");
			daoInfo.append("\t*/\r\n");			
			daoInfo.append("\tpublic int findCount() throws SQLException{\r\n");
			daoInfo.append("\t\tint count = 0;\r\n");
			daoInfo.append("\t\tConnection conn = DBUtil.getConnection();\r\n");
			daoInfo.append("\t\tString sql = \"select count(*) from "+_tablename+"\";\r\n");
			daoInfo.append("\t\tStatement stmt = conn.createStatement();\r\n");
			daoInfo.append("\t\tResultSet rs = stmt.executeQuery(sql);\r\n");
			daoInfo.append("\t\tif(rs.next()){\r\n");
			daoInfo.append("\t\t\tcount = rs.getInt(1);\r\n");
			daoInfo.append("\t\t}\r\n");
			daoInfo.append("\t\tDBUtil.close(conn, stmt, rs);\r\n");
			daoInfo.append("\t\treturn count;\r\n");
			daoInfo.append("\t}\r\n");
			//生成Dao层deleteBatch()方法
			daoInfo.append("\t/**\r\n");			
			daoInfo.append("\t* 批量删除\r\n");
			daoInfo.append("\t* @param page\r\n");
			daoInfo.append("\t* @return\r\n");
			daoInfo.append("\t\\* @throws SQLException\r\n");
			daoInfo.append("\t*/\r\n");
			daoInfo.append("\tpublic int deleteBatch(String[] arrId) throws SQLException{\r\n");
			daoInfo.append("\t\tint row = 0;\r\n");
			daoInfo.append("\t\tConnection conn = DBUtil.getConnection();\r\n");
			daoInfo.append("\t\tString sql = \"delete from "+_tablename+" where "+TableDetail.tb_details.get(0).getColumnname()+" = ?\";\r\n");
			daoInfo.append("\t\tPreparedStatement ps = conn.prepareStatement(sql);\r\n");
			daoInfo.append("\t\tfor(int i = 0 ; i<arrId.length ; i++){\r\n");
			daoInfo.append("\t\t\tarrId[i] = arrId[i].substring(0, arrId[i].indexOf(\"/\"));\r\n");
			daoInfo.append("\t\t\tps.setInt(1, Integer.parseInt(arrId[i]));\r\n");
			daoInfo.append("\t\t\tps.addBatch();\r\n");
			daoInfo.append("\t\t}\r\n");
			daoInfo.append("\t\tint[] rows = ps.executeBatch();\r\n");
			daoInfo.append("\t\trow = rows.length;\r\n");
			daoInfo.append("\t\tDBUtil.close(conn, ps);\r\n");
			daoInfo.append("\t\treturn row;\r\n");
			daoInfo.append("\t}\r\n");			
     		//生成Dao层findAll()方法
			daoInfo.append("\t/**\r\n");			
			daoInfo.append("\t* 分页查询\r\n");
			daoInfo.append("\t* @param page\r\n");
			daoInfo.append("\t* @return\r\n");
			daoInfo.append("\t\\* @throws SQLException\r\n");
			daoInfo.append("\t*/\r\n");
			daoInfo.append("\tpublic List<"+upperFirstChar(_tablename)+"Bean>" +" findAll(int page) throws SQLException{\r\n");
			daoInfo.append("\t\tConnection conn = DBUtil.getConnection();\r\n");
			daoInfo.append("\t\tString sql = \"select * from "+_tablename+" order by "+TableDetail.tb_details.get(0).getColumnname()+" desc limit ?,?\";\r\n");
			daoInfo.append("\t\tPreparedStatement ps = conn.prepareStatement(sql);\r\n");
			daoInfo.append("\t\tList<"+upperFirstChar(_tablename)+"Bean> "
					+lowerFirstChar(_tablename)+"Beans = new ArrayList<"+upperFirstChar(_tablename)+"Bean>();\r\n");
			daoInfo.append("\t\tps.setInt(1, (page - 1)*"+upperFirstChar(_tablename)+"Bean.pageSize);\r\n");
			daoInfo.append("\t\tps.setInt(2, "+upperFirstChar(_tablename)+"Bean.pageSize);\r\n");
			daoInfo.append("\t\tResultSet rs = ps.executeQuery();\r\n");
			daoInfo.append("\t\twhile(rs.next()){\r\n");
			daoInfo.append("\t\t\t"+upperFirstChar(_tablename)+"Bean "+lowerFirstChar(_tablename)+"Bean = new "+upperFirstChar(_tablename)+"Bean();\r\n");

			//获取列名和列类型
			for (int i = 0;i<TableDetail.tb_details.size();i++) {
				String field = TableDetail.tb_details.get(i).getColumnname();				
				String type = typeTrans(TableDetail.tb_details.get(i).getColumntype().toLowerCase());
				daoInfo.append("\t\t\t"+lowerFirstChar(_tablename)+"Bean.set"+upperFirstChar(field)+"(rs.get"+upperFirstChar(type)+"(\""+field+"\"));\r\n");

			}
			daoInfo.append("\r\n");
			daoInfo.append("\t\t\t"+lowerFirstChar(_tablename)+"Beans.add("+lowerFirstChar(_tablename)+"Bean);\r\n");
			daoInfo.append("\t\t}\r\n");
			daoInfo.append("\t\tDBUtil.close(conn, ps, rs);\r\n");
			daoInfo.append("\treturn "+lowerFirstChar(_tablename)+"Beans;\r\n");
			daoInfo.append("\t}\r\n");			

			//生成Dao层getById()方法
			daoInfo.append("\tpublic "+upperFirstChar(_tablename)+"Bean" +" getById(int id) throws SQLException{\r\n");
			daoInfo.append("\t\tConnection conn = DBUtil.getConnection();\r\n");
			daoInfo.append("\t\tStatement stmt = conn.createStatement();\r\n");
			daoInfo.append("\t\tResultSet rs = null;\r\n");
			daoInfo.append("\t\t"+upperFirstChar(_tablename)+"Bean "
					+lowerFirstChar(_tablename)+"Bean = new "+upperFirstChar(_tablename)+"Bean();\r\n");
			daoInfo.append("\t\trs = stmt.executeQuery(\"select * from "+_tablename+" where "+TableDetail.tb_details.get(0).getColumnname()+" = \"+id);\r\n");
			daoInfo.append("\t\twhile(rs.next()){\r\n");			
			//获取列名和列类型
			for (int i = 0;i<TableDetail.tb_details.size();i++) {
				String field = TableDetail.tb_details.get(i).getColumnname();				
				String type = typeTrans(TableDetail.tb_details.get(i).getColumntype().toLowerCase());
				daoInfo.append("\t\t\t"+lowerFirstChar(_tablename)+"Bean.set"+upperFirstChar(field)+"(rs.get"+upperFirstChar(type)+"(\""+field+"\"));\r\n");

			}
			daoInfo.append("\r\n");
			daoInfo.append("\t\t}\r\n");
			daoInfo.append("\t\tDBUtil.close(conn, stmt, rs);\r\n");
			daoInfo.append("\treturn "+lowerFirstChar(_tablename)+"Bean;\r\n");
			daoInfo.append("\t}\r\n");

			//生成Dao层add()方法
			daoInfo.append("\tpublic void add("+upperFirstChar(_tablename)+"Bean " +lowerFirstChar(_tablename)+"Bean) throws SQLException{\r\n");
			daoInfo.append("\t\tConnection conn = DBUtil.getConnection();\r\n");
			daoInfo.append("\t\tStatement stmt = conn.createStatement();\r\n");
			daoInfo.append("\t\tstmt.executeUpdate(\"insert into "+_tablename+" values(null\"\r\n");			
			//获取表中的每一项列名
			for (int i = 1;i<TableDetail.tb_details.size();i++) {							
				daoInfo.append("\t\t+\",\'\" + "+lowerFirstChar(_tablename)+"Bean.get" +upperFirstChar(TableDetail.tb_details.get(i).getColumnname())+"() + \"\'\"\r\n");
			}
			daoInfo.append("\t\t+\")\");\r\n");
			daoInfo.append("\r\n");
			daoInfo.append("\t\tDBUtil.close(conn, stmt);\r\n");
			daoInfo.append("\t}\r\n");

			//生成Dao层update()方法
			daoInfo.append("\tpublic void update("+upperFirstChar(_tablename)+"Bean " +lowerFirstChar(_tablename)+"Bean) throws SQLException{\r\n");
			daoInfo.append("\t\tConnection conn = DBUtil.getConnection();\r\n");
			daoInfo.append("\t\tStatement stmt = conn.createStatement();\r\n");
			daoInfo.append("\t\tResultSet rs = null;\r\n");
			daoInfo.append("\t\tString sql = \"update "+_tablename+" set \"\r\n");			
			//获取表中每一项列名
			for (int i = 1;i<TableDetail.tb_details.size();i++) {							
				daoInfo.append("\t\t+\""+TableDetail.tb_details.get(i).getColumnname()+" =\'\" + "+lowerFirstChar(_tablename)+"Bean.get" +upperFirstChar(TableDetail.tb_details.get(i).getColumnname())+"() + \"\',\"\r\n");
			}
			//去掉字符串最后一个,
			daoInfo.deleteCharAt(daoInfo.length()-4);

			daoInfo.append("\t\t+\"where "+TableDetail.tb_details.get(0).getColumnname()+" = \"+"+lowerFirstChar(_tablename)+"Bean.get"+upperFirstChar(TableDetail.tb_details.get(0).getColumnname())+"();\r\n");
			daoInfo.append("\t\tstmt.executeUpdate(sql);");
			daoInfo.append("\r\n");
			daoInfo.append("\t\tDBUtil.close(conn, stmt, rs);\r\n");
			daoInfo.append("\t}\r\n");

			//生成Dao层delete()方法
			daoInfo.append("\tpublic void delete(int id) throws SQLException{\r\n");
			daoInfo.append("\t\tConnection conn = DBUtil.getConnection();\r\n");
			daoInfo.append("\t\tStatement stmt = conn.createStatement();\r\n");
			daoInfo.append("\t\tResultSet rs = null;\r\n");
			daoInfo.append("\t\tString sql = \"delete from "+_tablename+" where "+ TableDetail.tb_details.get(0).getColumnname() + "=\"+id;\r\n");			
			daoInfo.append("\t\tstmt.executeUpdate(sql);");
			daoInfo.append("\r\n");
			daoInfo.append("\t\tDBUtil.close(conn, stmt, rs);\r\n");			
			daoInfo.append("\t}\r\n");
			daoInfo.append("}");

			//生成Dao层.java文件
			File file = new File(catPackName.getCatname(), upperFirstChar(_tablename)+"Dao" + ".java");
			try {			
				String packageinfo =  "package " + catPackName.getDaoPackName().toString() + ";\r\n\r\n";
				//转换编码格式，避免中文乱码	
				OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
				BufferedWriter writer=new BufferedWriter(write); 					
				//向文件中写入包名
				writer.write(packageinfo);							  
				//这样会出现中文乱码
				//FileWriter fw = new FileWriter(file);				
				//向文件中写入要生成的代码				
				writer.write(daoInfo.toString());
				writer.flush();
				writer.close();
			} catch (IOException e) {			
				e.printStackTrace();
			}

		}
		catPackName.setCatname(catPackName.getCatnameold());
		return daoInfo.toString();
	}

	/**
	 * 生成Util层代码
	 * @throws SQLException
	 * @return
	 */
	public String createUtil(CatPackName catPackName, String _database, String _tablename){
		StringBuilder utilInfo = new StringBuilder();
		//给原始目录赋值
		catPackName.setCatnameold(catPackName.getCatname());
		//如果目录为空，默认为D:\
		if(catPackName.getCatname().trim() == ""){
			catPackName.setCatname("D://");
		}
		//生成Dao包结构的文件目录
		if(!catPackName.getUtilPackName().equals("")){
			String catPack = catPackName.getCatname().toString() + catPackName.getUtilPackName().toString();
			catPack = catPack.replace(".", "/");
			new File(catPack).mkdirs();
			catPackName.setCatname(catPack);

			utilInfo.append("import java.sql.Connection;\r\n");
			utilInfo.append("import java.sql.DriverManager;\r\n");
			utilInfo.append("import java.sql.ResultSet;\r\n");
			utilInfo.append("import java.sql.SQLException;\r\n");
			utilInfo.append("import java.sql.PreparedStatement;\r\n");
			utilInfo.append("import java.sql.Statement;\r\n");			
			utilInfo.append("\r\n");
			utilInfo.append("public class DBUtil {\r\n");
			utilInfo.append("\t//数据库连接参数设置\r\n");
			utilInfo.append("\tpublic static String db_driver = \"com.mysql.jdbc.Driver\";\r\n");
			utilInfo.append("\tpublic static String db_url = \""+DBUtil.db_url+"\";\r\n");
			utilInfo.append("\tpublic static String db_user = \""+DBUtil.db_user+"\";\r\n");
			utilInfo.append("\tpublic static String db_password = \""+DBUtil.db_password+"\";\r\n");
			utilInfo.append("\r\n");
			//为DBUtil层添加getConnection()方法
			utilInfo.append("\tpublic static Connection getConnection() throws SQLException{\r\n");
			utilInfo.append("\tConnection conn = null;\r\n");
			utilInfo.append("\ttry {\r\n");
			utilInfo.append("\t\tClass.forName(db_driver);\r\n");
			utilInfo.append("\t} catch (ClassNotFoundException e) {\r\n");
			utilInfo.append("\t\te.printStackTrace();\r\n");
			utilInfo.append("\t}\r\n");
			utilInfo.append("\tconn=DriverManager.getConnection(db_url,db_user,db_password);\r\n");
			utilInfo.append("\t\treturn conn;\r\n");
			utilInfo.append("\t}\r\n");
			//为DBUtil添加close()方法
			utilInfo.append("\t//关闭相关数据库连接\r\n");
			utilInfo.append("\tpublic  static void close(Connection conn,Statement stmt) throws SQLException {\r\n");
			utilInfo.append("\t\tif(stmt!=null){\r\n");
			utilInfo.append("\t\t\tstmt.close();\r\n");
			utilInfo.append("\t\t}\r\n");
			utilInfo.append("\t\tif(conn!=null){\r\n");
			utilInfo.append("\t\t\tconn.close();\r\n");
			utilInfo.append("\t\t}\r\n");
			utilInfo.append("\t}\r\n");
			utilInfo.append("\r\n");					

			utilInfo.append("\tpublic static void close(Connection conn,Statement stmt,ResultSet rs) throws SQLException {\r\n");
			utilInfo.append("\t\tif(rs!=null){\r\n");
			utilInfo.append("\t\t\trs.close();\r\n");
			utilInfo.append("\t\t\tif(stmt!=null){\r\n");
			utilInfo.append("\t\t\t\tstmt.close();\r\n");
			utilInfo.append("\t\t\t}\r\n");
			utilInfo.append("\t\t\tif(conn!=null){\r\n");
			utilInfo.append("\t\t\t\tconn.close();\r\n");
			utilInfo.append("\t\t\t}\r\n");
			utilInfo.append("\t\t}\r\n");
			utilInfo.append("\t}\r\n");
			utilInfo.append("\r\n");
			
			utilInfo.append("\tpublic static void close(Connection conn,PreparedStatement stmt,ResultSet rs) throws SQLException {\r\n");
			utilInfo.append("\t\tif(rs!=null){\r\n");
			utilInfo.append("\t\t\trs.close();\r\n");
			utilInfo.append("\t\t\tif(stmt!=null){\r\n");
			utilInfo.append("\t\t\t\tstmt.close();\r\n");
			utilInfo.append("\t\t\t}\r\n");
			utilInfo.append("\t\t\tif(conn!=null){\r\n");
			utilInfo.append("\t\t\t\tconn.close();\r\n");
			utilInfo.append("\t\t\t}\r\n");
			utilInfo.append("\t\t}\r\n");
			utilInfo.append("\t}\r\n");
			utilInfo.append("\r\n");

			utilInfo.append("\t//关闭相关数据库连接\r\n");
			utilInfo.append("\tpublic static void close(Statement state, Connection conn) throws SQLException {\r\n");
			utilInfo.append("\t\tif(state!=null){\r\n");
			utilInfo.append("\t\t\tstate.close();\r\n");
			utilInfo.append("\t\t}\r\n");
			utilInfo.append("\t\tif(conn!=null){\r\n");
			utilInfo.append("\t\t\tconn.close();\r\n");
			utilInfo.append("\t\t}\r\n");
			utilInfo.append("\t}\r\n");
			utilInfo.append("\r\n");	

			utilInfo.append("\tpublic static void close(ResultSet rs, Statement state, Connection conn) throws SQLException {\r\n");
			utilInfo.append("\t\tif(rs!=null){\r\n");
			utilInfo.append("\t\t\trs.close();\r\n");
			utilInfo.append("\t\t\tif(state!=null){\r\n");
			utilInfo.append("\t\t\t\tstate.close();\r\n");
			utilInfo.append("\t\t\t}\r\n");
			utilInfo.append("\t\t\tif(conn!=null){\r\n");
			utilInfo.append("\t\t\t\tconn.close();\r\n");
			utilInfo.append("\t\t\t}\r\n");
			utilInfo.append("\t\t}\r\n");
			utilInfo.append("\t}\r\n");
			utilInfo.append("\r\n");
			utilInfo.append("}");

			//生成Bean层.java文件
			File file = new File(catPackName.getCatname(), "DBUtil.java");
			try {
				String packageinfo =  "package " + catPackName.getUtilPackName().toString() + ";\r\n\r\n";
				//转换编码格式，避免中文乱码	
				OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
				BufferedWriter writer=new BufferedWriter(write); 					
				//向文件中写入包名
				writer.write(packageinfo);							  
				//这样会出现中文乱码
				//FileWriter fw = new FileWriter(file);				
				//向文件中写入要生成的代码				
				writer.write(utilInfo.toString());
				writer.flush();
				writer.close();
			} catch (IOException e) {			
				e.printStackTrace();
			}
			catPackName.setCatname(catPackName.getCatnameold());
			return utilInfo.toString();
		}
		return null;
	}

	/**
	 * 生成XML代码
	 * @throws SQLException
	 * @return
	 */
	public String createXml(CatPackName catPackName, String _tablename){
		StringBuilder utilInfo = new StringBuilder();
		//如果目录为空，默认为D:\
		if(catPackName.getCatname().trim() == ""){
			catPackName.setCatname("D://");
		}
		//生成Dao包结构的文件目录

		String bak = catPackName.getXmlCatName().toString();
		String catPack = catPackName.getXmlCatName().toString() + "WebRoot.WEB-INF";
		catPack = catPack.replace(".", "/");
		new File(catPack).mkdirs();
		catPackName.setXmlCatName(catPack);

		utilInfo.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
		utilInfo.append("<web-app xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://java.sun.com/xml/ns/javaee\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd\" id=\"WebApp_ID\" version=\"3.0\">\r\n");
		utilInfo.append("\t<display-name>"+catPackName.getProjectName()+"</display-name>\r\n");
		utilInfo.append("\t<servlet>\r\n");
		utilInfo.append("\t\t<servlet-name>"+upperFirstChar(_tablename)+"Servlet</servlet-name>\r\n");
		utilInfo.append("\t\t<servlet-class>"+catPackName.getServletPackName()+"."+upperFirstChar(_tablename)+"Servlet</servlet-class>\r\n");
		utilInfo.append("\t</servlet>\r\n");
		utilInfo.append("\t<servlet-mapping>\r\n");
		utilInfo.append("\t\t<servlet-name>"+upperFirstChar(_tablename)+"Servlet</servlet-name>\r\n");
		utilInfo.append("\t\t<url-pattern>/"+lowerFirstChar(_tablename)+"Servlet</url-pattern>\r\n");
		utilInfo.append("\t</servlet-mapping>\r\n");		
		utilInfo.append("\r\n");

		utilInfo.append("\t<welcome-file-list>\r\n");
		utilInfo.append("\t\t<welcome-file>index.html</welcome-file>\r\n");
		utilInfo.append("\t\t<welcome-file>index.htm</welcome-file>\r\n");
		utilInfo.append("\t\t<welcome-file>index.htm</welcome-file>\r\n");
		utilInfo.append("\t\t<welcome-file>index.jsp</welcome-file>\r\n");
		utilInfo.append("\t\t<welcome-file>default.html</welcome-file>\r\n");
		utilInfo.append("\t\t<welcome-file>default.htm</welcome-file>\r\n");
		utilInfo.append("\t\t<welcome-file>default.jsp</welcome-file>\r\n");
		utilInfo.append("\t</welcome-file-list>\r\n");
		utilInfo.append("</web-app>\r\n");
		//生成Bean层.java文件
		File file = new File(catPackName.getXmlCatName(), "web.xml");
		try {
			//转换编码格式，避免中文乱码
			OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
			BufferedWriter writer=new BufferedWriter(write);   
			//这样会出现中文乱码
			//FileWriter fw = new FileWriter(file);				
			//向文件中写入要生成的代码
			writer.write(utilInfo.toString());
			writer.flush();
			writer.close();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		catPackName.setXmlCatName(bak);
		return utilInfo.toString();
	}

	/**
	 * 生成.settings/org.eclipse.wst.common.component代码
	 * @throws SQLException
	 * @return
	 */
	public String createComponent(CatPackName catPackName, String _tablename){
		StringBuilder componentInfo = new StringBuilder();
		
		String[] bc ={"org.eclipse.wst.jsdt.core.javascriptValidator","org.eclipse.jdt.core.javabuilder"
				,"org.eclipse.wst.common.project.facet.core.builder","org.eclipse.wst.validation.validationbuilder"
				,"com.genuitec.eclipse.j2eedt.core.DeploymentDescriptorValidator","com.genuitec.eclipse.ast.deploy.core.DeploymentBuilder"};
		
		componentInfo.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project-modules id=\"moduleCoreId\" project-version=\"1.5.0\">\r\n");
		componentInfo.append("\t<wb-module deploy-name=\""+catPackName.getProjectName()+"\">\r\n");
		componentInfo.append("\t<name>"+catPackName.getProjectName()+"</name>\r\n");
		componentInfo.append("\t\t<wb-resource deploy-path=\"/\" source-path=\"/WebRoot\" tag=\"defaultRootSource\"/>\r\n");
		componentInfo.append("\t\t<wb-resource deploy-path=\"/WEB-INF/classes\" source-path=\"/src\"/>\r\n");
		componentInfo.append("\t\t<property name=\"context-root\" value=\""+catPackName.getProjectName()+"\"/>\r\n");
		componentInfo.append("\t\t<property name=\"java-output-path\" value=\"/"+catPackName.getProjectName()+"/WebRoot/WEB-INF/classes\"/>\r\n");
		componentInfo.append("\t</wb-module>");
		componentInfo.append("</project-modules>\r\n");
			
		//生成.component文件
		File file = new File(catPackName.getXmlCatName()+"/.settings", "org.eclipse.wst.common.component");
		try {
			//转换编码格式，避免中文乱码
			OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
			BufferedWriter writer=new BufferedWriter(write);   
			//这样会出现中文乱码
			//FileWriter fw = new FileWriter(file);				
			//向文件中写入要生成的代码
			writer.write(componentInfo.toString());
			writer.flush();
			writer.close();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		return componentInfo.toString();
	}

	/**
	 * 生成.project代码
	 * @throws SQLException
	 * @return
	 */
	public String createProject(CatPackName catPackName, String _tablename){
		StringBuilder projectInfo = new StringBuilder();
		//如果目录为空，默认为D:\
		if(catPackName.getCatname().trim() == ""){
			catPackName.setCatname("D://");
		}
		//生成Dao包结构的文件目录
		String catPack = catPackName.getXmlCatName().toString();
		catPack = catPack.replace(".", "/");
		new File(catPack).mkdirs();
		catPackName.setXmlCatName(catPack);

		String[] bc ={"org.eclipse.wst.jsdt.core.javascriptValidator","org.eclipse.jdt.core.javabuilder"
				,"org.eclipse.wst.common.project.facet.core.builder","org.eclipse.wst.validation.validationbuilder"
				,"com.genuitec.eclipse.j2eedt.core.DeploymentDescriptorValidator","com.genuitec.eclipse.ast.deploy.core.DeploymentBuilder"};
		
		projectInfo.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
		projectInfo.append("<projectDescription>\r\n");
		projectInfo.append("\t<name>"+catPackName.getProjectName()+"</name>\r\n");
		projectInfo.append("\t<comment></comment>\r\n");
		projectInfo.append("\t<projects></projects>\r\n");
		projectInfo.append("\t<buildSpec>\r\n");
		for(int i = 0 ; i<bc.length ; i++){
			projectInfo.append("\t\t<buildCommand>\r\n");
			projectInfo.append("\t\t\t<name>"+bc[i]+"</name>\r\n");
			projectInfo.append("\t\t\t<arguments>\r\n");
			projectInfo.append("\t\t\t</arguments>\r\n");
			projectInfo.append("\t\t</buildCommand>\r\n");			
		}		
		projectInfo.append("\t</buildSpec>\r\n");
		projectInfo.append("\t<natures>\r\n");
		projectInfo.append("\t\t<nature>org.eclipse.jem.workbench.JavaEMFNature</nature>\r\n");
		projectInfo.append("\t\t<nature>org.eclipse.wst.common.modulecore.ModuleCoreNature</nature>\r\n");
		projectInfo.append("\t\t<nature>org.eclipse.wst.common.project.facet.core.nature</nature>\r\n");
		projectInfo.append("\t\t<nature>org.eclipse.jdt.core.javanature</nature>\r\n");
		projectInfo.append("\t\t<nature>org.eclipse.wst.jsdt.core.jsNature</nature>\r\n");
		projectInfo.append("\t</natures>\r\n");
		projectInfo.append("</projectDescription>\r\n");
		
		//生成.project文件
		File file = new File(catPackName.getXmlCatName(), ".project");
		try {
			//转换编码格式，避免中文乱码
			OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
			BufferedWriter writer=new BufferedWriter(write);   
			//这样会出现中文乱码
			//FileWriter fw = new FileWriter(file);				
			//向文件中写入要生成的代码
			writer.write(projectInfo.toString());
			writer.flush();
			writer.close();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		return projectInfo.toString();
	}
	
	/**
	 * 生成添加页面.jsp代码
	 */
	public String createAddJsp(CatPackName catPackName, String _tablename){
		StringBuilder addJspInfo = new StringBuilder();
		//给原始目录赋值
		catPackName.setCatnameold(catPackName.getCatname());
		//如果目录为空，默认为D:\
		if(catPackName.getJspCatName().trim() == ""){
			catPackName.setJspCatName("D://");
		}
		//生成ListJsp的文件目录
		if(!catPackName.getJspCatName().equals("")){
			String catPack = catPackName.getJspCatName().toString();
			catPack = catPack.replace(".", "/");
			new File(catPack).mkdirs();
			catPackName.setJspCatName(catPack); 	
			addJspInfo.append("<%@page import=\"java.sql.ResultSet\"%>\r\n");
			addJspInfo.append("<%@page import=\"java.sql.Statement\"%>\r\n");
			addJspInfo.append("<%@page import=\"java.sql.Connection\"%>\r\n");
			addJspInfo.append("<%@ page language=\"java\" contentType=\"text/html; charset=UTF-8\"	pageEncoding=\"UTF-8\"%>\r\n");
			addJspInfo.append("<%@ taglib uri=\"http://java.sun.com/jsp/jstl/core\" prefix=\"c\"%>\r\n");
			addJspInfo.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\r\n");
			addJspInfo.append("<html>\r\n");
			addJspInfo.append("<head>\r\n");
			addJspInfo.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\r\n");
			addJspInfo.append("<title>"+catPackName.getJspAddTitle()+"</title>\r\n");
			addJspInfo.append("<script	src=\"${pageContext.request.contextPath}/static/js/jquery.validate.js\" type=\"text/javascript\"></script>\r\n");
			addJspInfo.append("<script	src=\"${pageContext.request.contextPath}/static/js/myValidate.js\" type=\"text/javascript\"></script>\r\n");
			addJspInfo.append("<script src=\"${pageContext.request.contextPath}/static/js/jquery-1.12.1.js\" type=\"text/javascript\"></script>\r\n");
			addJspInfo.append("<script	src=\"${pageContext.request.contextPath}/static/bootstrap-3.3.5-dist/js/bootstrap.js\" type=\"text/javascript\"></script>\r\n");
			addJspInfo.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"${pageContext.request.contextPath}/static/bootstrap-3.3.5-dist/css/bootstrap.css\" />\r\n");
			addJspInfo.append("</head>\r\n");
			addJspInfo.append("<html>\r\n");
			addJspInfo.append("<body>\r\n");		
			addJspInfo.append("\t<div class=\"row-fluid\" style=\"margin-top: 200px;\">\r\n");
			addJspInfo.append("\t\t<div class=\"col-md-3\"></div>\r\n");
			addJspInfo.append("\t\t<div class=\"col-md-6\">\r\n");					
			addJspInfo.append("\t\t\t<form role=\"form\" class=\"form-horizontal\"\r\n");
			addJspInfo.append("\t\t\t\taction=\"${pageContext.request.contextPath}/"+lowerFirstChar(_tablename)+"Servlet?method=addOrUpdate&ss_id=${"+lowerFirstChar(_tablename)+"Beans."+lowerFirstChar(TableDetail.tb_details.get(0).getColumnname())+"}\"\r\n");
			addJspInfo.append("\t\t\t\tmethod=\"post\" id=\"checkForm\">\r\n");

			for (int i = 1;i<TableDetail.tb_details.size();i++) {
				addJspInfo.append("\t\t\t\t<div class=\"form-group\">\r\n");
				addJspInfo.append("\t\t\t\t\t<label class=\"col-md-3 control-label\" for=\""+TableDetail.tb_details.get(i).getColumnname()+"\">"+TableDetail.tb_details.get(i).getColumnname()+"</label>\r\n");
				addJspInfo.append("\t\t\t\t\t<div class=\"col-md-9\">\r\n");
				addJspInfo.append("\t\t\t\t\t\t<input class=\"form-control\" name=\""+lowerFirstChar(TableDetail.tb_details.get(i).getColumnname())+"\" type=\"text\" id=\""+lowerFirstChar(TableDetail.tb_details.get(i).getColumnname())+"\" placeholder=\""+upperFirstChar(TableDetail.tb_details.get(i).getColumnname())+"\" value=\"${"+lowerFirstChar(_tablename)+"Beans."+lowerFirstChar(TableDetail.tb_details.get(i).getColumnname())+"}\" />\r\n");
				addJspInfo.append("\t\t\t\t\t</div>\r\n");
				addJspInfo.append("\t\t\t\t</div>\r\n");
			}
			addJspInfo.append("\t\t\t\t<div class=\"form-group\">\r\n");
			addJspInfo.append("\t\t\t\t\t<div class=\"col-md-offset-3 col-md-9\">\r\n");
			addJspInfo.append("\t\t\t\t\t\t<button type=\"submit\" class=\"btn btn-primary btn-block\">提交</button>\r\n");
			addJspInfo.append("\t\t\t\t\t</div>\r\n");
			addJspInfo.append("\t\t\t\t</div>\r\n");			

			addJspInfo.append("\t\t\t\t<c:if test=\"${param.status.equals('1')}\">\r\n");
			addJspInfo.append("\t\t\t\t\t<div class=\"alert alert-success\" role=\"alert\">添加成功</div>\r\n");
			addJspInfo.append("\t\t\t\t</c:if>\r\n");
			addJspInfo.append("\t\t\t\t<c:if test=\"${param.status.equals('2')}\">\r\n");
			addJspInfo.append("\t\t\t\t\t<div class=\"alert alert-success\" role=\"alert\">该信息已存在</div>\r\n");
			addJspInfo.append("\t\t\t\t</c:if>\r\n");
			addJspInfo.append("\t\t\t\t<c:if test=\"${param.status.equals('3')}\">\r\n");
			addJspInfo.append("\t\t\t\t\t<div class=\"alert alert-info\" role=\"alert\">成功</div>\r\n");
			addJspInfo.append("\t\t\t\t</c:if>\r\n");			
			addJspInfo.append("\t\t\t</form>\r\n");
			addJspInfo.append("\t\t</div>\r\n");
			addJspInfo.append("\t\t<div class=\"col-md-3\"></div>\r\n");			
			addJspInfo.append("\t</div>\r\n");
			addJspInfo.append("</body>\r\n");
			addJspInfo.append("</html>	\r\n");		

			//生成Jsp层.jsp文件
			File file = new File(catPackName.getJspCatName(), catPackName.getJspAddName());
			try {
				//转换编码格式，避免中文乱码
				OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
				BufferedWriter writer=new BufferedWriter(write);   
				//这样会出现中文乱码
				//FileWriter fw = new FileWriter(file);				
				//向文件中写入要生成的代码
				writer.write(addJspInfo.toString());
				writer.flush();
				writer.close();
			} catch (IOException e) {			
				e.printStackTrace();
			}			
		}
		return addJspInfo.toString();
	}

	/**
	 * 生成查询页面.jsp代码
	 */
	public String createListJsp(CatPackName catPackName, String _tablename, String[] columnnames){
		StringBuilder listJspInfo = new StringBuilder();
		//给原始目录赋值
		catPackName.setCatnameold(catPackName.getCatname());
		//如果目录为空，默认为D:\
		if(catPackName.getJspCatName().trim() == ""){
			catPackName.setJspCatName("D://");
		}
		//生成ListJsp的文件目录
		if(!catPackName.getJspCatName().equals("")){
			String catPack = catPackName.getJspCatName().toString();
			catPack = catPack.replace(".", "/");
			new File(catPack).mkdirs();
			catPackName.setJspCatName(catPack); 	
			listJspInfo.append("<%@page import=\"java.sql.ResultSet\"%>\r\n");
			listJspInfo.append("<%@page import=\"java.sql.Statement\"%>\r\n");
			listJspInfo.append("<%@page import=\"java.sql.Connection\"%>\r\n");
			listJspInfo.append("<%@ page language=\"java\" contentType=\"text/html; charset=UTF-8\"	pageEncoding=\"UTF-8\"%>\r\n");
			listJspInfo.append("<%@ taglib uri=\"http://java.sun.com/jsp/jstl/core\" prefix=\"c\"%>\r\n");
			listJspInfo.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\r\n");
			listJspInfo.append("<html>\r\n");
			listJspInfo.append("<head>\r\n");
			listJspInfo.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\r\n");
			listJspInfo.append("<title>"+catPackName.getJspListTitle()+"</title>\r\n");
			listJspInfo.append("<script src=\"${pageContext.request.contextPath}/static/js/jquery-1.12.1.js\" type=\"text/javascript\"></script>\r\n");
			listJspInfo.append("<script	src=\"${pageContext.request.contextPath}/static/bootstrap-3.3.5-dist/js/bootstrap.js\" type=\"text/javascript\"></script>\r\n");
			listJspInfo.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"${pageContext.request.contextPath}/static/bootstrap-3.3.5-dist/css/bootstrap.css\" />\r\n");
			listJspInfo.append("<script type=\"text/javascript\">\r\n");			
			//添加script中的pageSize()方法
			listJspInfo.append("\t//获取每页的记录数\r\n");
			listJspInfo.append("\tfunction pageSize() {\r\n");
			listJspInfo.append("\t\tvar pageSize = document.getElementById(\"pageSize\").value;\r\n");
			listJspInfo.append("\t\tif(pageSize == \"0\"){\r\n");
			listJspInfo.append("\t\t\talert(\"请输入正确数字!\");\r\n");
			listJspInfo.append("\t\t}else{\r\n");
			listJspInfo.append("\t\t\tdocument.location.href='${pageContext.request.contextPath}/"+lowerFirstChar(_tablename)+"Servlet?method=list&pageSize='+pageSize;\r\n");
			listJspInfo.append("\t\t}\r\n");
			listJspInfo.append("\t}\r\n");
			//添加script中的swapCheck()方法
			listJspInfo.append("\t//checkbox 全选/取消全选\r\n");
			listJspInfo.append("\tvar isCheckAll = false;\r\n");
			listJspInfo.append("\tfunction swapCheck() {\r\n");
			listJspInfo.append("\t\tif (isCheckAll) {\r\n");
			listJspInfo.append("\t\t\t$(\"input[id='cb']\").each(function() {\r\n");
			listJspInfo.append("\t\t\t\tthis.checked = false;\r\n");
			listJspInfo.append("\t\t\t});\r\n");
			listJspInfo.append("\t\t\tisCheckAll = false;\r\n");
			listJspInfo.append("\t\t} else {\r\n");
			listJspInfo.append("\t\t\t$(\"input[id='cb']\").each(function() {\r\n");
			listJspInfo.append("\t\t\t\tthis.checked = true;\r\n");
			listJspInfo.append("\t\t\t});\r\n");
			listJspInfo.append("\t\t\tisCheckAll = true;\r\n");
			listJspInfo.append("\t\t}\r\n");
			listJspInfo.append("\t}\r\n");
			//添加script中的deleteBatch()方法
			listJspInfo.append("\t//获取批量删除的id\r\n");
			listJspInfo.append("\tfunction deleteBatch() {\r\n");
			listJspInfo.append("\t\tvar arrs = document.getElementsByName(\"cb_title\");\r\n");
			listJspInfo.append("\t\tvar arrId = \"\";\r\n");
			listJspInfo.append("\t\tfor(k in arrs){\r\n");
			listJspInfo.append("\t\t\tif(arrs[k].checked){\r\n");
			listJspInfo.append("\t\t\t\tarrId += \"arrId=\" + arrs[k].value +\"&\";\r\n");
			listJspInfo.append("\t\t\t}\r\n");
			listJspInfo.append("\t\t}\r\n");
			listJspInfo.append("\t\tdocument.location.href='${pageContext.request.contextPath}/"+lowerFirstChar(_tablename)+"Servlet?method=deleteBatch&'+arrId;\r\n");
			listJspInfo.append("\t}\r\n");			
			listJspInfo.append("</script>\r\n");
			listJspInfo.append("</head>\r\n");
			listJspInfo.append("<body>\r\n");
			listJspInfo.append("\t<div class=\"container-fluid\">\r\n");
			listJspInfo.append("\t\t<div class=\"row-fluid\">\r\n");
			listJspInfo.append("\t\t\t<div class=\"span12\">\r\n");
			listJspInfo.append("\t\t\t\t<h3>信息列表</h3>\r\n");
			listJspInfo.append("\t\t\t\t<div style=\"padding-right: 100px;padding-bottom: 40px;\">\r\n");
			listJspInfo.append("\t\t\t\t\t<div style=\"float:right; margin-left:10px;\">\r\n");
			listJspInfo.append("\t\t\t\t\t\t<button type=\"submit\" style=\"width:50px; height:30px; font-size:12px;\" class=\"btn btn-primary btn-block\" onclick=\"deleteBatch()\">删除</button>\r\n");
			listJspInfo.append("\t\t\t\t\t</div>\r\n");
			listJspInfo.append("\t\t\t\t\t<div style=\"float:right; margin-left:10px;\">\r\n");
			listJspInfo.append("\t\t\t\t\t\t<button type=\"submit\" style=\"width:50px; height:30px; font-size:12px;\" class=\"btn btn-primary btn-block\" onclick=\"pageSize()\">查询</button>\r\n");
			listJspInfo.append("\t\t\t\t\t</div>\r\n");
			listJspInfo.append("\t\t\t\t\t<div style=\"float:right;\">\r\n");
			listJspInfo.append("\t\t\t\t\t\t每页记录数：<input type=\"text\" id=\"pageSize\" size=\"1\"/>\r\n");
			listJspInfo.append("\t\t\t\t\t</div>\r\n");		
			listJspInfo.append("\t\t\t\t</div>\r\n");			    
			listJspInfo.append("\t\t\t</div>\r\n");
			listJspInfo.append("\t\t</div>\r\n");
			listJspInfo.append("\t\t<div class=\"row-fluid\">\r\n");
			listJspInfo.append("\t\t\t<div class=\"col-md-1 \"></div>\r\n");
			listJspInfo.append("\t\t\t<div class=\"col-md-10 \">\r\n");
			listJspInfo.append("\t\t\t\t<table class=\"table table-striped\">\r\n");
			listJspInfo.append("\t\t\t\t\t<tr>\r\n");			
			listJspInfo.append("\t\t\t\t\t\t<td><input type=\"checkbox\" id=\"cb\" onclick=\"swapCheck()\"/>&nbsp;全选</td>\r\n");
			for (int i = 0;i<columnnames.length;i++) {
				listJspInfo.append("\t\t\t\t\t\t<td>"+columnnames[i]+"</td>\r\n");
			}
			listJspInfo.append("\t\t\t\t\t\t<td align=\"center\" colspan=\"2\">操作</td>\r\n");
			listJspInfo.append("\t\t\t\t\t</tr>\r\n");
			listJspInfo.append("\t\t\t\t\t<c:forEach items=\"${"+lowerFirstChar(_tablename)+"Beans }\" var=\"item\" varStatus=\"status\">\r\n");
			listJspInfo.append("\t\t\t\t\t\t<tr>\r\n");
			listJspInfo.append("\t\t\t\t\t\t\t<td><input type=\"checkbox\" name=\"cb_title\" id=\"cb\" value=${item."+lowerFirstChar(TableDetail.tb_details.get(0).getColumnname())+" }/>\r\n");
			for (int i = 0;i<columnnames.length;i++) {
				listJspInfo.append("\t\t\t\t\t\t\t<td>${item."+lowerFirstChar(columnnames[i])+" }</td>\r\n");
			}
			listJspInfo.append("\t\t\t\t\t\t\t<td><a href=\"${pageContext.request.contextPath}/"+lowerFirstChar(_tablename)+"Servlet?method=updateUI&ss_id=${item."+lowerFirstChar(TableDetail.tb_details.get(0).getColumnname())+" }\">修改</a></td>\r\n");
			listJspInfo.append("\t\t\t\t\t\t\t<td><a href=\"${pageContext.request.contextPath}/"+lowerFirstChar(_tablename)+"Servlet?method=delete&ss_id=${item."+lowerFirstChar(TableDetail.tb_details.get(0).getColumnname())+" }\">删除</a></td>\r\n");
			listJspInfo.append("\t\t\t\t\t\t</tr>\r\n");
			listJspInfo.append("\t\t\t\t\t</c:forEach>\r\n");
			listJspInfo.append("\t\t\t\t\t<tr>\r\n");
			listJspInfo.append("\t\t\t\t\t\t<td colspan=\"7\" align=\"center\">\r\n");
			listJspInfo.append("\t\t\t\t\t\t\t<%=request.getAttribute(\"bar\")%>\r\n");
			listJspInfo.append("\t\t\t\t\t\t</td>\r\n");
			listJspInfo.append("\t\t\t\t\t</tr>\r\n");	
			listJspInfo.append("\t\t\t\t</table>\r\n");
			listJspInfo.append("\t\t\t\t<c:if test=\"${param.status.equals('2')}\">\r\n");
			listJspInfo.append("\t\t\t\t\t<div class=\"alert alert-success\" role=\"alert\">修改成功</div>\r\n");
			listJspInfo.append("\t\t\t\t</c:if>\r\n");
			listJspInfo.append("\t\t\t\t<c:if test=\"${param.status.equals('3')}\">\r\n");
			listJspInfo.append("\t\t\t\t\t<div class=\"alert alert-success\" role=\"alert\">删除成功</div>\r\n");
			listJspInfo.append("\t\t\t\t</c:if>\r\n");
			listJspInfo.append("\t\t\t\t<c:if test=\"${param.status.equals('1')}\">\r\n");
			listJspInfo.append("\t\t\t\t\t<div class=\"alert alert-info\" role=\"alert\">没有权限操作超级管理员</div>\r\n");
			listJspInfo.append("\t\t\t\t</c:if>\r\n");
			listJspInfo.append("\t\t\t</div>\r\n");
			listJspInfo.append("\t\t\t<div class=\" col-md-1\"></div>\r\n");
			listJspInfo.append("\t\t</div>\r\n");
			listJspInfo.append("\t\t<div class=\"row-fluid\">\r\n");
			listJspInfo.append("\t\t\t<div class=\"col-md-3\"></div>\r\n");
			listJspInfo.append("\t\t\t<div class=\"col-md-6\">\r\n");
			listJspInfo.append("\t\t\t\t<div>\r\n");
			listJspInfo.append("\t\t\t\t\t<!-- <util:page pagingBean=\"${pagingBean }\" /> -->\r\n");
			listJspInfo.append("\t\t\t\t</div>\r\n");
			listJspInfo.append("\t\t\t</div>\r\n");
			listJspInfo.append("\t\t\t<div class=\"col-md-3\"></div>\r\n");
			listJspInfo.append("\t\t</div>\r\n");
			listJspInfo.append("\t</div>\r\n");
			listJspInfo.append("</body>\r\n");
			listJspInfo.append("</html>	\r\n");		

			//生成Jsp层.jsp文件
			File file = new File(catPackName.getJspCatName(), catPackName.getJspListName());
			try {
				//转换编码格式，避免中文乱码
				OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
				BufferedWriter writer=new BufferedWriter(write);   
				//这样会出现中文乱码
				//FileWriter fw = new FileWriter(file);				
				//向文件中写入要生成的代码
				writer.write(listJspInfo.toString());
				writer.flush();
				writer.close();
			} catch (IOException e) {			
				e.printStackTrace();
			}			
		}
		return listJspInfo.toString();
	}

	/**
	 * 生成head.jsp代码
	 */
	public String createHeadJsp(CatPackName catPackName, String _tablename){
		StringBuilder headJspInfo = new StringBuilder();
		//给原始目录赋值
		catPackName.setCatnameold(catPackName.getCatname());
		//如果目录为空，默认为D:\
		if(catPackName.getJspCatName().trim() == ""){
			catPackName.setJspCatName("D://");
		}
		//生成ListJsp的文件目录
		if(!catPackName.getJspCatName().equals("")){
			String catPack = catPackName.getJspCatName().toString();
			catPack = catPack.replace(".", "/");
			new File(catPack).mkdirs();
			catPackName.setJspCatName(catPack);			
			headJspInfo.append("<%@ page language=\"java\" contentType=\"text/html; charset=UTF-8\"	pageEncoding=\"UTF-8\"%>\r\n");
			headJspInfo.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\r\n");
			headJspInfo.append("<script src=\"${pageContext.request.contextPath}/static/js/jquery-1.12.1.js\" type=\"text/javascript\"></script>\r\n");
			headJspInfo.append("<script	src=\"${pageContext.request.contextPath}/static/bootstrap-3.3.5-dist/js/bootstrap.js\" type=\"text/javascript\"></script>\r\n");
			headJspInfo.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"${pageContext.request.contextPath}/static/bootstrap-3.3.5-dist/css/bootstrap.css\" />\r\n");
			headJspInfo.append("<div class=\"navbar navbar-default\" style=\"padding:20px  0 10px;  margin: 0 ;\">\r\n");
			headJspInfo.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\r\n");
			headJspInfo.append("<div class=\"container-fluid\">\r\n");
			headJspInfo.append("\t<div class=\"navbar-header\">\r\n");
			headJspInfo.append("\t\t<button type=\"button\" class=\"navbar-toggle collapsed\" data-toggle=\"collapse\" data-target=\"#bs-example-navbar-collapse-1\" aria-expanded=\"false\">\r\n");
			headJspInfo.append("\t\t\t<span class=\"sr-only\">Toggle navigation</span> <span class=\"icon-bar\"></span>\r\n");
			headJspInfo.append("\t\t\t<span class=\"icon-bar\"></span> <span class=\"icon-bar\"></span>\r\n");
			headJspInfo.append("\t\t</button>\r\n");
			headJspInfo.append("\t\t<a class=\"navbar-brand\" href=\"#\">信息管理系统&nbsp;&nbsp;&nbsp;&nbsp;</a>\r\n");
			headJspInfo.append("\t</div>\r\n");
			headJspInfo.append("\t<div class=\"collapse navbar-collapse\" id=\"bs-example-navbar-collapse-1\">\r\n");
			headJspInfo.append("\t\t<ul class=\"nav navbar-nav navbar-right\">\r\n");
			headJspInfo.append("\t\t\t<li><a target=\"_parent\" href=\"${pageContext.request.contextPath}/main.jsp\">首页</a></li>\r\n");			
			headJspInfo.append("\t\t\t<li role=\"separator\" class=\"divider\"></li>\r\n");
			headJspInfo.append("\t\t\t<li><a target=\"_parent\" href=\"${pageContext.request.contextPath}/adminServlet?method=end&status=1\">退出登录</a></li>\r\n");
			headJspInfo.append("\t\t</ul>\r\n");
			headJspInfo.append("\t</div>\r\n");
			headJspInfo.append("</div>\r\n");
			headJspInfo.append("</div>\r\n");						
			//生成Jsp层head.jsp文件
			File file = new File(catPackName.getJspCatName(), "head.jsp");
			try {
				//转换编码格式，避免中文乱码
				OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
				BufferedWriter writer=new BufferedWriter(write);   
				//这样会出现中文乱码
				//FileWriter fw = new FileWriter(file);				
				//向文件中写入要生成的代码
				writer.write(headJspInfo.toString());
				writer.flush();
				writer.close();
			} catch (IOException e) {			
				e.printStackTrace();
			}			
		}
		return headJspInfo.toString();
	}

	/**
	 * 生成left.jsp代码
	 */
	public String createLeftJsp(CatPackName catPackName, String _tablename){
		StringBuilder mainJspInfo = new StringBuilder();
		//给原始目录赋值
		catPackName.setCatnameold(catPackName.getCatname());
		//如果目录为空，默认为D:\
		if(catPackName.getJspCatName().trim() == ""){
			catPackName.setJspCatName("D://");
		}
		//生成ListJsp的文件目录
		if(!catPackName.getJspCatName().equals("")){
			String catPack = catPackName.getJspCatName().toString();
			catPack = catPack.replace(".", "/");
			new File(catPack).mkdirs();
			catPackName.setJspCatName(catPack);			
			mainJspInfo.append("<%@ page language=\"java\" contentType=\"text/html; charset=UTF-8\"	pageEncoding=\"UTF-8\"%>\r\n");
			mainJspInfo.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\r\n");
			mainJspInfo.append("<html>\r\n");	
			mainJspInfo.append("<head>\r\n");
			mainJspInfo.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\r\n");
			mainJspInfo.append("<title>Insert title here</title>\r\n");
			mainJspInfo.append("<style type=\"text/css\">\r\n");
			mainJspInfo.append("body {\r\n");
			mainJspInfo.append("\tmargin: 0;\r\n");
			mainJspInfo.append("\tpadding: 0;\r\n");
			mainJspInfo.append("\tfont-size: 12px;\r\n");
			mainJspInfo.append("\tfont-family: \"Microsoft Yahei\", Verdana, Arial, Helvetica, sans-serif\r\n");
			mainJspInfo.append("}\r\n");
			mainJspInfo.append(".leftMenu {\r\n");
			mainJspInfo.append("\tmin-width:220px;\r\n");
			mainJspInfo.append("\twidth:268px;\r\n");
			mainJspInfo.append("\tmargin:40px auto 0 auto;\r\n");
			mainJspInfo.append("}\r\n");
			mainJspInfo.append(".menu {\r\n");
			mainJspInfo.append("\tborder: #bdd7f2 1px solid;\r\n");
			mainJspInfo.append("\tborder-top: #0080c4 4px solid;\r\n");
			mainJspInfo.append("\tborder-bottom: #0080c4 4px solid;\r\n");
			mainJspInfo.append("\tbackground: #f4f9ff repeat-y right;\r\n");
			mainJspInfo.append("\tmargin-left: 10px;\r\n");
			mainJspInfo.append("}\r\n");
			mainJspInfo.append(".menu .ListTitle {\r\n");
			mainJspInfo.append("\tborder-bottom: 1px #98c9ee solid;\r\n");
			mainJspInfo.append("\tdisplay: block;\r\n");
			mainJspInfo.append("\ttext-align: center;\r\n");
			mainJspInfo.append("\t/*position: relative;*/\r\n");
			mainJspInfo.append("\theight: 38px;\r\n");
			mainJspInfo.append("\tline-height: 38px;\r\n");
			mainJspInfo.append("\tcursor: pointer;\r\n");
			mainJspInfo.append("\t/*+min-width:220px;*/\r\n");
			mainJspInfo.append("\t+width:100%;\r\n");			
			mainJspInfo.append("}\r\n");
			mainJspInfo.append(".ListTitlePanel {\r\n");
			mainJspInfo.append("\tposition: relative;\r\n");
			mainJspInfo.append("}\r\n");
			mainJspInfo.append(".leftbgbt {\r\n");
			mainJspInfo.append("\tposition: absolute;\r\n");
			mainJspInfo.append("\tbackground: no-repeat;\r\n");
			mainJspInfo.append("\twidth: 11px;\r\n");
			mainJspInfo.append("\theight: 52px;\r\n");
			mainJspInfo.append("\tleft: -11px;\r\n");
			mainJspInfo.append("\ttop: -4px;\r\n");
			mainJspInfo.append("}\r\n");
			mainJspInfo.append(".leftbgbt2 {\r\n");
			mainJspInfo.append("\tposition: absolute;\r\n");
			mainJspInfo.append("\tbackground: no-repeat;\r\n");
			mainJspInfo.append("\twidth: 11px;\r\n");
			mainJspInfo.append("\theight: 48px;\r\n");
			mainJspInfo.append("\tleft: -11px;\r\n");
			mainJspInfo.append("\ttop: -1px;\r\n");
			mainJspInfo.append("}\r\n");
			mainJspInfo.append(".menuList {\r\n");
			mainJspInfo.append("\tdisplay: block;\r\n");
			mainJspInfo.append("\theight: auto;\r\n");
			mainJspInfo.append("}\r\n");
			mainJspInfo.append(".menuList div {\r\n");
			mainJspInfo.append("\theight: 28px;\r\n");
			mainJspInfo.append("\tline-height: 24px;\r\n");
			mainJspInfo.append("\tborder-bottom: 1px #98c9ee dotted;\r\n");
			mainJspInfo.append("}\r\n");
			mainJspInfo.append(".menuList div a {\r\n");
			mainJspInfo.append("\tdisplay: block;\r\n");
			mainJspInfo.append("\tbackground: #fff;\r\n");
			mainJspInfo.append("\tline-height: 28px;\r\n");
			mainJspInfo.append("\theight: 28px;\r\n");
			mainJspInfo.append("\ttext-align: center;\r\n");
			mainJspInfo.append("\tcolor: #185697;\r\n");
			mainJspInfo.append("\ttext-decoration: none;\r\n");
			mainJspInfo.append("}\r\n");
			mainJspInfo.append(".menuList div a:hover {\r\n");
			mainJspInfo.append("\tcolor: #f30;\r\n");
			mainJspInfo.append("\tbackground: #0080c4;\r\n");
			mainJspInfo.append("\tcolor: #fff;\r\n");
			mainJspInfo.append("}\r\n");
			mainJspInfo.append("</style>\r\n");			
			mainJspInfo.append("<script src=\"${pageContext.request.contextPath}/static/js/jquery-1.12.1.js\" type=\"text/javascript\"></script>\r\n");
			mainJspInfo.append("<script type=\"text/javascript\">\r\n");	
			mainJspInfo.append("$(document).ready(function() {\r\n");	
			mainJspInfo.append("\tvar menuParent = $('.menu > .ListTitlePanel > div');//获取menu下的父层的DIV\r\n");	
			mainJspInfo.append("\tvar menuList = $('.menuList');\r\n");	
			mainJspInfo.append("\t$('.menu > .menuParent > .ListTitlePanel > .ListTitle').each(function(i) {//获取列表的大标题并遍历\r\n");	
			mainJspInfo.append("\t\t$(this).click(function(){\r\n");
			mainJspInfo.append("\t\t\tif($(menuList[i]).css('display') == 'none'){\r\n");
			mainJspInfo.append("\t\t\t\t$(menuList[i]).slideDown(300);\r\n");
			mainJspInfo.append("\t\t\t}\r\n");
			mainJspInfo.append("\t\t\telse{\r\n");
			mainJspInfo.append("\t\t\t\t$(menuList[i]).slideUp(300);\r\n");
			mainJspInfo.append("\t\t\t}\r\n");
			mainJspInfo.append("\t\t});\r\n");
			mainJspInfo.append("\t});\r\n");
			mainJspInfo.append("});\r\n");
			mainJspInfo.append("</script>\r\n");
			mainJspInfo.append("</head>\r\n");
			mainJspInfo.append("<body style=\"margin-top: -30px;\">\r\n");
			mainJspInfo.append("<div class=\"leftMenu\">\r\n");
			mainJspInfo.append("\t<div class=\"menu\">\r\n");
			mainJspInfo.append("\t\t<div class=\"menuParent\">\r\n");
			mainJspInfo.append("\t\t\t<div class=\"ListTitlePanel\">\r\n");
			mainJspInfo.append("\t\t\t\t<div class=\"ListTitle\">\r\n");
			mainJspInfo.append("\t\t\t\t\t<strong>信息管理</strong>\r\n");
			mainJspInfo.append("\t\t\t\t\t<div class=\"leftbgbt\"> </div>\r\n");			
			mainJspInfo.append("\t\t\t\t</div>\r\n");
			mainJspInfo.append("\t\t\t</div>\r\n");
			mainJspInfo.append("\t\t\t<div class=\"menuList\">\r\n");
			mainJspInfo.append("\t\t\t\t<div> <a target=\"mainAction\" href=\"${pageContext.request.contextPath}/"+catPackName.getJspAddName()+"\">"+catPackName.getJspAddTitle()+"</a></div>\r\n");
			mainJspInfo.append("\t\t\t\t<div> <a target=\"mainAction\" href=\"${pageContext.request.contextPath}/"+lowerFirstChar(_tablename)+"Servlet?method=list\">"+catPackName.getJspListTitle()+"</a> </div>\r\n");
			mainJspInfo.append("\t\t\t</div>\r\n");			
			mainJspInfo.append("\t\t</div>\r\n");
			mainJspInfo.append("\t</div>\r\n");
			mainJspInfo.append("<div style=\"text-align:center;\"></div>\r\n");
			mainJspInfo.append("</div>\r\n");
			mainJspInfo.append("</body>\r\n");
			mainJspInfo.append("</html>\r\n");
			//生成Jsp层head.jsp文件
			File file = new File(catPackName.getJspCatName(), "left.jsp");
			try {
				//转换编码格式，避免中文乱码
				OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
				BufferedWriter writer=new BufferedWriter(write);   
				//这样会出现中文乱码
				//FileWriter fw = new FileWriter(file);				
				//向文件中写入要生成的代码
				writer.write(mainJspInfo.toString());
				writer.flush();
				writer.close();
			} catch (IOException e) {			
				e.printStackTrace();
			}			
		}
		return mainJspInfo.toString();
	}

	/**
	 * 生成main.jsp代码
	 */
	public String createMainJsp(CatPackName catPackName, String _tablename){
		StringBuilder mainJspInfo = new StringBuilder();
		//给原始目录赋值
		catPackName.setCatnameold(catPackName.getCatname());
		//如果目录为空，默认为D:\
		if(catPackName.getJspCatName().trim() == ""){
			catPackName.setJspCatName("D://");
		}
		//生成ListJsp的文件目录
		if(!catPackName.getJspCatName().equals("")){
			String catPack = catPackName.getJspCatName().toString();
			catPack = catPack.replace(".", "/");
			new File(catPack).mkdirs();
			catPackName.setJspCatName(catPack);			
			mainJspInfo.append("<%@ page language=\"java\" contentType=\"text/html; charset=UTF-8\"	pageEncoding=\"UTF-8\"%>\r\n");
			mainJspInfo.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\r\n");
			mainJspInfo.append("<html>\r\n");	
			mainJspInfo.append("<head>\r\n");
			mainJspInfo.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\r\n");
			mainJspInfo.append("<title>后台信息管理系统</title>\r\n");
			mainJspInfo.append("<script src=\"${pageContext.request.contextPath}/static/js/jquery-1.12.1.js\" type=\"text/javascript\"></script>\r\n");
			mainJspInfo.append("<script	src=\"${pageContext.request.contextPath}/static/bootstrap-3.3.5-dist/js/bootstrap.js\" type=\"text/javascript\"></script>\r\n");
			mainJspInfo.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"${pageContext.request.contextPath}/static/bootstrap-3.3.5-dist/css/bootstrap.css\" />\r\n");
			mainJspInfo.append("</head>\r\n");			
			mainJspInfo.append("<frameset frameborder=\"no\" rows=\"82px, *\">\r\n");
			mainJspInfo.append("\t<frame src=\"head.jsp\">\r\n");
			mainJspInfo.append("\t<frameset cols=\"285px, *\">\r\n");
			mainJspInfo.append("\t\t<frame src=\"left.jsp\">\r\n");
			mainJspInfo.append("\t\t<frame name=\"mainAction\">\r\n");
			mainJspInfo.append("\t</frameset>\r\n");
			mainJspInfo.append("</frameset>\r\n");
			mainJspInfo.append("</html>\r\n");
			//生成Jsp层head.jsp文件
			File file = new File(catPackName.getJspCatName(), "main.jsp");
			try {
				//转换编码格式，避免中文乱码
				OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
				BufferedWriter writer=new BufferedWriter(write);   
				//这样会出现中文乱码
				//FileWriter fw = new FileWriter(file);				
				//向文件中写入要生成的代码
				writer.write(mainJspInfo.toString());
				writer.flush();
				writer.close();
			} catch (IOException e) {			
				e.printStackTrace();
			}			
		}
		return mainJspInfo.toString();
	}

	/**
	 * 类型转换
	 * @param type
	 * @return
	 */
	//数据库字段类型与JAVA类型转换
	public String typeTrans(String type) {
		if (type.contains("tinyint")) {
			return "boolean";
		} else if (type.contains("int")) {
			return "int";
		} else if (type.contains("datetime")) {
			return "String";
		} else if (type.contains("BIGINT")) {
			return "Long";
		} else if (type.contains("varchar") || type.contains("date")
				|| type.contains("time") || type.contains("timestamp")
				|| type.contains("text") || type.contains("enum")
				|| type.contains("set")) {
			return "String";
		} else if (type.contains("binary") || type.contains("blob")) {
			return "byte[]";
		} else {
			return "String";
		}
	}

	//获取方法字符串 
	private String getMethodStr(String field, String type) {
		StringBuilder get = new StringBuilder("\tpublic ");
		get.append(type).append(" ");
		if (type.equals("boolean")) {
			get.append(field);
		} else {
			get.append("get");
			get.append(upperFirstChar(field));
		}
		get.append("(){").append("\r\n\t\treturn this.").append(field)
		.append(";\r\n\t}\r\n");
		StringBuilder set = new StringBuilder("\tpublic void ");

		if (type.equals("boolean")) {
			set.append(field);
		} else {
			set.append("set");
			set.append(upperFirstChar(field));
		}
		set.append("(").append(type).append(" ").append(field)
		.append("){\r\n\t\tthis.").append(field).append("=")
		.append(field).append(";\r\n\t}\r\n");
		get.append(set);
		return get.toString();
	}

	//首字母大写
	public String upperFirstChar(String src) {

		return src.substring(0, 1).toUpperCase().concat(src.substring(1));
	}

	//首字母小写
	public String lowerFirstChar(String src){
		return src.substring(0,1).toLowerCase().concat(src).substring(1);
	}	

	//获取字段
	private String getFieldStr(String field, String type) {
		StringBuilder sb = new StringBuilder();
		sb.append("\t").append("private ").append(type).append(" ")
		.append(field).append(";");
		sb.append("\r\n");
		return sb.toString();
	}

}





