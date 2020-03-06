package com.mooc.controller;

/**
 * Romantic
 * 2020.2.26
 * 996261913@qq.com
 */
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mooc.biz.CourseBiz;
import com.mooc.biz.LogBiz;
import com.mooc.biz.MessageBiz;
import com.mooc.biz.ReviewBiz;
import com.mooc.biz.UserBiz;
import com.mooc.entity.*;
import com.mooc.util.DateUtil;
import com.wf.captcha.utils.CaptchaUtil;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UserController {
	@Autowired
	UserBiz userBiz;
	@Autowired
	CourseBiz courseBiz;
	@Autowired
	MessageBiz messageBiz;
	@Autowired
	ReviewBiz reviewBiz;
	@Autowired
	LogBiz logBiz;

	/**
	 * 普通日志写入
	 * 
	 * @param loginUser
	 * @param ip
	 * @param type
	 */
	public void setlog(User loginUser, String ip, String type) {
		Log log = new Log();
		log.setUserid(loginUser.getId());
		log.setUsername(loginUser.getUsername());
		log.setIp(ip);
		log.setType(type);
		logBiz.insert(log);
	}

	//登录方法,如果未登录,进入login页面,同时记录日志
	//同时判断会员到期,设置vip为null,下架所有课程
	@RequestMapping(value = "login")
	public String login(User user, HttpSession session, HttpServletRequest req) {
		Map<String, String> paramMap = new HashMap<String, String>();
		//放入参数
		paramMap.put("username", user.getUsername());
		paramMap.put("password", user.getPassword());
		//当前时间
		Date date=new Date();
		//看是否能根据用户名密码,查询到数据库用户
		User loginUser = userBiz.selectLoginUser(paramMap);
		//说明没有这个用户,返回登录页面
		if (loginUser == null) {
			return "login";
		}
		if(loginUser.getVip()!=null){
			if(loginUser.getVip().getTime()<date.getTime()){
				//如果登录的用户会员已经过期
				loginUser.setVip(null);
				List<Review> reviews = reviewBiz.selectbyuserid(loginUser.getUsername());
				for (int a = 0; a < reviews.size(); a++) {//个人信息修改的同时更新评论的用户信息
					reviews.get(a).setVip(0);
				}
				//更新评论
				//更新用户
				reviewBiz.updateByPrimaryKeySelective(reviews);
				userBiz.updateVIPisNullByUserName(loginUser.getUsername());
				System.out.println(loginUser.getId());
				messageBiz.deleteByUserId(loginUser.getId());
			}
		}


		//记录登录日志
		setlog(loginUser, req.getRemoteAddr(), "登录");
		//保存session
		//更新用户的会员数据等等
		session.setAttribute("loginUser", loginUser);
		return "redirect:course";
	}

	@RequestMapping(value = "logout")
	//注销登出,并记录日志
	public String logout(String type, User user, HttpSession session, HttpServletRequest req) {
		User loginUser = (User) session.getAttribute("loginUser");
		if (loginUser == null) {
			return "login";
		} else {
			//注销session
			session.invalidate();
			setlog(loginUser, req.getRemoteAddr(), "注销");
			//重定向到首页
			return "redirect:index";
		}
	}

	/*
	 * ajax密码检查,判断是否可以登录,是否被屏蔽等,同时密码错误后记录日志
	 */
	@RequestMapping(value = "passwordcheck")
	public void selectUser(User user, HttpServletResponse response, HttpServletRequest req)
			throws IOException {
		Map<String, String> paramMap = new HashMap<String, String>();
		//放入输入的参数
		paramMap.put("username", user.getUsername());
		paramMap.put("password", user.getPassword());
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		//查询到了一个可登录用户,说明用户名密码正确
		if (userBiz.selectUser(paramMap) == 1) {
			user = userBiz.selectLoginUser(paramMap);
			//不是管理员,是普通用户
			if (!"admin".equals(user.getMission())&&!"showadmin".equals(user.getMission())) {
				if (user.getBuycase() != null) {//根据BuyCase字段分类,判断是否可以登录
					if ("1".equals(user.getBuycase())) {
						out.println("3");// 屏蔽登录
					} else
						out.println("1");// 正常登录密码正确
				} else {
					out.println("1");//
				}
			} else {
				out.println("2");// 管理员返回
			}
		} else {
			//登录失败,记录日志
			Log log = new Log();
			log.setIp(req.getRemoteAddr());
			log.setType("尝试登录账号:" + user.getUsername() + "，密码错误");
			logBiz.insert(log);
			out.println("0");//密码错误返回值
		}
	}

	@RequestMapping(value = "usercheck")
	// 注册检查,检测是否用户名重复,1代表重复,0代表不重复
	public void Usercheck(String username, HttpSession session, HttpServletResponse response) throws IOException {
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		int i = userBiz.selectUser(username);
		out.println(String.valueOf(i));
	}

	@RequestMapping(value = "quickregist")
	// 快速注册,传递验证码,一个用户对象
	public ModelAndView insertUser(String varcode, User user, HttpSession session, HttpServletRequest req, ModelAndView mav) {
		//自动生成id值
		String id = DateUtil.getId();
		//
		String username = user.getUsername();
		mav.setViewName("redirect:course");
		//过滤空验证码
		if (varcode == null) {
			return mav;
		}
		if (userBiz.selectUser(username) == 1 || !CaptchaUtil.ver(varcode, req)) {
			return mav;
		}
		user.setId(id);
		user.setMission("student");
		user.setBuycase(null);
		user.setMycase(null);
		user.setVip(null);
		userBiz.insertSelective(user);
		session.setAttribute("loginUser", user);
		setlog(user, req.getRemoteAddr(), "注册");
		return mav;
	}

	@RequestMapping(value = "regist")
	// 注册并记录日志
	public ModelAndView regist(ModelAndView mav, String varcode, User user, HttpSession session, HttpServletRequest req) {
		//DateUitl生成日期Id
		String id = DateUtil.getId();
		//User对象
		String username = user.getUsername();
		mav.setViewName("redirect:course");
		if (varcode == null) {
			return mav;
		}
		if (userBiz.selectUser(username) == 1 || !CaptchaUtil.ver(varcode, req)) {
			return mav;
		}
		user.setId(id);
		//学生注册
		user.setMission("student");
		user.setBuycase(null);
		user.setMycase(null);
		user.setVip(null);
		userBiz.insertSelective(user);
		//记录注册日志
		setlog(user, req.getRemoteAddr(), "注册");
		return mav;
	}


	// 返回regist.jsp页面
	@RequestMapping(value = "reg")
	public String reg() {
		return  "regist";
	}

	@RequestMapping(value = "showvip")
	// 会员中心接口
	public ModelAndView showvip(HttpSession session,ModelAndView mav) {
		User loginUser = (User) session.getAttribute("loginUser");
		if (loginUser != null) {
			//更新一些数据,将最初的登录用户改变为全部数据的用户
			loginUser = userBiz.selectByPrimaryKey(loginUser.getId());
			session.setAttribute("loginUser", loginUser);
		}
		//返回vip.jsp
		mav.setViewName("vip");
		return mav;
	}

	@RequestMapping(value = "mylearn")
	// 我的课程查询,遍历message表,获取对应的课程
	public ModelAndView myCourse(HttpSession session, ModelAndView mav) {
		User loginUser = (User) session.getAttribute("loginUser");
		if (loginUser == null) {
			mav.setViewName("login");
			return mav;
		}
		List<Course> courses = new ArrayList<Course>();
		//查询上架信息
		List<Message> messages = messageBiz.selectmy(loginUser.getId());
		//遍历上架信息
		for (int i = 0; i < messages.size(); i++) {
			int courseid = messages.get(i).getCourseid();//获取课程id
			Course course = courseBiz.selectByPrimaryKey(courseid);//获取课程
			courses.add(course);//加入课程集合
		}
		mav.addObject("mycourses", courses);
		mav.setViewName("mylearn");//跳转到mylearn.jsp
		return mav;

	}

	@RequestMapping(value = "course")
	// 主页查询所有课程
	public String Course(HttpSession session, Map map) {
		List<Course> courses = courseBiz.selectAllCourse();
		map.put("courses", courses);
		//返回courseindex.jsp
		return "courseindex";

	}

	@RequestMapping(value = "coursedetail")
	// 单课程主页
	//传递课程id,在message表中查询是否有订阅记录
	//注入isSelect代表是否订阅课程
	//注入Course代表某个课程
	public ModelAndView Courseindex(int id, HttpSession session,ModelAndView mav) {
		User loginUser = (User) session.getAttribute("loginUser");
		if (loginUser == null) {//验证是否登录
			mav.setViewName("login");
			return mav;
		}
		//封装message对象,看课程id和用户id是否存在message表里,验证是否订阅
		Message message = new Message();
		message.setCourseid(id);
		message.setUserid(loginUser.getId());
		//查询message对象
		Message me = messageBiz.select(message);
		if (me == null) {
			//放入是否订阅课程字段
			mav.addObject("isSelect", false);
		} else {
			//放入是否订阅课程字段
			mav.addObject("isSelect", true);
		}
		//根据课程id查询课程
		Course course = courseBiz.selectByPrimaryKey(id);
		mav.addObject("course", course);
		mav.setViewName("coursedetail");
		return mav;

	}

	@RequestMapping(value = "coursevideo")
	// 单课程视屏,传递CourseId
	//验证要访问的课程是否是会员,用户是否是会员
	//注入评论列表和单课程对象
	public String Coursevideo(int courseid, HttpSession session, Map map) {
		User loginUser = (User) session.getAttribute("loginUser");
		if (loginUser == null) {
			return "login";
		}
		//根据传递的课程id查询相应的课程
		Course course = courseBiz.selectByPrimaryKey(courseid);
		//校验是否是会员课程
		if ("1".equals(course.getType())) {
			//如果是会员课程用户不是会员
			if (loginUser.getVip() == null) {
				return "vip";
			}
		}
		//放入课程
		map.put("course", course);
		//查询评论
		List<Review> reviews = reviewBiz.select(courseid);
		//放入评论
		map.put("reviews", reviews);
		//返回video页面
		return "coursevideo";

	}

	@RequestMapping(value = "insertCourse")
	// 前台用户订阅课程，传递用户id,课程id
	//订阅前，通过此方法判断是否是会员
	public void insertCourse(int courseid, String userid, HttpSession session, HttpServletRequest req,
			HttpServletResponse response) throws IOException {
		String result = "订阅成功！";
		Date date=new Date();
		User user = (User) session.getAttribute("loginUser");
		Course c = courseBiz.selectByPrimaryKey(courseid);
		if (user.getVip() == null && "1".equals(c.getType())) {
			result = "此课程是会员课程，请购买会员！";
		}
		else if(user.getVip()!=null&&user.getVip().getTime()<date.getTime()){
			//如果会员已经过期,提醒用户充值会员
			result = "会员已经到期!请充值会员!";
			user.setVip(null);
			//循环更新评论表
			List<Review> reviews = reviewBiz.selectbyuserid(user.getUsername());
			for (int a = 0; a < reviews.size(); a++) {//个人信息修改的同时更新评论的用户信息
				reviews.get(a).setVip(0);
			}
			//更新评论
			//更新用户
			reviewBiz.updateByPrimaryKeySelective(reviews);
			userBiz.updateVIPisNullByUserName(user.getUsername());
		}
		else {
			Message message = new Message();
			message.setCourseid(courseid);
			message.setUserid(userid);
			int i = messageBiz.insert(message);
			Course course= courseBiz.selectByPrimaryKey(courseid);
			if(course.getHour()==null||course.getHour().equals("")){
				course.setHour("1");
				courseBiz.updateByPrimaryKeySelective(course);
			}
			else{
				String old = course.getHour();
				int oldHour = Integer.parseInt(old);
				oldHour+=1;
				course.setHour(String.valueOf(oldHour));
				courseBiz.updateByPrimaryKeySelective(course);
			}
			setlog(user, req.getRemoteAddr(), "订阅课程:" + c.getName());
		}
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		out.print(result);
	}

	@RequestMapping(value = "deleteCourse")
	// 前台用户取消订阅课程,传递用户id,课程id
	//删除message表中的订阅记录
	public String deleteCourse(int courseid, String userid, HttpServletResponse response, HttpServletRequest req)
			throws IOException {
		//设置message对象
		Message message = new Message();
		message.setCourseid(courseid);
		message.setUserid(userid);

		PrintWriter out = response.getWriter();
		int i = messageBiz.delete(message);//删除订阅记录
		User loginUser = userBiz.selectByPrimaryKey(userid);//查询是哪个用户要取消课程
		Course c = courseBiz.selectByPrimaryKey(courseid);//查询取消的是哪个课程
		//调用setLog方法
		setlog(loginUser, req.getRemoteAddr(), "取消课程：" + c.getName());
		String result = i > 0 ? "true" : "false";//是否删除了课程,删除了返回true
		return result;
	}
	
	@RequestMapping(value = "info")
	//返回个人信息页面
	public String Info(User user, HttpSession session) {
		User loginUser = (User) session.getAttribute("loginUser");
		if (loginUser == null) {
			return "login";
		}
		return "infoset";
	}

	@RequestMapping(value = "infoset")
	// 个人信息设置
	public String Infoset(User user, HttpSession session, HttpServletRequest req) {
		User loginUser = (User) session.getAttribute("loginUser");
		if (loginUser == null) {
			return "login";
		}
		user.setCollect(loginUser.getCollect());
		//根据用户名查询评论
		List<Review> reviews = reviewBiz.selectbyuserid(loginUser.getUsername());
		for (int a = 0; a < reviews.size(); a++) {//个人信息修改的同时更新评论的用户信息
			reviews.get(a).setSex(user.getSex());
		}
		//更新评论表的一些信息
		reviewBiz.updateByPrimaryKeySelective(reviews);
		//更新user表
		userBiz.updateByPrimaryKeySelective(user);
		//更新session
		Map map = new HashMap<String, String>();
		map.put("username", loginUser.getUsername());
		map.put("password", loginUser.getPassword());
		session.setAttribute("loginUser", userBiz.selectLoginUser(map));
		//更新日志
		setlog(loginUser, req.getRemoteAddr(), "个人信息更改");
		return "redirect:course";

	}

	@RequestMapping(value = "vip")
	// vip购买 0为1个月，1为半年，2为一年
	//更新对应的评论UI,更新session
	public void Vip(HttpSession session, int viptype, HttpServletResponse response, HttpServletRequest req)
			throws IOException {
		String data = "已经成功购买会员";//生成初始化字符串
		User loginUser = (User) session.getAttribute("loginUser");
		if (loginUser == null) {
			// return "login";
		}
		//根据用户名查询评论
		List<Review> reviews = reviewBiz.selectbyuserid(loginUser.getUsername());
		//获取余额
		int collect = loginUser.getCollect();
		boolean isvip = false;
		Date date = new Date();//现在时间
		Date vipdate = loginUser.getVip();//vip到期时间
		//如果不是vip或者vip已经到期,设置vip的时间为当前时间
		if (vipdate == null||vipdate.getTime() < date.getTime()) {
			loginUser.setVip(new Date());
		}
		switch (viptype){
			default:
				data = "请求错误！";
				break;
				//月会员
			case 0:
				if (collect < 500) {
					data = "余额不足，请联系管理员充值！";
				} else {
					loginUser.setCollect(collect - 500);
					vipdate = loginUser.getVip();
					vipdate.setMonth(vipdate.getMonth() + 1);
					loginUser.setVip(vipdate);
					isvip = true;
					//记录购买会员日志
					setlog(loginUser, req.getRemoteAddr(), "购买会员：一个月");
				}
				break;
				//半年卡
			case 1:
				if (collect < 2000) {
					data = "余额不足，请联系管理员充值！";
				} else {
					loginUser.setCollect(collect - 2000);
					vipdate = loginUser.getVip();
					vipdate.setMonth(vipdate.getMonth() + 6);
					loginUser.setVip(vipdate);
					isvip = true;
					//记录购买会员日志
					setlog(loginUser, req.getRemoteAddr(), "购买会员：半年");
				}
				break;
				//年卡
			case 2:
				if (collect < 3000) {
					data = "余额不足，请联系管理员充值！";
				} else {
					loginUser.setCollect(collect - 3000);
					vipdate = loginUser.getVip();
					vipdate.setYear(vipdate.getYear() + 1);//更新vipdate
					loginUser.setVip(vipdate);
					isvip = true;
					//记录购买会员日志
					setlog(loginUser, req.getRemoteAddr(), "购买会员：一年");
				}
				break;
		}
		//购买vip成功
		if (isvip) {
			for (int a = 0; a < reviews.size(); a++) {
				//将该用户的评论的vip字段设置为1
				reviews.get(a).setVip(1);
			}
		}
		//更新数据库
		reviewBiz.updateByPrimaryKeySelective(reviews);
		//更新用户
		userBiz.updateByPrimaryKeySelective(loginUser);
		//更新session
		Map map = new HashMap<String, String>();
		map.put("username", loginUser.getUsername());
		map.put("password", loginUser.getPassword());
		session.setAttribute("loginUser", userBiz.selectLoginUser(map));

		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		//返回数据
		out.println(data);

	}
}
