package org.spat.scf.server.contract.context;

import org.spat.scf.server.utility.ServiceFrameException;

/**
 * a interface for description ProxyStub
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public interface IProxyStub {
	
	public SCFResponse invoke(SCFContext context)  throws ServiceFrameException;
}
