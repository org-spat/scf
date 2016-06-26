
package org.spat.scf.server.deploy.hotdeploy;

import org.spat.scf.server.proxy.IInvokerHandle;

/**
 * a interface for description hot deploy class
 * 
 * @author Service Platform Architecture Team 
 * 

 * 
 */
public interface IHotDeploy {
	
	public void setSyncInvokerHandle(IInvokerHandle handle);
	
	public void setAsyncInvokerHandle(IInvokerHandle handle);
	
}