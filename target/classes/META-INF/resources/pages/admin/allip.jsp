<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
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
    <title>ip日志</title>
</head>
<body>
<div>
    <div class="container" style="max-height: 99%; max-width:99%;margin-left: 0px;
    float:left; text-align:center;margin-right: 0px;">
        <p style="font-size: 50px;">Mooc IP日志</p>
        <div class="container" style="margin-top: 50px;
			margin-right: 0px;padding-left: 0px;padding-right: 0px;">
            <table class="table table-striped" width="950">
                <tr>
                    <td class="col-md-2 text-center">ip地址</td>
                    <td class="col-md-2 text-center">状态</td>
                    <td class="col-md-2 text-center">备注</td>
                    <td class="col-md-2 text-center">初次访问时间</td>
                    <td class="col-md-2 text-center">最后访问时间</td>
                    <td class="col-md-2 text-center">封禁至</td>
                    <td class="col-md-2 text-center" colspan="2">操作</td>
                </tr>
                <!--如果没有ip数据,显示暂无信息-->
                <c:if test="${ips.size()==0 }">
                    <tr>
                        <td colspan="5" style="font-size: 35px;text-align:center;">暂无IP</td>
                    </tr>
                </c:if>
				<!--否则循环输出ip信息-->
                <c:forEach items="${ips}" var="ip">
                    <tr>
						<!--ip地址-->
                        <td class="col-md-2 text-center ">
                                ${ip.ip }
                        </td>
						<!--正常还是已经封锁信息,根据type字段判断-->
                        <td class="col-md-2 text-center">
                            <c:choose>
                                <c:when test="${ip.type eq '1'}">
                                    <b style="color: red;">已封禁!</b>
                                </c:when>
                                <c:otherwise>
                                    正常！
                                </c:otherwise>
                            </c:choose>
                        </td>
						<!--封禁原因-->
                        <td class="col-md-2 text-center">${ip.mark }</td>
						<!--首次访问时间-->
                        <td class="col-md-2 text-center">
                            <fmt:formatDate value="${ip.firsttime}" pattern="yyyy-MM-dd HH:mm:ss"/>
                        </td>
						<!--最后访问时间-->
                        <td class="col-md-2 text-center">
                            <fmt:formatDate value="${ip.totime}" pattern="yyyy-MM-dd HH:mm:ss"/>
                        </td>
						<!--根据ip的type字段输出屏蔽时间-->
                        <td class="col-md-2 text-center">
                            <c:if test="${ip.type eq '0'}">
                                -------------
                            </c:if>
                            <c:if test="${ip.type ==null}">
                                -------------
                            </c:if>
                            <c:if test="${ip.type eq '1'}">
                                <fmt:formatDate value="${ip.bantime}" pattern="yyyy-MM-dd HH:mm:ss"/>
                            </c:if>
                        </td>
						<!--屏蔽ip按钮,请求带着ip请求ipset接口-->
                        <td class="col-md-2 text-center">
                            <a class="btn btn-danger delete" style="background-color: red;" href="ipset?ip=${ip.ip }">封禁IP</a>
                        </td>
						<!--如果已经封禁ip则显示解除封禁按钮,传递参数onbaned,请求ipset接口-->
                        <td class="col-md-2 text-center">
                            <c:if test="${ip.type eq '1'}">
                                <a class="btn btn-danger delete" style="background-color: red;"
                                   href="ipset?ip=${ip.ip }&onbaned=onbaned">解除封禁</a>
                            </c:if>
                        </td>
                    </tr>


                </c:forEach>
            </table>
            <center>
				<!--分页-->
                <c:if test="${page!=0 }">
                    <button onclick="prevpage()">上一页</button>
                </c:if>
                <button onclick="reflash()">刷新</button>
                <c:if test="${page!=maxpage-1 }">
                    <button onclick="nextpage()">下一页</button>
                </c:if>
            </center>
        </div>
    </div>
</div>
</body>
<script type="text/javascript">
    function reflash() {
        window.location.reload(true);
    }

    function prevpage() {
        var page = ${page}-1;
        window.location.href = "allip?page=" + page;
    }

    function nextpage() {
        var maxpage = ${maxpage};
        var page = ${page}+1;
        if (page > maxpage) {
            alert("这是最后一页！");
        } else {
            window.location.href = "allip?page=" + page;
        }
    }

</script>
</html>