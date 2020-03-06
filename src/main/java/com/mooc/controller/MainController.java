package com.mooc.controller;

import com.mooc.biz.*;
import com.mooc.entity.Course;
import com.mooc.entity.Log;
import com.mooc.entity.Review;
import com.mooc.entity.User;
import com.mooc.util.DateUtil;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.utils.CaptchaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Controller
//主Controller,做验证码控制,做
public class MainController {
	//注入所有Service层接口
	@Autowired
	UserBiz userBiz;
	@Autowired
	CourseBiz courseBiz;
	@Autowired
	ReviewBiz reviewBiz;
	@Autowired
	MessageBiz messageBiz;
	@Autowired
	LogBiz logBiz;
	//记录日志方法
	public void setlog(User loginUser, String ip, String type) {
		Log log = new Log();
		log.setUserid(loginUser.getId());
		log.setUsername(loginUser.getUsername());
		log.setIp(ip);
		log.setType(type);
		logBiz.insert(log);
	}
	//校验验证码,不正确返回0
	@RequestMapping(value = "varcodecheck")//验证码验证
	public void varcodecheck(String varcode,HttpServletRequest req,HttpServletResponse res) throws IOException {
		res.setCharacterEncoding("utf-8");
		PrintWriter pw = res.getWriter();
		/*String var = (String) session.getAttribute("varcodenumber");*/
		if(!CaptchaUtil.ver(varcode, req)){
			pw.write("0");//返回字符串0,代表验证未通过
		}
	}

	//改变验证码
	@RequestMapping(value = "changevarcode")//更换验证码，验证码显示
	public void changevarcode(HttpServletRequest req,HttpServletResponse res) throws IOException, FontFormatException {

        // 使用gif验证码
        GifCaptcha gifCaptcha = new GifCaptcha(130,48,4);
        gifCaptcha.setFont(gifCaptcha.FONT_7);
		CaptchaUtil.out(gifCaptcha,req, res);
	}

	//返回管理员入口
	@RequestMapping(value = "admin")//管理员登录入口,返回管理员登录页面
	public String admin(HttpSession session) {
		return "loginadmin";
	}
	
	

	//访问首页,入口
	//查询免费课程
	//查询收费课程
	//index页面分别显示3个
	@RequestMapping(value = {"index",""})
	public ModelAndView index(ModelAndView mav) {
		//查询免费课程
		List<Course> freecourses = courseBiz.freeCourse();
		//查询收费课程
		List<Course> vipcourses = courseBiz.vipCourse();
		//放入ModelAndView
		mav.addObject("freecourses", freecourses);
		mav.addObject("vipcourses", vipcourses);
		mav.setViewName("index");//返回index.jsp页面
		return mav;
	}

	@RequestMapping(value = "subreview")
	// 提交评论
	//根据vip字段判断
	public String subreview(HttpSession session, Review review,HttpServletRequest req) {
		User loginUser = (User) session.getAttribute("loginUser");
		if (loginUser == null) {
			return "login";
		}
		int vip ;
		if(loginUser.getVip()!=null){
			vip=1;
		}else{
			vip=0;
		}
		Course course = new Course();
		course.setId(review.getCourseid());
		//平均评价
		course.setLabel(reviewBiz.avglable(review.getCourseid()));
		//提交
		courseBiz.updateByPrimaryKeySelective(course);
		review.setVip(vip);
		review.setSex(loginUser.getSex());
		review.setReviewid(DateUtil.getId());
		review.setUsername(loginUser.getUsername());
		reviewBiz.insert(review);
		setlog(loginUser, req.getRemoteAddr(), "发表评论，在'"+courseBiz.selectByPrimaryKey(review.getCourseid()).getName() +"'");
		//带着课程id冲顶访问courseVideo接口,刷新页面
		return "redirect:coursevideo?courseid=" + review.getCourseid();

	}

	@RequestMapping(value = "review")
	// 查看评论
	public ModelAndView review(ModelAndView mav, int courseid) {
		List<Review> reviews = reviewBiz.select(courseid);
		mav.addObject("reviews", reviews);
		mav.setViewName("redirect:coursevideo");
		return mav;
	}

	@RequestMapping(value = "coursesearch")
	// 搜索课程
	public String coursesearch(String search, Map map) {
		//根据参数查询符合条件的课程
		List<Course> courses = courseBiz.coursesearch(search);
		//移除map中符合条件的课程
		map.remove(courses);
		//放入符合的课程
		map.put("courses", courses);
		//放入搜索参数
		map.put("search", search);
		//返回搜索结果页面
		return "courseindex";
	}
	
	@RequestMapping("/error/{errorcode}")
	public String error(@PathVariable int errorcode) {
		String pager = "404";
		switch (errorcode) {
        case 404:
            pager = "404";
            break;
        case 500:
            pager = "500";
            break;
    }
		return pager;
	}

   
  
}
