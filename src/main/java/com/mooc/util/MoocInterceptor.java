package com.mooc.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.mooc.entity.Ipset;
import com.mooc.mapper.IpsetMapper;
/**
 * 过滤器，过滤封禁的IP
 *
 */
public class MoocInterceptor implements HandlerInterceptor{
	@Autowired
	IpsetMapper ipsetMapper;

	@Override
	//存在的ip地址,需要更新最后访问时间
	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("进入：afterCompletion()");
		Ipset ipset =ipsetMapper.selectip(arg0.getRemoteAddr());
		Date date =new Date();
		//更新最后访问时间
		ipset.setTotime(date);
		ipsetMapper.updateByPrimaryKeySelective(ipset);
	}

	//登录后触发这个方法,验证是否是新ip地址进入
	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3)
			throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("进入：postHandle()");
		//根据客户ip查询数据库有没有对应ip
		Ipset ipset =ipsetMapper.selectip(arg0.getRemoteAddr());
		//如果没有set说明是新用户
		if(ipset==null) {
			ipset = new Ipset();
			ipset.setIp(arg0.getRemoteAddr());
			ipsetMapper.insert(ipset);
		}
		
	}

	@Override
	//前处理方法,登录前,先要通过这个方法
	public boolean preHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("进入：preHandle()");
		//获取客户ip,查询一个ipset对象
		Ipset ipset =ipsetMapper.selectip(arg0.getRemoteAddr());
		//
		Date date = new Date();
		SimpleDateFormat sdf=new  SimpleDateFormat("yyyy年MM月d日 HH:mm:ss");
		if(ipset!=null&&ipset.getType()!=null&&ipset.getType().equals("1")) {
			arg1.setHeader("Content-Type", "text/html;charset=UTF-8");
			//向客户端输出ipset信息
			arg1.getWriter().write("<h1 style='text-align:center;color:red;padding-top:300px;'>您的IP已经被封禁！<br>封禁原因："+ipset.getMark()
						+"<br>封禁时间到:"+sdf.format(ipset.getBantime())+"</h1>");
				//看用户ip被禁用时间是否已经无效
				if(date.getTime()>ipset.getBantime().getTime()) {
						ipset.setType("0");
						ipsetMapper.updateByPrimaryKeySelective(ipset);
						return true;
			}
			return false;
		}
		return true;
	}

}
