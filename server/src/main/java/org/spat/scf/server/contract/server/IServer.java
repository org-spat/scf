package org.spat.scf.server.contract.server;


/**
 * a interface for description start/stop socket server
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public interface IServer {

	public void start() throws Exception;

	public void stop() throws Exception;
}