package com.mooc.mapper;

import java.util.List;

import com.mooc.entity.Course;
//Mapper接口
public interface CourseMapper {
    //搜索课程,传递搜索参数
	public List<Course> coursesearch(String scarch);
	//查询所有课程
	public List<Course> selectAllCourse();
	//查询对应老师的课程,传递教师id
	public List<Course> selectTeacherCourse(String id);
	//查询免费课程(type=0)
	public List<Course> freecourse();
    //查询收费课程(type=1)
	public List<Course> vipcourse();
	//查询最后一个课程,为了查询最大的id值
	Course selectlastcourse();
	//根据主键删除课程
    int deleteByPrimaryKey(String id);
    //添加一个课程
    int insert(Course record);
    //根据添加的字段插入课程
    int insertSelective(Course record);
    //根据主键查询唯一课程
    Course selectByPrimaryKey(int id);
    //根据主键对应的课程更新相应的字段
    int updateByPrimaryKeySelective(Course record);
    //根据主键更新相应课程
    int updateByPrimaryKey(Course record);
}