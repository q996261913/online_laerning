##course表
1. type为1 代表会员课程,0代表免费课程
2. price字段为0代表已经上架,price字段为1未上架
## message表
1. 存储的用户订阅课程的基本信息包含字段用户id和课程id
## log表
1. type字段代表用户做的操作,中文记录
2. executor:是否是管理员执行操作
3. 如果非管理员则是普通用户的操作记录
## ip表
1. type为1代表已经封禁,0代表未封禁
2. firsttime代表第一次访问时间,now()函数插入
3. totime代表最后访问时间
4. bantime代表封禁结束时间
## review表
1. vip字段看是否是课程,1为vip,0非vip
2. label字段分为1/2/3/4四个级别
3. time字段通过now()函数插入
## user表
1. buycase字段:1代表被屏蔽,0代表非屏蔽
2. firsttime代表注册时间,通过执行insert的now函数插入

