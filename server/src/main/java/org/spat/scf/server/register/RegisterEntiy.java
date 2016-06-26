package org.spat.scf.server.register;

public class RegisterEntiy {
	private String serviceName;
	private int port;
	private int telnetport;
	private int state;
	
	public RegisterEntiy(String serviceName, int port, int telnetport, int state) {
		this.port = port;
		this.serviceName = serviceName;
		this.telnetport = telnetport;
		this.state = state;
	}
	
	public byte[] toBuffer() {
		StringBuffer sb = new StringBuffer();
		sb.append(serviceName).append("\t");
		sb.append(port).append("\t");
		sb.append(telnetport).append("\t");
		sb.append(state);
		return sb.toString().getBytes();
	}
	
}
