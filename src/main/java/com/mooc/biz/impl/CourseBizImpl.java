package com.mooc.biz.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.mooc.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mooc.biz.CourseBiz;
import com.mooc.entity.Course;
import com.mooc.mapper.CourseMapper;


//service层,实现各种Biz接口
@Service(value = "CourseBiz")
public class CourseBizImpl implements CourseBiz {
    //自动装配Dao层Mapper接口
    @Autowired
    CourseMapper courseMapper;

    @Override
    public List<Course> selectAllCourse() {
        // TODO Auto-generated method stub
        return courseMapper.selectAllCourse();
    }


    @Override
    public List<Course> selectTeacherCourse(String id) {
        // TODO Auto-generated method stub
        return courseMapper.selectTeacherCourse(id);
    }

    @Override
    public Course selectByPrimaryKey(int id) {
        // TODO Auto-generated method stub
        return courseMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(Course record) {
        // TODO Auto-generated method stub
        return courseMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<Course> coursesearch(String search) {
        // TODO Auto-generated method stub
        return courseMapper.coursesearch(search);
    }

    @Override
    public List<Course> freeCourse() {
        // TODO Auto-generated method stub
        return courseMapper.freecourse();
    }

    @Override
    public List<Course> vipCourse() {
        // TODO Auto-generated method stub
        return courseMapper.vipcourse();
    }

    @Override
    public int deleteByPrimaryKey(String id) {
        // TODO Auto-generated method stub
        return courseMapper.deleteByPrimaryKey(id);
    }



    @Override
    //像数据库增添一条记录,并返回增添后的课程id
    public int savecourse(String courseName, String type, String context,String teacherId) {
        Course lastCourse = courseMapper.selectlastcourse();
        Course course=new Course();
        course.setName(courseName);
        course.setType(type);
        course.setContext(context);
        course.setTeacherid(teacherId);
        //未上架
        course.setPrice("1");
        int maxid;
        if(lastCourse==null){
            courseMapper.insertSelective(course);
           maxid = courseMapper.selectlastcourse().getId();
        }
        else{
            courseMapper.insertSelective(course);
            maxid= courseMapper.selectlastcourse().getId();
        }
        System.out.println(maxid);
        return maxid;

    }
}
