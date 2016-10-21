package com.sohu.sns.monitor.constant;

import java.util.HashMap;

public class StatusConstant {
	 
	public static int SUCCESS = 100000; 
	
	public static HashMap<Integer, String> MAP_STATUS = new HashMap<Integer, String>();
	 
	static {
		MAP_STATUS.put(StatusConstant.SUCCESS, "success");
		MAP_STATUS.put(200001, "System error");
		MAP_STATUS.put(200002, "Request api not found");
		MAP_STATUS.put(200003, "Api params is required");
		MAP_STATUS.put(200004, "At least one api parameters is required");
		MAP_STATUS.put(200005, "Http get is not supported at this api");
		MAP_STATUS.put(200006, "Http post is not supported at this api");

		MAP_STATUS.put(300001, "Parameter type must be integer");
		MAP_STATUS.put(300002, "Parameter type must be integer[]");
		MAP_STATUS.put(300003, "Parameter type must be string[]");
		MAP_STATUS.put(300004, "Parameter value must be greater than 0");
		MAP_STATUS.put(300005, "Database access error"); 
		MAP_STATUS.put(300006, "Send Mq message failed"); 
		MAP_STATUS.put(300007, "Parameter can't be space");
		MAP_STATUS.put(300008, "Search error");
		MAP_STATUS.put(300009, "Parameter error");
		MAP_STATUS.put(300010, "Upload file is too large(limit 10M)"); 
		MAP_STATUS.put(300011, "Parameter type must be date (yyyy-MM-dd HH:mm:ss.SSS)"); 

        MAP_STATUS.put(301001, "repost fail");
        MAP_STATUS.put(301002, "del repost fail");
        MAP_STATUS.put(301003, "source feed not exist");

		MAP_STATUS.put(302001, "User not exsist");
		MAP_STATUS.put(302002, "User sex tag parameter type is int (1 male, 0 female)");
		MAP_STATUS.put(302003, "User profile synchronize to passport failure");
		MAP_STATUS.put(302004, "User insert db failure ");
        MAP_STATUS.put(302010, "");
		 
		MAP_STATUS.put(303001, "Follow failure"); 
		MAP_STATUS.put(303002, "Show failure"); 
		MAP_STATUS.put(303003, "Get follow list failure"); 
		MAP_STATUS.put(303004, "User has been followed"); 
		MAP_STATUS.put(303005, "User is'nt followed"); 
		MAP_STATUS.put(303006, "Cann't follow one self"); 
		MAP_STATUS.put(303007, "Cann't unfollow one self"); 

		//passport错误码范围 304001 ~ 304099 
		MAP_STATUS.put(304001, "Passport status, system error");
		MAP_STATUS.put(304002, "Passport status, sig error");
		MAP_STATUS.put(304003, "Passport status, wrong params");
		MAP_STATUS.put(304004, "Passport status, account is exsist");
		MAP_STATUS.put(304005, "Passport status, mobile has bound with other account");
		MAP_STATUS.put(304006, "Passport status, msg send times more than limit");
		MAP_STATUS.put(304007, "Passport status, msg verify code auth than limit");
		MAP_STATUS.put(304008, "Passport status, msg verify code expired");
  
		MAP_STATUS.put(304009, "Passport status, code error");
		MAP_STATUS.put(304010, "Passport status, mobile never bound with any account");
		MAP_STATUS.put(304011, "Passport status, mobile couldn't unbind");

		MAP_STATUS.put(304012, "Passport status, userid or nickname is not exsist");
  		MAP_STATUS.put(304013, "Passport status, update userinfo failed");
		MAP_STATUS.put(304014, "Passport status, illegal uniqname"); 
		MAP_STATUS.put(304015, "Passport status, uniqname is exsist");
		MAP_STATUS.put(304016, "Passport status, interface request more than limit (1000 times per 5 minutes)");
		MAP_STATUS.put(304017, "Passport status, V plus user couldn't change uniqname");

		MAP_STATUS.put(304019, "Passport status, get userinfo failed");

 		MAP_STATUS.put(304020, "Passport status, wrong password");
  		MAP_STATUS.put(304021, "Passport status, get token failed");
  		 
		MAP_STATUS.put(304022, "Passport status, token unactived");
  		MAP_STATUS.put(304023, "Passport status, auth failed");
  		
 		MAP_STATUS.put(304024, "Passport status, wrong appid (1074 and 1106 only)");
   		MAP_STATUS.put(304025, "Passport status, unsupported open account (qq and sina only)");
		MAP_STATUS.put(304026, "Passport status, wrong gid");
 		MAP_STATUS.put(304027, "Passport status, wrong accesstoken (token)");
 		
  		MAP_STATUS.put(304028, "Passport status, uniqname contains limit words");  
  		
  		MAP_STATUS.put(304029, "Passport status, illegal userid"); 
  		MAP_STATUS.put(304030, "Passport status, check userid error");
        MAP_STATUS.put(304031, "Passport status, mobile un bind");
		MAP_STATUS.put(304099, "Connected to passport exception");
 
		//mysohu错误码范围 304101 ~ 304199 
 		MAP_STATUS.put(304101, "Mysohu status, unkown error");
 		MAP_STATUS.put(304102, "Mysohu status, get avatar error");
 		MAP_STATUS.put(304103, "Mysohu status, update avatar error");
 		MAP_STATUS.put(304104, "Mysohu status, upload avatar image file error");

        MAP_STATUS.put(304198, "sohu wei bo error");

		//QQ互联 错误码范围 304201 ~ 304299 
 		MAP_STATUS.put(304201, "QQ platform status, unknow error");
 		MAP_STATUS.put(304202, "QQ platform status, auth failure");

		//Sina weibo 错误码范围 304301 ~ 304399 
 		MAP_STATUS.put(304301, "Sina weibo platform status, unknow error");
 		MAP_STATUS.put(304302, "Sina weibo platform status, auth failure");
 		MAP_STATUS.put(304303, "Sina weibo platform status, get friend_ids (follow user_ids) failuer");
 		
 		//
 		MAP_STATUS.put(304401, "Image file is null");  
 		MAP_STATUS.put(305001, "Message is not exsist!");  

 		
 		MAP_STATUS.put(306001, "Not weibo user!");  
 		
 		MAP_STATUS.put(307001, "uname conflicted !");  
 		MAP_STATUS.put(307002, "no matched info with this passport_id !");  
 		/*
 		用户隐私安全消息
 		 */
        MAP_STATUS.put(308000, "cant repost be black");
        MAP_STATUS.put(308001, "cant repost in black");
        MAP_STATUS.put(308002, "cant follow in black");
        MAP_STATUS.put(308003, "cant repost not fl");
        MAP_STATUS.put(308004, "cant follow be black");

		//用户修改头像状态锁定
		MAP_STATUS.put(309001, "update avatar status is locked !");
		//用户修改简介状态锁定
		MAP_STATUS.put(309002, "update introduction status is locked !");
		//用户修改昵称桌台锁定
		MAP_STATUS.put(309003, "update nick status is locked !");
		//用户已被禁言
		MAP_STATUS.put(309004, "comment status is locked !");

 		//签名校验不正确
 		MAP_STATUS.put(400001, "sig check fail!");

		//token 验证未通过
		MAP_STATUS.put(400002,"appSessionToken is not right !");



	}
}
