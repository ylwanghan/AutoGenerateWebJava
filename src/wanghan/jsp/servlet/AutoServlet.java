package wanghan.jsp.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import wanghan.jsp.bean.CatPackName;
import wanghan.jsp.bean.MyConnection;
import wanghan.jsp.bean.TableDetail;
import wanghan.jsp.dao.AutoDao;
import wanghan.jsp.util.CopyFileUtil;

public class AutoServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public String _database;
	public String _tablename;
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//设置编码格式
		request.setCharacterEncoding("utf-8");
		//执行操作类型（登录，注销）
		String method=request.getParameter("method");
		if("login".equals(method)){
			//登录操作
			try {
				login(request,response);
			} catch (SQLException e) {
				e.printStackTrace();
			} 
		} //查询数据库所有表
		else if("queryTable".equals(method)){
			try {
				queryTable(request,response);
			} catch (SQLException e) {
				e.printStackTrace();
			}//查询表中所有信息
		}else if("queryDetail".equals(method)){
			try {
				queryDetail(request,response);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else if("create".equals(method)){
			create(request,response);
		}	      
	}
	/**
	 * 登录并连接MySQL数据库
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws SQLException
	 * @throws ServletException
	 */
	public void login(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException, ServletException {
		String _server=request.getParameter("server");
		String _port=request.getParameter("port");
		String _user=request.getParameter("user");
		String _password=request.getParameter("password");		
		AutoDao admindao=new AutoDao();		
		if(admindao.checkLogin(_server,_port,_user,_password)){
			//查询所有数据库
			List<MyConnection> list = admindao.checkLogin();			
			request.setAttribute("lists", list);
			request.getRequestDispatcher("Auto.jsp").forward(request,response);			
		}else{
			response.sendRedirect(request.getContextPath()+"/admin/login.jsp?status=1");
		} 		
	}	
	/**
	 * 查询数据库中所有表
	 * @param request
	 * @param response
	 * @throws UnsupportedEncodingException 
	 */
	public void queryTable(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException,IOException,SQLException{
		request.setCharacterEncoding("utf-8");
		//接收前台数据
		_database = request.getParameter("database");		
		//查询实体
		AutoDao autoDao = new AutoDao();
		List<MyConnection> list = autoDao.checkLogin();			
		request.setAttribute("lists", list);
		List<MyConnection> connections = autoDao.queryTable(_database);
		request.setAttribute("connections",connections);			
		request.getRequestDispatcher("Auto.jsp").forward(request,response);		
	}
	/**
	 * 查询表中所有信息
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws SQLException
	 * @throws ServletException
	 */
	public void queryDetail(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException, SQLException{
		request.setCharacterEncoding("UTF-8");
		//接受前台数据
		_tablename = request.getParameter("tablename");
		//查询实体
		AutoDao autoDao = new AutoDao();
		//所有数据库
		List<MyConnection> list = autoDao.checkLogin();			
		request.setAttribute("lists", list);
		//所选数据库所有表
		List<MyConnection> connections = autoDao.queryTable(_database);
		request.setAttribute("connections",connections);	
		//所选表所有信息
		List<TableDetail> tableDetails = autoDao.queryDetail(_tablename);
		TableDetail.tb_details = tableDetails;
		request.setAttribute("tableDetails", tableDetails);		
		request.getRequestDispatcher("Auto.jsp").forward(request, response);
	}	
	/**
	 * 生成代码文件
	 * @param request
	 * @param response
	 * @throws IOException 
	 * @throws ServletException 
	 */
	private void create(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		//实例化目录名对象
		CatPackName catPackName = new CatPackName();
		//接收前台数据
		String id = request.getParameter("id");
		String projectName = request.getParameter("projectName");
		String jspListName = request.getParameter("jspListName");
		String jspAddName = request.getParameter("jspAddName");
		String xmlCatName = request.getParameter("catname")+projectName+"/";
		String jspCatName = request.getParameter("catname")+request.getParameter("projectName")+"/WebRoot/";
		String catname = request.getParameter("catname")+projectName+".src.";
		String beanPackName = request.getParameter("beanPackName");
		String daoPackName = request.getParameter("daoPackName");
		String servletPackName = request.getParameter("servletPackName");
		String utilPackName = request.getParameter("utilPackName");
		String jspListTitle = request.getParameter("jspListTitle");
		String jspAddTitle = request.getParameter("jspAddTitle");
		String[] columnnames = request.getParameterValues("columnname");
		String[] columntype = request.getParameterValues("columntype");
		//把包名和目录名放到目录名对象中
		catPackName.setId(id);
		catPackName.setProjectName(projectName);
		catPackName.setXmlCatName(xmlCatName);
		catPackName.setJspListName(jspListName);
		catPackName.setJspAddName(jspAddName);
		catPackName.setCatname(catname);
		catPackName.setJspCatName(jspCatName);
		catPackName.setBeanPackName(beanPackName);
		catPackName.setDaoPackName(daoPackName);
		catPackName.setServletPackName(servletPackName);
		catPackName.setUtilPackName(utilPackName);
		catPackName.setJspListTitle(jspListTitle);
		catPackName.setJspAddTitle(jspAddTitle);		
		//预览页面面板
		String text = "";		
		//调用Dao层create方法生成代码
		AutoDao autoDao = new AutoDao(); 
		
		if(id.equals("review")){		
			text = autoDao.createBean(catPackName, _database, _tablename);
			text += autoDao.createDao(catPackName, _database, _tablename);
			text += autoDao.createUtil(catPackName, _database, _tablename);
			text += autoDao.createXml(catPackName, _tablename);
			text += autoDao.createServlet(catPackName, _tablename);
			text += autoDao.createListJsp(catPackName, _tablename, columnnames);
			text += autoDao.createAddJsp(catPackName, _tablename);
			text += autoDao.createHeadJsp(catPackName, _tablename);
			text += autoDao.createMainJsp(catPackName, _tablename);
			text += autoDao.createLeftJsp(catPackName, _tablename);
			text += autoDao.createProject(catPackName, _tablename);
			//填充预览面板中所生成的代码
			request.setAttribute("text",text);
			//测试获取服务器上项目路径
			System.out.println(request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath());
			//复制MyEclipse中Java项目里不变的文件
			CopyFileUtil.copyDirectory("E:/AutoWeb/lib", jspCatName+"WEB-INF/lib", true);
			CopyFileUtil.copyDirectory("E:/AutoWeb/static", jspCatName+"static", true);
			CopyFileUtil.copyDirectory("E:/AutoWeb/META-INF", jspCatName+"META-INF", true);
			CopyFileUtil.copyDirectory("E:/AutoWeb/.settings", xmlCatName+".settings", true);
			CopyFileUtil.copyFile("E:/AutoWeb/.classpath", xmlCatName+".classpath", true);
			text += autoDao.createComponent(catPackName, _tablename);
		}
		request.getRequestDispatcher("Auto.jsp").forward(request,response);			
	}
}
