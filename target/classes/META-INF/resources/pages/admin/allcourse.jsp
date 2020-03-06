<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
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
<title>mooc后台管理系统</title>
</head><%-- 
<%@include file="leftmeun.jsp"%> --%>

<body>
	<div>
		<div class="container"
			style="max-height: 100%; max-width:100%; margin-left: 0px; float: left; margin-right: 0px;">
			<p style="font-size: 50px;text-align: center;">Mooc课程管理</p>
			<div class="container"
				style="max-height: 100%; max-width:100%; margin-top: 50px; margin-right: 0px; padding-left: 0px; padding-right: 0px;">
				<table class="table table-striped" width="950">
					<!--表头-->
					<tr>
						<td class="col-md-2 text-center">课程ID</td>
						<td class="col-md-2 text-center">课程名</td>
						<td class="col-md-2 text-center">课程介绍</td>
						<td class="col-md-2 text-center">课程评价</td>
						<td class="col-md-2 text-center">课程订阅数</td>
						<td class="col-md-2 text-center">课程属性</td>
						<td class="col-md-2 text-center">课程老师</td>
						<td class="col-md-2 text-center">操作</td>
						<td class="col-md-2 text-center">删除课程</td>
					</tr>
					<!--如果没有课程,显示没有课程-->
					<c:if test="${courses.size()==0 }">
						<tr>
							<td colspan="5" style="font-size: 35px; text-align: center;">暂无课程
							</td>
						</tr>
					</c:if>
					<!--循环输出课程-->
					<c:forEach items="${courses }" var="course">
						<tr>
							<!--课程id-->
							<td class="col-md-2 text-center ">${course.id }</td>
							<!--课程名-->
							<td class="col-md-2 text-center" style="">${course.name }</td>
							<!--课程详情-->
							<td class="col-md-2 text-center">${course.context }</td>
							<!--课程评价-->
							<td class="col-md-2 text-center">${course.label }</td>
							<!--课程人数-->
							<td class="col-md-2 text-center">${course.hour }</td>
							<!--课程类别分类显示-->
							<td class="col-md-2 text-center">
							<c:if test="${course.type==1 }">
							会员课
							</c:if>
							<c:if test="${course.type==0 }">
							免费课
							</c:if>
							</td>
							<td class="col-md-2 text-center">
								${course.teacherid}
							</td>
							<!--课程是否上架,根据price字段判断,price=1未上架,0已经上架-->
							<td class="col-md-2 text-center ">
										<c:if test="${course.price == 1}">
											<a class="btn btn-danger delete"
												style="background-color: gray;"
												href="bancourse?courseid=${course.id }&type=0">上架</a>
										</c:if>
										<c:if test="${course.price==0}">
											<a class="btn btn-danger delete"
												href="bancourse?courseid=${course.id }&type=1">下架</a>
										</c:if>
									</td>
							<!--删除课程接口调用removecourse接口,发送ajax请求-->
							<td class="col-md-2 text-center ">
										<a class="btn btn-danger delete"
											style="background-color: red;"
											href="javascript:removecourse(courseid='${course.id }')">删除课程</a>
									</td>
						</tr>


					</c:forEach>
				</table>
				<center>
				<c:if test="${page!=0 }">
					<button onclick="prevpage()">上一页</button>
				</c:if>
				<button onclick="reflash()">刷新</button>
				<c:if test="${page==maxpage-1 }">
					<button onclick="nextpage()">下一页</button>
				</c:if>
				</center>

			</div>
		</div>
	</div>
	<script type="text/javascript">
function reflash(){
	window.location.reload(true);
}
function prevpage(){
	var page = ${page}-1;
	window.location.href="logpage?page="+page;
}
function nextpage(){
	var maxpage = ${maxpage};
	var page = ${page}+1;
	if(page>maxpage){
		alert("这是最后一页！");
	}else{ 
	window.location.href="logpage?page="+page;
    }
	}
function removecourse(courseid){
	var r=confirm("确认要删除课程："+courseid+"?");
	if (r==true){
		var removepassword=prompt("请输入删除密码","");
		if (removepassword==null || removepassword==""){
			alert("删除密码不能为空！");
		    return;
		}else{
		$.ajax({
			type : "post",
			url : "deletecourse",
			data : {
				"courseid" : courseid,
				"removepassword" : removepassword
			},
			async : true,
			dataType : 'text',
			success : function(data) {
				if (data == "0") {
					alert("删除密码错误！");
					return;
				}
				alert(data);
				location.reload(true);
			},
			error : function(data) {
				alert("出错！请联系管理员" + data);
				isok = false;
			}
		});}
	}
}
</script>
</body>
</html>