package org.spat.scf.client.proxy.component;

public class CallBackHelper {
	
	public static class Cb{
		public static CallBackExecutor callbackexecutor = new CallBackExecutor();
	}
	
	public static CallBackExecutor getInstance(){
		return Cb.callbackexecutor;
	}
}
