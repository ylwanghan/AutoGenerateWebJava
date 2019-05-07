<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.sql.Connection"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>网站自动生成系统</title>
<link href="css/bootstrap.min.css" rel="stylesheet">
<link href="css/datepicker3.css" rel="stylesheet">
<link href="css/styles.css" rel="stylesheet">
<link rel="stylesheet" type="text/css"
	href="js/tree_themes/SimpleTree.css" />
<script type="text/javascript" src="js/jquery-1.6.min.js"></script>
<script type="text/javascript" src="js/SimpleTree.js"></script>
<style type="text/css">
.auto-style1 {
	left: 0px;
	top: -20px;
}
.auto-style2 {
	display: table-cell;
	width: 1%;
	white-space: nowrap;
	vertical-align: middle;
	position: relative;
	font-size: 0;
	left: -46px;
	top: -152px;
	height: 34px;
}
</style>
<script type="text/javascript">
	//checkbox 全选/取消全选 
	var isCheckAll = false;
	function swapCheck() {
		if (isCheckAll) {
			$("input[id='cb']").each(function() {
				this.checked = false;
			});
			isCheckAll = false;
		} else {
			$("input[id='cb']").each(function() {
				this.checked = true;
			});
			isCheckAll = true;
		}
	}
</script>
<script type="text/javascript">
	function _test() {
		var a = document.getElementById("_namespace").value;
		if (_model.checked == false && _dal.checked == false) {
			alert("至少选择一项!");
			return false;
		}
		return true;
	}
	//预览并生成代码
	function create(obj) {
		var columnname = "";
		var columntype = "";
		var projectName = document.getElementById("projectName").value;
		var jspListName = document.getElementById("jspListName").value;
		var jspAddName = document.getElementById("jspAddName").value;
		var catname = document.getElementById("pathName").value;
		var beanPackName = document.getElementById("beanPackName").value;
		var daoPackName = document.getElementById("daoPackName").value;
		var servletPackName = document.getElementById("servletPackName").value;
		var utilPackName = document.getElementById("utilPackName").value;
		var jspListTitle = document.getElementById("jspListTitle").value;
		var jspAddTitle = document.getElementById("jspAddTitle").value;
		
		var arrs = document.getElementsByName("cb_title");
		check_val = [];
		for (k in arrs) {
			if (arrs[k].checked) {
				check_val.push(arrs[k].value);
				arr = [];
				arr = arrs[k].value.split(",");
				columnname += "columnname=" + arr[0] + "&";
				columntype += "columntype=" + arr[1] + "&";
			}
		}
		document.location.href = '${pageContext.request.contextPath}/admin/autoServlet?method=create&' + columnname + columntype + 'id=' + obj.id + '&projectName=' + projectName + '&jspListName=' + jspListName + '&jspAddName=' + jspAddName + '&catname=' + catname + '&beanPackName=' + beanPackName + '&daoPackName=' + daoPackName + '&servletPackName=' + servletPackName + '&utilPackName=' + utilPackName + '&jspListTitle=' + jspListTitle + '&jspAddTitle=' + jspAddTitle;
	}
