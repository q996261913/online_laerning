package com.mooc.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.mooc.biz.*;
import com.mooc.entity.*;
import com.mooc.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.mooc.util.DateUtil;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AdminController {
    @Autowired
    UserBiz userBiz;
    @Autowired
    LogBiz logBiz;
    @Autowired
    CourseBiz courseBiz;
    @Autowired
    IpsetBiz ipsetBiz;
    @Autowired
    ReviewBiz reviewBiz;
    @Autowired
    MessageBiz messageBiz;
    //管理员日志记录
    public void setlog(User loginUser, String ip, String type, String adminname) {
        Log log = new Log();
        log.setUserid(loginUser.getId());
        log.setUsername(loginUser.getUsername());
        log.setIp(ip);
        log.setType(type);
        log.setExecutor(adminname);
        logBiz.insert(log);
    }

    @RequestMapping(value = "adminindex")
    public String adminindex(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "login";
        }
//		else if(!"admin".equals(loginUser.getMission())&&!"showadmin".equals(loginUser.getMission())){
//			//添加管理员的再次验证
//		session.setAttribute("loginUser", loginUser);
//		return "redirect:course";
//		}
        return "admin/adminindex";
    }


    @RequestMapping(value = "adminlogin")
    //管理员登录
    //非管理员/老师登录强制定位到前台页面
    //并记录日志
    public String adminlogin(User user, HttpSession session, HttpServletRequest req) {
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("username", user.getUsername());
        paramMap.put("password", user.getPassword());
        User loginUser = userBiz.selectLoginUser(paramMap);
        //如果用户未登录,返回后台登录页面
        if (loginUser == null) {
            return "loginadmin";
        } else if (!"admin".equals(loginUser.getMission()) && !"teacher".equals(loginUser.getMission())) {
            //如果不是管理员也不是老师,那么记录强制登录日志
            session.setAttribute("loginUser", loginUser);
            //记录日志
            Log log = new Log();
            log.setUserid(loginUser.getId());
            log.setUsername(loginUser.getUsername());
            log.setIp(req.getRemoteAddr());
            log.setType("用户尝试强制登录管理员页面");
            logBiz.insert(log);
            //强制返回前台登录页面
            return "redirect:login";
        } else {//如果满足权限
            session.setAttribute("loginUser", loginUser);
            setlog(loginUser, req.getRemoteAddr(), "登录", loginUser.getUsername());
            if (loginUser.getMission().equals("admin")) {//如果是管理员
                return "admin/leftmeun";
            } else {//如果是老师
                return "admin/teacherleftmeun";
            }


        }
    }

    @RequestMapping(value = "alluser")
	//展示所有的用户信息,分页查询每页用户
	//传递的page为当前页
    public ModelAndView alluser(ModelAndView mav, int page, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            mav.setViewName("loginadmin");
            return mav;
        } else if (!"admin".equals(loginUser.getMission()) && !"teacher".equals(loginUser.getMission())) {
            //如果不是管理员也不是老师,定位到前台登录
            mav.setViewName("redirect:login");
            return mav;
        } else {
        	//查询所有用户
            List<User> userss = userBiz.selectAllUser();
            int pageSize = 14;//一页的数量
            List<User> users = new ArrayList<User>();
            //告诉前台最大页面
            mav.addObject("maxpage", (userss.size() - 1) / pageSize);
            //分页查询当前页数据
            for (int i = page * pageSize; i < page * pageSize + pageSize; i++) {
                if (userss.size() == i) {
                	//如果课程数不够一页
                    mav.addObject("users", users);
                    mav.addObject("page", page);
                    //返回全部用户列表
                    mav.setViewName("admin/alluser");
                    return mav;
                }
                users.add(userss.get(i));
            }
            mav.addObject("page", page);
            mav.addObject("loginUser", loginUser);
            mav.addObject("users", users);
            mav.setViewName("admin/alluser");
            return mav;

        }
    }

    @RequestMapping(value = "banuser")
	//屏蔽和恢复用户接口
    //同时记录日志
    //type字段为0代表要屏蔽用户,1代表要恢复登录
    public String banuser(String userid, int type, HttpSession session, HttpServletRequest req) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "loginadmin";
        } else if (!"admin".equals(loginUser.getMission())) {
            //添加管理员的再次验证,如果不是管理员登录,返回到前台
            session.setAttribute("loginUser", loginUser);
            return "redirect:login";
        } else {
            if (type == 0) {//0为前台要屏蔽登录
                //查询该用户
                User user = userBiz.selectByPrimaryKey(userid);
                //设置屏蔽字段为1
                user.setBuycase("1");
                //更新用户到数据库
                userBiz.updateByPrimaryKeySelective(user);
                //记录日志
                setlog(user, req.getRemoteAddr(), "屏蔽用户登录", loginUser.getUsername());
                return "redirect:alluser?page=0";
            }
            if (type == 1) {//1代表前台要恢复用户登录
                //查询该用户
                User user = userBiz.selectByPrimaryKey(userid);
                //设置屏蔽字段为0
                user.setBuycase("0");
                //查询该用户
                userBiz.updateByPrimaryKeySelective(user);
                //记录日志
                setlog(user, req.getRemoteAddr(), "恢复用户登录", loginUser.getUsername());
                return "redirect:alluser?page=0";
            }

        }
        //重新回到首页
        return "redirect:alluser?page=0";
    }

    @RequestMapping(value = "rechargeindex")
    //重定向到recharge.jsp页面
    public String rechargeindex(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "login";
        } else if (!"admin".equals(loginUser.getMission())) {
            //添加管理员的再次验证
            return "redirect:loginadmin";
        }
        return "admin/recharge";
    }

    @RequestMapping(value = "recharge")
    //充值余额,传递用户id,充值金额,充值密码
    //未查询到用户则输出信息
    // 支付密码不正确返回0,充值成功,更新数据库,记录日志,返回成功信息
    public void recharge(String userid, int collect, String paypassword, HttpServletRequest req,
                         HttpSession session, HttpServletResponse resp) throws IOException {
        User loginUser = (User) session.getAttribute("loginUser");
        resp.setCharacterEncoding("utf-8");
        PrintWriter pw = resp.getWriter();
        //根据用户id查询用户
        User user = userBiz.selectByPrimaryKey(userid);

        if (user == null) {
            pw.print("用户ID不存在！请核实后再充值");
        } else if (!paypassword.equals("123456")) {
            pw.print("0");
        } else {
            user.setCollect(user.getCollect() + collect);
            userBiz.updateByPrimaryKeySelective(user);
            setlog(user, req.getRemoteAddr(), "充值" + collect + "元", loginUser.getUsername());
            pw.print("账户" + userid + ",充值" + collect + "元成功，余额：" + user.getCollect());
        }
    }

    @RequestMapping(value = "newuser")
    //新建用户界面，用户详情界面
    //带着用户id则说明是查看某个用户详情
    //否则是新建用户,需要移除一个user属性和loginuser不一样_
    public String newuser(String userid, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "login";
        } else if (!"admin".equals(loginUser.getMission())) {
            //如果不是管理员定位到后台登录页面
            return "redirect:loginadmin";
        }
        if (userid != null) {
            //参数过滤,如果传递了用户id,则说明要查看用户详情
            User user = userBiz.selectByPrimaryKey(userid);
            //放入session,跳转到newuser.jsp页面
            session.setAttribute("user", user);
            return "admin/newuser";
        } else {
            //否则说明只是要新建用户
            session.removeAttribute("user");
            return "admin/newuser";
        }
    }

    @RequestMapping(value = "newadduser")
    //后台新建账户,利用DateUtil自动生成id,然后重定向刷新newuser.jsp页面b
    //记录新增用户日志
    public String newadduser(User newuser, HttpSession session, HttpServletRequest req) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "login";
        } else if (!"admin".equals(loginUser.getMission())) {
            //添加管理员的再次验证
            return "redirect:loginadmin";
        }
        //自动生成id
        newuser.setId(DateUtil.getId());
        userBiz.insertSelective(newuser);
        setlog(newuser, req.getRemoteAddr(), "创建用户", loginUser.getUsername());
        //可能会再次新增用户,所以请求
        return "redirect:newuser";
    }

    @RequestMapping(value = "setuser")
    //修改用户,并记录日志
    public String setuser(User user, HttpSession session, HttpServletRequest req) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "login";
        } else if (!"admin".equals(loginUser.getMission())) {
            //添加管理员的再次验证
            return "redirect:login";
        }
        user.setCollect(userBiz.selectByPrimaryKey(user.getId()).getCollect());
        userBiz.updateByPrimaryKeySelective(user);
        setlog(user, req.getRemoteAddr(), "修改用户信息", loginUser.getUsername());
        return "redirect:alluser?page=0";
    }

    @RequestMapping(value = "removeuser")
    //删除用户接口,传递用户id,删除密码
    //默认删除密码为123456,如果删除密码错误则返回0
    //删除成功则记录日志,同时返回前台data数据,并重新更新session的users列表
    //修改密码为123456
    public void removeuser(String userid, String removepassword, HttpSession session, HttpServletRequest req,
                           HttpServletResponse resp) throws IOException {
        User loginUser = (User) session.getAttribute("loginUser");
        resp.setCharacterEncoding("utf-8");
        PrintWriter pw = resp.getWriter();
        if (!removepassword.equals("123456")) {
            pw.print("0");
        } else {
            User user = userBiz.selectByPrimaryKey(userid);
            userBiz.deleteByPrimaryKey(userid);
            setlog(user, req.getRemoteAddr(), "删除用户", loginUser.getUsername());
            pw.print("账户：" + userid + ",删除成功");
            List<User> users = userBiz.selectAllUser();
            session.setAttribute("users", users);
        }
    }

    @RequestMapping(value = "showlog")
    //日志查询
    //若传递type则为管理员日志,同时放入session一个type
    //若是普通日志,移除type控制前台输出
    public String showlog(String seachusername, String type, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "loginadmin";
        } else if (!"admin".equals(loginUser.getMission())) {
            //添加管理员的再次验证,如果不是管理员登录,返回前台登录页面
            return "redirect:login";
        }
        List<Log> logs;
        //普通日志,带有搜索参数,根据搜索参数查询
        if (seachusername != null && type == null) {
            //根据用户名搜索日志
            logs = logBiz.selectbyusername(seachusername);
            //放入logs属性
            session.setAttribute("logss", logs);
            //移除type,控制前台输出
            session.removeAttribute("type");
            //传递第一页的logs
            session.setAttribute("logs", initlogpage(logs));
            //传递最大页码数
            session.setAttribute("maxpage", (logs.size() - 1) / 15);//15为每页个数
            //传递当前页为第一页
            session.setAttribute("page", 0);
            return "admin/log";
        }
        //如果没有搜索参数,且是查询管理员日志
        if (type != null && seachusername == null) {
            //查询管理员日志
            logs = logBiz.selectadminlog();
            session.setAttribute("type", "admin");
            //放入全部日志
            session.setAttribute("logss", logs);
            //放入第一页日志
            session.setAttribute("logs", initlogpage(logs));
            //放入最大页码
            session.setAttribute("maxpage", (logs.size() - 1) / 15);
            //放入当前页
            session.setAttribute("page", 0);
            return "admin/log";
        }
        //如果是查询普通日志,不带搜索参数
        if (type == null) {
            //查询普通日志
            logs = logBiz.select();
            session.removeAttribute("type");
            //放入所有日志
            session.setAttribute("logss", logs);
            session.setAttribute("logs", initlogpage(logs));
            session.setAttribute("maxpage", (logs.size() - 1) / 15);
            session.setAttribute("page", 0);
            return "admin/log";
        }
        //查询管理员日志(根据搜索的用户名)
        else {
            logs = logBiz.selectadminlogbyusername(seachusername);
            session.removeAttribute("type");
            session.setAttribute("logss", logs);
            session.setAttribute("logs", initlogpage(logs));
            session.setAttribute("maxpage", (logs.size() - 1) / 15);
            session.setAttribute("page", 0);
            return "admin/log";
        }
    }

    //将logss的日志进行过滤,返回第一页的数据
    public List<Log> initlogpage(List<Log> logss) {
        int pageSize = 15;//一页的数量
        List<Log> logs = new ArrayList<Log>();
        //查询一页数据log数据
        for (int i = 0; i < pageSize; i++) {
            //如果不足一页
            if (logss.size() == i)
                return logs;
            logs.add(logss.get(i));
        }
        return logs;
    }

    @RequestMapping(value = "logpage")
    //翻页,查询当前页的数据返回前台
    public String logpage(int page, HttpSession session) {
        //从session中获取全部日志
        List<Log> logss = (List<Log>) session.getAttribute("logss");
        //当前页
        int rpage = (int) session.getAttribute("page");
        int pageSize = 15;//一页的数量
        List<Log> logs = new ArrayList<Log>();
        //根据当前页,查询一页的数据
        for (int i = page * pageSize; i < page * pageSize + pageSize; i++) {
            if (logss.size() == i) {
                session.setAttribute("logs", logs);
                session.setAttribute("page", page);
                return "admin/log";
            }
            logs.add(logss.get(i));
        }
        //放入当前页logs,和全部日志logss
        session.setAttribute("logs", logs);
        session.setAttribute("page", page);
        return "admin/log";
    }

    @RequestMapping(value = "allcourse")
    //根据登录用户权限,查询课程,管理员查询全部课程
    //教师查询自己的课程,返回allcourse.jsp
    public String allcourseindex(int page, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        List<Course> coursess;
        if (loginUser == null) {
            return "loginadmin";
        }else if(!loginUser.getMission().equals("admin")&&!loginUser.getMission().equals("teacher")){
            return "loginadmin";
        }
        //如果登录的是管理员,没有课程限制
        if(loginUser.getMission().equals("admin")){
            coursess = courseBiz.selectAllCourse();
        }
        else
         coursess = courseBiz.selectTeacherCourse(loginUser.getUsername());
        int pageSize = 14;//一页的数量
        List<Course> courses = new ArrayList<Course>();
        //查询一页课程
        session.setAttribute("maxpage", (coursess.size() - 1) / pageSize);
        for (int i = page * pageSize; i < page * pageSize + pageSize; i++) {
            if (coursess.size() == i) {
                session.setAttribute("courses", courses);
                session.setAttribute("page", page);
                return "admin/allcourse";
            }
            courses.add(coursess.get(i));
        }
        session.setAttribute("page", page);
        session.setAttribute("courses", courses);
        return "admin/allcourse";
    }

    @RequestMapping(value = "bancourse")
    //上下架课程,传递课程id,和操作类别
    //type=1下架课程
    //type=0上架课程
    //记录上架下架日志
    public String bancourse(int type, int courseid, HttpSession session, HttpServletRequest req) {
        User loginUser = (User) session.getAttribute("loginUser");
        int page = (int) session.getAttribute("page");
        if (loginUser == null) {
            return "login";
        }
        //查询当前课程
        Course course = courseBiz.selectByPrimaryKey(courseid);
        //记录日志
        Log log = new Log();
        log.setId(courseid);
        log.setExecutor(loginUser.getUsername());
        log.setIp(req.getRemoteAddr());
        if (type == 1) {//下架课程
            course.setPrice("1");
            log.setType("下架课程：" + course.getName());
        }
        if (type == 0) {//上架课程
            course.setPrice("0");
            log.setType("上架课程：" + course.getName());
        }
        //插入日志
        logBiz.insert(log);
        //更新课程
        courseBiz.updateByPrimaryKeySelective(course);
        return "redirect:allcourse?page=" + page;
    }


    @RequestMapping(value = "deletecourse")
    //管理员后台删除课程,并记录日志
    //没有实现,同时清除文件
    //同时删除课程的评论
    public void deletecourse(int courseid, String removepassword, HttpSession session, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User loginUser = (User) session.getAttribute("loginUser");
        resp.setCharacterEncoding("utf-8");
        PrintWriter pw = resp.getWriter();
        if (!removepassword.equals("123456")) {
            pw.print("0");//删除密码错误
        } else {
            //删除数据库的课程表记录
            Course course = courseBiz.selectByPrimaryKey(courseid);
            courseBiz.deleteByPrimaryKey(String.valueOf(courseid));
            //记录删除日志
            Log log = new Log();
            log.setId(courseid);
            log.setExecutor(loginUser.getUsername());
            log.setIp(req.getRemoteAddr());
            log.setType("删除课程：" + course.getName());
            logBiz.insert(log);
            //删除上架信息
           messageBiz.deleteByUserId(loginUser.getId());
            //删除课程文件
            FileUtils.removeFile(req,courseid);
            pw.print("课程：" + course.getName() + ",删除成功！请刷新页面后操作");
        }
    }

    @RequestMapping(value = "allip")
    //显示所有ip信息,传递当前页码
    //查询当前页的数据
    public String allip(int page, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "login";
        }
        //如果不是管理员登录
        else if (!"admin".equals(loginUser.getMission())) {
            //添加管理员的再次验证,如果不是管理员,跳转到前台页面
            session.setAttribute("loginUser", loginUser);
            return "login";
        } else {
            //查询所有ip信息
            List<Ipset> ipss = ipsetBiz.select();
            int pageSize = 14;//一页的数量
            List<Ipset> ips = new ArrayList<Ipset>();
            //存入最页码数
            session.setAttribute("maxpage", (ipss.size() - 1) / pageSize);
            for (int i = page * pageSize; i < page * pageSize + pageSize; i++) {
                if (ipss.size() == i) {
                    //放入所有ip信息
                    session.setAttribute("ips", ips);
                    //放入当前页码
                    session.setAttribute("page", page);
                    return "admin/allip";
                }
                ips.add(ipss.get(i));
            }
            session.setAttribute("page", page);
            session.setAttribute("ips", ips);
            return "admin/allip";
        }
    }

    @RequestMapping(value = "ipset")
    //传递ip信息和onband
    //onband非空代表是解除ip封锁,设置type为1直接返回allip页面
    //如果onband为空,则代表要封锁ip,跳转到setip页面
    public String ipset(HttpSession session, String ip, String onbaned) {
        if (onbaned != null) {
            Ipset ip1 = ipsetBiz.selectip(ip);
            //设置type为0
            ip1.setType("0");
            ip1.setBantime(null);
            //更新ip表
            ipsetBiz.updateByPrimaryKeySelective(ip1);
            return "redirect:allip?page=0";
        }
        //否则代表要前台需要封锁ip,跳转到ipset.jsp页面
        session.setAttribute("ip", ipsetBiz.selectip(ip));
        return "admin/ipset";
    }

    @RequestMapping(value = "banip")
    //封禁ip,根据传递的ip查询,看是否存在
    //如果不存在,调用insert
    //否则调用update
    //根据前台传递的time参数,为ip设置bantime
    public void banip(HttpServletResponse resp, HttpSession session, String ip, String mark, String time) throws IOException {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ;
        } else if (!"admin".equals(loginUser.getMission())) {
            //添加管理员的再次验证
            return;
        }
        Date date = new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-mm-dd");
        //根据ip查询ipset数据
        Ipset ip1 = ipsetBiz.selectip(ip);
        boolean isnull = false;
        //如果没有查到,说明是要新插入一条ip信息
        if (ip1 == null) {
            ip1 = new Ipset();
            ip1.setIp(ip);
            isnull = true;
        }
        //如果查询到了,则说明数据有ip,需要修改信息
        ip1.setIp(ip);
        ip1.setMark(mark);
        ip1.setType("1");
        //根据前台的time设置封禁时间
        switch (time) {
            case "5m":
                //如果当前分钟数大于55,小时数+1,分钟数减55
                if (date.getMinutes() > 55) {
                    date.setMinutes(date.getMinutes() - 55);
                    date.setHours(date.getHours() + 1);
                } else {
                    //否则直接加5分钟
                    date.setMinutes(date.getMinutes() + 5);
                }
                ip1.setBantime(date);
                break;
            case "2h":
                date.setHours(date.getHours() + 2);
                ip1.setBantime(date);
                break;
            case "1d":
                date.setDate(date.getDate() + 1);
                ip1.setBantime(date);
                break;
            case "1m":
                date.setMonth(date.getMonth() + 1);
                ip1.setBantime(date);
                break;
            case "1y":
                date.setYear(date.getYear() + 1);
                ip1.setBantime(date);
                break;
            case "ever":
                date.setYear(date.getYear() + 99);
                ip1.setBantime(date);
                break;
        }
        if (isnull) {
            //如果未查询到,说明是要插入新ip记录
            ipsetBiz.insert(ip1);
        } else {
            //否则调用更新
            ipsetBiz.updateByPrimaryKeySelective(ip1);
        }
        String format = sdf.format(date);
        resp.setCharacterEncoding("utf-8");
        resp.getWriter().write("封禁成功！封禁至：" + format);
    }

    @RequestMapping(value = "logoutadmin")
    //管理员注销,并记录日志
    public String logoutadmin(HttpSession session, HttpServletRequest req) {
        User loginUser = (User) session.getAttribute("loginUser");
        session.invalidate();
        setlog(loginUser, req.getRemoteAddr(), "注销", loginUser.getUsername());
        return "loginadmin";
    }

    @RequestMapping(value = "coursede")
    //课程详情页面,传递课程id
    public String coursede(String courseid, HttpSession session) {
        session.removeAttribute("msg");
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "login";
        }
        if (courseid != null) {
            //如果课程id不为空,说明是单个课程详情页面,放入课程给前端
            Course course = courseBiz.selectByPrimaryKey(Integer.parseInt(courseid));
            session.setAttribute("course", course);
            return "admin/course";
        }
        session.removeAttribute("course");
        //返回course详情页面
        return "admin/course";

    }


    @RequestMapping(value = "save")
    //课程上传,
    public String save(String name,String context,String type,@RequestParam("ogg") MultipartFile ogg,@RequestParam("jpg") MultipartFile jpg,HttpSession session,HttpServletRequest request) {
        session.removeAttribute("msg");
        if(ogg==null){
            session.setAttribute("msg", "请上传视频文件");
            return "admin/course";
        }
        else if(jpg==null){
            session.setAttribute("msg", "请上传课程图片");
            return "admin/course";
        }
        User loginUser = (User) session.getAttribute("loginUser");
        int maxid = courseBiz.savecourse(name, type, context, loginUser.getUsername());
        boolean isFileUpload = FileUtils.uploadFile(ogg, jpg, request, maxid);
        if(isFileUpload)
        session.setAttribute("msg", "操作成功");
        else {
            session.setAttribute("msg", "上传文件失败!");
        }
        return "admin/course";
    }


}
