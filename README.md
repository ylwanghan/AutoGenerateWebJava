# AutoGenerateWebJava
### 基于MySQL自动生成JavaWeb工程

本系统会根据MySQL数据库表的数据和元数据，自动生成关于单一数据表的增删改查，批处理，分页功能。生成后的新项目为传统的三层架构。系统所用开发工具MyEclipse 2018 8.0 和 Tomcat 8.5。

1. 把文件夹AutoWeb复制到E:盘下，文件夹内包含MyEclipse中JavaWeb工程中所必需的文件，以及生成项目用到的jar包，CSS，JavaScript文件等。
   
2. [系统运行地址](http://localhost:8080/AutoGenerateWebJava/admin/login.jsp)打开登录页面，输入localhost、3306、root、密码，连接登录MySQL数据库。
   
3. 左侧显示MySQL中所有的数据库，点击数据库，选择所要操作的数据库表，在界面中要显示的字段，填写工程相关信息。
   
4. 点击生成代码，右侧面板显示新生成的相关代码信息，证明工程生成成功，去相关目录查看新生成的工程。
   
5. 在MyEclipse中Import后测试，网站路径：localhost:8080/工程名/main.jsp
