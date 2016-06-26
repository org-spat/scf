package org.spat.scf.server.contract.context;

public class ApproveContext {
	
	private boolean isRight = false;
	
	private String data;
	
	public ApproveContext() {
		this.data = Global.getSingleton().getServiceConfig().getString("scf.server.approve.securedata");
	}

	public boolean isRight() {
		return isRight;
	}

	public void setRight(boolean isRight) {
		this.isRight = isRight;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
	
}
