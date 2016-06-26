package org.spat.scf.server.proxy;

import org.spat.scf.server.contract.context.SCFContext;

/**
 * a interface for description InvokerHandle
 * such as: AsyncInvokerHandle, AsyncInvokerHandle
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public interface IInvokerHandle {
	
	public void invoke(SCFContext context) throws Exception;
	
}