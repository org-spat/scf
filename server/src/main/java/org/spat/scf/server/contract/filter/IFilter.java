package org.spat.scf.server.contract.filter;

import org.spat.scf.server.contract.context.SCFContext;

public interface IFilter {
	
	/**
	 * 获得优先级
	 * @return
	 */
	public int getPriority();
	
	/**
	 * 过虑
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public void filter(SCFContext context) throws Exception;
	
}
