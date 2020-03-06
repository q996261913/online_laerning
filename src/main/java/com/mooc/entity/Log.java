package com.mooc.entity;

import lombok.Data;

import java.util.Date;


@Data
public class Log {
	private int id;
	private String userid;
	private String username;
	private String type;
	private Date time;
	private String ip;
	private String executor;

   
}
