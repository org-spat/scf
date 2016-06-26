package org.spat.scf.client.utility;

public class ScfKeyLoad extends KeyLoad {

	private static class ScfKeyLoadHolder{
		public static ScfKeyLoad keyLoad = new ScfKeyLoad();
	}
	
	public static ScfKeyLoad getInstance(){
		return ScfKeyLoadHolder.keyLoad;
	}
	
	@Override
	public void analysis() {
		String context = this.getContext();
		/**
		 * 解析内容
		 */
	}
	
	public String getContext(){
		try {
			return super.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
