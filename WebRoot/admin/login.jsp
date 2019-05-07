<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en" class="no-js">
    <head>
        <meta charset="utf-8">
        <title>登录页面</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="description" content="">
        <meta name="author" content="">

        <!-- CSS -->
        <link rel='stylesheet' href='http://fonts.googleapis.com/css?family=PT+Sans:400,700'>
        <link rel="stylesheet" href="assets/css/reset.css">
        <link rel="stylesheet" href="assets/css/supersized.css">
        <link rel="stylesheet" href="assets/css/style.css">
       
         <script type="text/javascript">
             function test() {              
              var value1 = document.getElementById("_server").value;
              var value2 = document.getElementById("_port").value;
              var value3 = document.getElementById("_user").value;
              var value4 = document.getElementById("_password").value;
           
            if (value1.trim() == "") {
                alert("服务器名称不能为空值");
                return false;
            }
            else if (value2.trim() == "") {
                alert("端口号不能为空值");
                return false;
            }
            else if(value3.trim()==""){
            	alert("登录名不能为空值");
            	return false;
            }
            else if(value4.trim()==""){
            	alert("密码不能为空值");
            	return false;
            }
            return true;
        }
    </script>

    </head>

    <body>

        <div class="page-container">
            <h1>Login</h1>		
			<form role="form" name="form1" class="form-horizontal" action="${pageContext.request.contextPath}/admin/autoServlet?method=login"
				method="post" id="checkForm">		
                <input type="text" id="_server" name="server" class="server" placeholder="服务器名称">
                <input type="text" id="_port" name="port" class="port" placeholder="端口">
                <input type="text" id="_user" name="user" class="user" placeholder="登录名">
                <input type="password" id="_password" name="password" class="password" placeholder="密码">
                <button type="submit" id="_connection"  onclick="if(!test()) return false;">连接数据库</button>
               
                
                <div class="error"><span>+</span></div>
            </form>
			
	  </div>
        
        <!-- Javascript -->
        <script src="assets/js/jquery-1.8.2.min.js"></script>
        <script src="assets/js/supersized.3.2.7.min.js"></script>
        <script src="assets/js/supersized-init.js"></script>
        <script src="assets/js/scripts.js"></script>

    </body>

</html>

