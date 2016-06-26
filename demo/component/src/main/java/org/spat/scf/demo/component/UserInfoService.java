package org.spat.scf.demo.component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.spat.scf.demo.contract.IUserInfoService;
import org.spat.scf.demo.entity.UserInfo;
import org.spat.scf.protocol.annotation.ServiceBehavior;

@ServiceBehavior
public class UserInfoService implements IUserInfoService{

	@Override
	public void insertUser(UserInfo user) throws Exception {
		System.out.println("insertUser: userid="+user.getUserId()+" userName="+user.getUserName()+" password="+user.getPassword()+" birthday="+user.getBirthday()+" age="+ user.getAge());
	}

	
	@Override
	public UserInfo getUserByUserId(long userId) throws Exception {
		UserInfo user=new UserInfo();
		user.setAge(19);
		user.setBirthday(new Date());
		user.setPassword("ddss");
		user.setSex(false);
		user.setUserId(12334L);
		user.setUserName("peida3");
		return user;
	}

	@Override
	public List<UserInfo> getUserList(int cityId) throws Exception {
		List<UserInfo> userList=new ArrayList<UserInfo>();
		for(int i=0;i<10;i++){
			UserInfo user=new UserInfo();
			user.setAge(19+i);
			user.setBirthday(new Date());
			user.setPassword("ddss");
			user.setSex(false);
			user.setUserId(12334L);
			user.setUserName("peida"+i+"_dd");
			userList.add(user);
		}
		return userList;
	}

}
