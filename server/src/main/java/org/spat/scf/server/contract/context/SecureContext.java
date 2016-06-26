package org.spat.scf.server.contract.context;

public class SecureContext {
	/**
	 * DES密钥
	 */
	private String desKey;
	/**
	 * 服务器端RSA公钥
	 */
	private String serverPublicKey;
	/**
	 * 服务器端RSA私钥
	 */
	private String serverPrivateKey;
	/**
	 * 客户端RSA公钥
	 */
	private String clientPublicKey;
	/**
	 * 客户端RSA私钥
	 */
	private String clientPrivateKey;
	/**
	 * 是否通过认证
	 */
	private boolean isRights = false;

	public boolean isRights() {
		return isRights;
	}

	public void setRights(boolean isRights) {
		this.isRights = isRights;
	}

	public String getDesKey() {
		return desKey;
	}
	
	public void setDesKey(String desKey) {
		this.desKey = desKey;
	}
	
	public String getServerPublicKey() {
		return serverPublicKey;
	}
	
	public void setServerPublicKey(String serverPublicKey) {
		this.serverPublicKey = serverPublicKey;
	}
	
	public String getServerPrivateKey() {
		return serverPrivateKey;
	}
	
	public void setServerPrivateKey(String serverPrivateKey) {
		this.serverPrivateKey = serverPrivateKey;
	}
	
	public String getClientPublicKey() {
		return clientPublicKey;
	}
	
	public void setClientPublicKey(String clientPublicKey) {
		this.clientPublicKey = clientPublicKey;
	}
	
	public String getClientPrivateKey() {
		return clientPrivateKey;
	}
	
	public void setClientPrivateKey(String clientPrivateKey) {
		this.clientPrivateKey = clientPrivateKey;
	}
}
