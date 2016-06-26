package org.spat.scf.serializer.test.serializer;

import org.spat.scf.serializer.annotation.SCFMember;
import org.spat.scf.serializer.annotation.SCFSerializable;

@SCFSerializable(name="Serializer.People")
public class People {
	
	@SCFMember(sortId=1)
	private String name;
	@SCFMember(sortId=2)
	private char sex;
	@SCFMember(sortId=3)
	private int age;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public char getSex() {
		return sex;
	}
	public void setSex(char sex) {
		this.sex = sex;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	@Override
	public String toString() {
		return "People [name=" + name + ", sex=" + sex + ", age=" + age + "]";
	}
	
}
