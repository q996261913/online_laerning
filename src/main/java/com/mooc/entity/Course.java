package com.mooc.entity;

import lombok.Data;

@Data
public class Course {
    private int id;

    private String name;

    private String context;

    private String type;

    private String price;

    private String label;

    private  String teacherid;

    private  String hour;

}