package com.mooc.entity;

import lombok.Data;

import java.util.Date;
@Data
public class Ipset {
private String ip;
private String type;
private String mark;
private Date firsttime;
private Date bantime;
private Date totime;

}
