<%@ page language="java" contentType="text/html; charset=UTF-8"
         import="com.mooc.entity.User" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    String jsUrl = request.getContextPath() + "/style/js/";
    String cssUrl = request.getContextPath() + "/style/css/";
    String imgUrl = request.getContextPath() + "/style/img/";
%>
<link href="<%=cssUrl%>bootstrap.min.css" rel="stylesheet">
<script type="text/javascript" src="<%=jsUrl%>jquery-3.2.1.min.js"></script>
<script type="text/javascript" src="<%=jsUrl%>bootstrap.min.js"></script>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>课程属性</title>
</head>
<body>
<form method="post" action="/save" enctype="multipart/form-data">
	上传课程视频:<input type="file" name="ogg">
    上传课程图片:<input type="file" name="jpg">
	<input type="submit" value="上传">
</form>

</body>
</html>