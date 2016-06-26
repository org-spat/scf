package org.spat.scf.server.contract.context;

/**
 * a interface for description ProxyFactory
 * every service contain only one ProxyFactory for create ProxyStub
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public interface IProxyFactory {
	public IProxyStub getProxy(String lookup);// throws ProtocolException;
}