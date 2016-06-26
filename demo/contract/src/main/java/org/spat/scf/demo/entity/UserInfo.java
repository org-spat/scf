package org.spat.scf.demo.entity;

import java.io.Serializable;
import java.util.Date;

import org.spat.scf.serializer.annotation.SCFMember;
import org.spat.scf.serializer.annotation.SCFSerializable;

@SCFSerializable(name="org.spat.scf.demo.UserInfo")
public class UserInfo implements Serializable{
	


	/**
	 * 
	 */
	private static final long serialVersionUID = 1446545456788L;

	/**
	 * sortId 缂栧彿鍙兘澧炲姞锛屼笉鑳藉垹闄わ紝鏂板灞炴�鐨剆ortId浠�寮�锛屽湪瀹炰綋澶氱増鏈瓨鍦ㄧ殑鎯呭喌涓嬶紝搴忓垪鍖栦細鏈変笉鍏煎鐨勫紓甯稿彂鐢燂紝瀵艰嚧搴忓垪鍜屽弽搴忓垪澶辫触
	 */
	
	
	@SCFMember(sortId=1)
	private long userId;
	
	@SCFMember(sortId=2)
	private String userName;
	
	@SCFMember(sortId=3)
	private String password;
	
	@SCFMember(sortId=4)
	private int age;
	
	@SCFMember(sortId=5)
	private boolean sex;
	
	@SCFMember(sortId=6)
	private Date birthday;
	
	

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public boolean isSex() {
		return sex;
	}

	public void setSex(boolean sex) {
		this.sex = sex;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	
}
