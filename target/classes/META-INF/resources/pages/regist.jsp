<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>注册</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <script src="style/js/loginkuang.js"></script>
    <link rel="stylesheet" href="style/css/course.css">
    <link rel="stylesheet" href="style/css/bootstrap.min.css">
    <script src="style/js/jquery.min.js"></script>
    <script src="style/js/bootstrap.min.js"></script>
    <link rel="stylesheet" type="text/css" href="style/css/11.css">

</head>
<!-- ajax验证用户名可用 -->
<!-- var url = "${pageContext.request.contextPath }/user/checkUser?username="
					+ (username.value) + "&time=" + new Date().getTime(); -->
<script type="text/javascript">


    function changevarcode() {
        var src = "changevarcode?t=" + new Date().getTime();
        $("#varcodeimg").attr("src", src);
    }
    function validateRegist() {
		var username = $("#username").val();
		var password = $("#password").val();
		var rpassword = $("#rpassword").val();
		var varcode = $("#varcode").val();
		var isok = true;
		if (username == "" || password == "") {
			$("#registInfo").html(
					"<b style='color:red;font-size:15px;'>用户名或密码不能为空!</b>");
			isok = false;
			return;
		}
		if (rpassword == "") {
			$("#registInfo").html(
					"<b style='color:red;font-size:15px;'>请输入确认密码！</b>");
			isok = false;
			return;
		}
		if (rpassword != password) {
			$("#registInfo").html(
					"<b style='color:red;font-size:15px;'>两次密码不同！</b>");
			isok = false;
			return;
		}
		if (varcode == "") {
			$("#registInfo").html(
					"<b style='color:red;font-size:15px;'>验证码不能为空!</b>");
			isok = false;
			return;
		}
		$.ajax({
			type: "post",
			url: "varcodecheck",
			data: {
				"varcode": varcode
			},
			async: false,
			dataType: 'text',
			success: function (data) {
				if (data == "0") {
					$("#registInfo")
							.html("<b style='color:red;font-size:15px;'>验证码错误!</b>");
					isok = false;
					return;
				} else {
					$
							.ajax({
								type: "post",
								url: "usercheck",
								data: {
									"username": username
								},
								async: false,
								datatype: "text",
								success: function (haveUser) {
									if (haveUser==1) {
										$("#registInfo").html(
												"<b style='color:red;font-size:15px;'>用户名已经存在!</b>");
										isok = false;
									} else {
										$("#registInfo").html("正在注册.....");
									}
								},
								error: function (data) {
									alert("注册出错！请联系管理员" + data);
									isok = false;
								}
							});
				}
			},
			error: function (data) {
				alert("登录出错！请联系管理员" + data);
				isok = false;
				return;
			}
		});

		if (isok) {
			$("#form1").submit();
		}
	}
</script>
<body>
<div id="ui" style="text-align: center;">
    <div class="user ">
        <div class="logo">
            <a href=#> <img
                    src="${pageContext.request.contextPath }/style/image/1.jpg"
                    width="204px" class="img-rounded"></a>
        </div>
        <!-- uiView:  -->
        <div data-ui-view="" class="container">
            <nav class="navbar navbar-default" role="navigation"
                 style="border: 0px solid transparent !important;">


                <ul class="nav navbar-nav" style="width: auto; margin-left: 490px;">
                    <li><a href="login" style="font-size: 20px;">登录</a></li>
                    <li class="active"><a href="#" style="font-size: 20px;">注册</a></li>

                </ul>


            </nav>

            <div class="container-fluid full ">
                <form action="regist"
                      class="form-horizontal col-md-9 col-md-offset-2 " role="form"
                      method="post" id="form1">
                    <!--用户名-->
                    <div class="form-group">
                        <label for="username" class="col-md-3 control-label">用户名</label>
                        <div class="col-md-6">
                            <input name="username" type="text" id="username" maxlength="10"
                                   placeholder="请输入用户名" class="input form-control"
                                   onkeyup="ajax()" autofocus/>
                        </div>
                    </div>
                    <!--密码-->
                    <div class="form-group">
                        <label for="password" class="col-md-3 control-label">密码</label>
                        <div class="col-md-6">
                            <input name="password" type="password" id="password"
                                   maxlength="15" placeholder="请输入密码" class="input form-control">
                        </div>


                    </div>

                    <!--再次输入密码-->
                    <div class="form-group">
                        <label for="rpassword" class="col-md-3 control-label">密码</label>
                        <div class="col-md-6">
                            <input name="rpassword" type="password" id="rpassword"
                                   maxlength="15" placeholder="请再次输入密码" class="input form-control">
                        </div>

                    </div>

                    <!--验证码-->
                    <div class="form-group">
                        <label for="varcode" class="col-md-3 control-label">验证码</label>
                        <div class="col-md-6">
                            <input name="varcode" type="text" id="varcode"
                                   maxlength="15" placeholder="请输入验证码" class="input form-control">
                        </div>
                    </div>
                    <!--验证码-->
                    <div class="form-group">
                        <img onclick="changevarcode()" id="varcodeimg" alt="验证码" src="changevarcode"> <br>
                    </div>

                    <!-- /.form-group -->

                    <div class="form-group">
                        <div id="registInfo"></div>
                    </div>
                    <!-- /.form-group -->
                    <div class="form-group">
                        <div class="col-md-9 col-md-offset-1">
                            <button type="button" id="submit1"
                                    class="btn btn-primary btn-block" onclick="validateRegist()">立即注册
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
        <div class="footer">
            <a id="back_home" href="index"> 返回首页</a>

        </div>
    </div>
</div>


<footer style="text-align: center">
    <hr>
    <p class="am-padding-left">© 2020 <a href="#">成都信息工程大学</a>. 作者:王彦淇</p>
</footer>


</body>
</html>