</script>
</head>
<body>
	<form>
		<nav class="navbar navbar-inverse navbar-fixed-top" role="navigation">
		<div class="container-fluid">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle collapsed">
					<span class="sr-only">Toggle navigation</span> <span
						class="icon-bar"></span> <span class="icon-bar"></span> <span
						class="icon-bar"></span>
				</button>
				<a class="navbar-brand" href="#">网站自动生成系统</a>

			</div>
		</div>
		<!-- /.container-fluid --> </nav>
		<div id="sidebar-collapse" class="col-sm-3 col-lg-2 sidebar">

			<div class="form-group" style="padding-left:20px;padding-top:10px;">
				<label>数据库</label>
			</div>

			<ul class="nav menu">
				<!-- forEach遍历出lists  -->
				<c:forEach items="${lists}" var="item" varStatus="status">
					<li><a
						href="${pageContext.request.contextPath}/admin/autoServlet?method=queryTable&database=${item.TABLE_CAT}">${item.TABLE_CAT}</a>
					</li>
				</c:forEach>
			</ul>
		</div>
		<div class="col-sm-9 col-sm-offset-3 col-lg-10 col-lg-offset-2 main">
			<div class="row">
				<ol class="breadcrumb">
					<li><a href="#"><span class="glyphicon glyphicon-home"></span></a></li>
					<li class="active">DataBase</li>
				</ol>
			</div>
			<!--/.row-->

			<div class="row">
				<div class="col-md-8">
					<div class="panel panel-default chat">
						<div class="panel-heading" id="accordion">
							<span class="glyphicon glyphicon-comment"></span> 数据库表
						</div>
						<div class="panel-body">
							<table width="50%" border="0" cellpadding="0" cellspacing="1"
								bgcolor="b5d6e6">
								<tr>
									<td width="10%" height="22" background="images/bg.gif"
										bgcolor="#FFFFFF">
										<div align="center">
											<span class="STYLE1">表名</span>
										</div>
									</td>
									<td width="10%" height="22" background="images/bg.gif"
										bgcolor="#FFFFFF" class="STYLE1">
										<div align="center">基本操作</div>
									</td>
								</tr>
								<c:forEach items="${connections}" var="item" varStatus="status">
									<tr>
										<td width="10%" height="22" background="images/bg.gif"
											bgcolor="#FFFFFF">
											<div align="center">${item.tablename}</div>
										</td>
										<td width="10%" height="22" background="images/bg.gif"
											bgcolor="#FFFFFF">
											<div align="center">
												<a
													href="${pageContext.request.contextPath}/admin/autoServlet?method=queryDetail&tablename=${item.tablename}">
													<img src="assets/img/edt.gif" />
												</a>
											</div>
										</td>
									</tr>
								</c:forEach>
							</table>
						</div>
					</div>
				</div>
			</div>
			<!--/.row-->
			<div class="row">
				<div class="col-xs-12 col-md-6 col-lg-3"></div>
				<div class="auto-style1"></div>
			</div>
			<!--/.row-->
			<div class="row"></div>
			<!--/.row-->
			<div class="row"></div>
			<!--/.row-->
			<div class="row">
				<div class="col-md-8">
					<div class="panel panel-default chat">
						<div class="panel-footer">
							<div class="input-group">
								<input type="checkbox" value="Bean" id="bean" />&nbsp;Bean层&nbsp;
								<input type="checkbox" value="Servlet" id="servlet" />&nbsp;Servlet层&nbsp;
								<input type="checkbox" value="Dao" id="dao" />&nbsp;Dao层&nbsp;
								<input type="checkbox" value="Database" id="database">&nbsp;Util层&nbsp;
								<input type="checkbox" value="Jsp" id="jsp">&nbsp;Jsp&nbsp;
								<input type="checkbox" value="XML" id="xml" />&nbsp;XML&nbsp; <br />
								<br /> 工程名:&nbsp;&nbsp;<input type="text" id="projectName"
									style="margin-left:16px;" />&nbsp;&nbsp;生成路径&nbsp;(格式：磁盘:/文件夹/...):
								<input id="pathName" type="text" name="path" size="30"><br>
								<br> Bean包名:&nbsp;<input type="text" id="beanPackName" />&nbsp;&nbsp;
								Servlet包名:&nbsp;<input type="text" id="servletPackName" /><br>
								<br> Dao包名:&nbsp;&nbsp;&nbsp;<input type="text"
									id="daoPackName" />&nbsp;&nbsp; Util包名:&nbsp;&nbsp;<input
									type="text" id="utilPackName" style="margin-left:20px;" /><br>
								<br> 查询页面:&nbsp;&nbsp;<input type="text" id="jspListName"
									value="" />&nbsp;&nbsp; 页面名称:&nbsp;&nbsp;<input type="text"
									id="jspListTitle" style="margin-left:15px;"><br> <br>
								添加页面:&nbsp;&nbsp;<input type="text" id="jspAddName" value="" />&nbsp;&nbsp;
								页面名称:&nbsp;&nbsp;<input type="text" id="jspAddTitle"
									style="margin-left:15px;"><br> <br>
								<br>
							</div>
						</div>
						<div class="panel-heading" id="accordion2">
							<span class="glyphicon glyphicon-comment"></span> 请选择显示的字段
						</div>
						<div class="panel-body">
							<table width="100%" border="0" cellpadding="0" cellspacing="1"
								bgcolor="b5d6e6">
								<tr>
									<td width="5%" height="22" background="images/bg.gif"
										bgcolor="#FFFFFF">
										<div align="center">
											<input type="checkbox" id="cb" name="_all"
												onclick="swapCheck()" />
										</div>
									</td>
									<td width="10%" height="22" background="images/bg.gif"
										bgcolor="#FFFFFF">
										<div align="center">
											<span class="STYLE1">字段标题</span>
										</div>
									</td>
									<td width="10%" height="22" background="images/bg.gif"
										bgcolor="#FFFFFF">
										<div align="center">
											<span class="STYLE1">字段名称</span>
										</div>
									</td>

									<td width="10%" height="22" background="images/bg.gif"
										bgcolor="#FFFFFF">
										<div align="center">
											<span class="STYLE1">字段类型</span>
										</div>
									</td>
									<td width="10%" height="22" background="images/bg.gif"
										bgcolor="#FFFFFF">
										<div align="center">
											<span class="STYLE1">是否为空</span>
										</div>
									</td>
									<td width="10%" height="22" background="images/bg.gif"
										bgcolor="#FFFFFF">
										<div align="center">
											<span class="STYLE1">是否自增</span>
										</div>
									</td>

								</tr>
								<c:forEach items="${tableDetails}" var="item" varStatus="status">
									<tr>
										<td width="5%" height="22" background="images/bg.gif"
											bgcolor="#FFFFFF">
											<div align="center">
												<input type="checkbox" id="cb" name="cb_title"
													value="${item.columnname},${item.columntype}" />
											</div>
										</td>
										<td width="10%" height="22" background="images/bg.gif"
											bgcolor="#FFFFFF">
											<div align="center">${item.title}</div>
										</td>
										<td width="10%" height="22" background="images/bg.gif"
											bgcolor="#FFFFFF">
											<div align="center">${item.columnname}</div>
										</td>
										<td width="10%" height="22" background="images/bg.gif"
											bgcolor="#FFFFFF">
											<div align="center">${item.columntype}</div>
										</td>
										<td width="10%" height="22" background="images/bg.gif"
											bgcolor="#FFFFFF">
											<div align="center">${item.isnull}</div>
										</td>
										<td width="10%" height="22" background="images/bg.gif"
											bgcolor="#FFFFFF">
											<div align="center">${item.isAutoInctement}</div>
										</td>
									</tr>
								</c:forEach>
							</table>
						</div>
						<button class="btn btn-success btn-md" type="button"
							onclick="if(!create(this)) return false;" id="review"
							style="margin-right:20px; margin-left:400px;">生成代码</button>
					</div>
				</div>
				<!--/.col-->
				<div class="col-md-4">
					<div class="panel panel-blue">
						<div class="panel-body">
							<textarea id="_text" readonly="readonly"
								style="font-family:'Franklin Gothic Medium', 'Arial Narrow', Arial, sans-serif;color:red; width:300px; height:380px; padding:0px;margin:0px;">
                     	${text}
                     </textarea>
						</div>
					</div>
				</div>
				<!--/.col-->
			</div>
			<!--/.row-->
		</div>
		<!--/.main-->
		<script src="js/jquery-1.11.1.min.js"></script>
		<script src="js/bootstrap.min.js"></script>
		<script src="js/chart.min.js"></script>
		<script src="js/chart-data.js"></script>
		<script src="js/easypiechart.js"></script>
		<script src="js/easypiechart-data.js"></script>
		<script src="js/bootstrap-datepicker.js"></script>
	</form>
</body>
</html>

