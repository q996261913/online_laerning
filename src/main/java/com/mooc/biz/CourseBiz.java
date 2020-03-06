package com.mooc.biz;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.mooc.entity.Course;

//Service层接口
public interface CourseBiz {
    //查询所有课程
	public List<Course> selectAllCourse();
	//根据教师id查询对应的课程
    public List<Course> selectTeacherCourse(String id);
    //根据主键查询所有课程
	Course selectByPrimaryKey(int id);
	//根据主键更新课程
    int updateByPrimaryKeySelective(Course record);
    //搜索课程接口
    List<Course> coursesearch(String search);
    //查询免费课程
    List<Course> freeCourse();
    //查询收费课程
    List<Course> vipCourse();
    //根据主键删除课程
    int deleteByPrimaryKey(String id);
    //根据请求上传课程,只做插入数据库操作
    int savecourse(String courseName,String type,String context,String teacherId);
}
