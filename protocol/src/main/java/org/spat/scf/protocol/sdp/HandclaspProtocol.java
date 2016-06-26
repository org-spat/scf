package org.spat.scf.protocol.sdp;

import java.io.Serializable;

import org.spat.scf.serializer.annotation.SCFMember;
import org.spat.scf.serializer.annotation.SCFSerializable;


/**
 * RequestProtocol
 *
 * @author Service Platform Architecture Team 
 * 
 */
@SCFSerializable(name="HandclaspProtocol")
public class HandclaspProtocol extends SDP implements Serializable{
	private static final long serialVersionUID = -9058373768755100878L;
	
	/**
	 * 权限认证类型(1、客户端发送公钥至服务器 2、客户端发送授权文件密文至服务器)
	 */
	@SCFMember(sortId=1)
	private String type;
	/**
	 * 信息内容
	 */
	@SCFMember(sortId=2)
	private String data; 
	
	public HandclaspProtocol(){
		
	}
	/**
	 * 实例化HandclaspProtocol
	 * @param type ("1"表示客户端发送公钥至服务器 "2"表示客户端发送授权文件密文至服务器)
	 * @param data (密文)
	 */
	public HandclaspProtocol(String type,String data){
		this.type = type;
		this.data = data;
	}

	
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
