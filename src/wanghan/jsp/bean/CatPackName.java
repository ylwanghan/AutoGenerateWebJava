package wanghan.jsp.bean;

public class CatPackName {

	private String id;//功能id
	private String catname;//网站生成目录
	private String catnameold;//原始目录
	private String jspCatName;//Jsp生成目录
	private String beanPackName;//bean包名
	private String daoPackName;//dao包名
	private String servletPackName;//servlet包名
	private String utilPackName;//util包名
	private String projectName;//工程名称
	private String jspListName;//jsp查询页面名称
	public String getJspListTitle() {
		return jspListTitle;
	}
	public void setJspListTitle(String jspListTitle) {
		this.jspListTitle = jspListTitle;
	}
	public String getJspAddTitle() {
		return jspAddTitle;
	}
	public void setJspAddTitle(String jspAddTitle) {
		this.jspAddTitle = jspAddTitle;
	}
	private String jspAddName;//jsp添加页面名称
	private String jspListTitle;//jsp查询页面标题
	private String jspAddTitle;//jsp添加页面标题
	public String getJspListName() {
		return jspListName;
	}
	public void setJspListName(String jspListName) {
		this.jspListName = jspListName;
	}
	public String getJspAddName() {
		return jspAddName;
	}
	public void setJspAddName(String jspAddName) {
		this.jspAddName = jspAddName;
	}
	private String xmlCatName;//XML路径	
	
	public String getXmlCatName() {
		return xmlCatName;
	}
	public void setXmlCatName(String xmlCatName) {
		this.xmlCatName = xmlCatName;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCatname() {
		return catname;
	}
	public void setCatname(String catname) {
		this.catname = catname;
	}
	public String getCatnameold(){
		return catnameold;
	}
	public void setCatnameold(String catnameold){
		this.catnameold = catnameold;
	}
	public String getJspCatName() {
		return jspCatName;
	}
	public void setJspCatName(String jspCatName) {
		this.jspCatName = jspCatName;
	}
	public String getBeanPackName() {
		return beanPackName;
	}
	public void setBeanPackName(String beanPackName) {
		this.beanPackName = beanPackName;
	}
	public String getDaoPackName() {
		return daoPackName;
	}
	public void setDaoPackName(String daoPackName) {
		this.daoPackName = daoPackName;
	}
	public String getServletPackName() {
		return servletPackName;
	}
	public void setServletPackName(String servletPackName) {
		this.servletPackName = servletPackName;
	}
	public String getUtilPackName() {
		return utilPackName;
	}
	public void setUtilPackName(String utilPackName) {
		this.utilPackName = utilPackName;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
}
