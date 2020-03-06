package com.mooc.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Review {
    private String reviewid;
    private String context;
    private String username;
    private int courseid;
    private Date time;
    private int lable;
    private String sex;
    private int vip;

}
