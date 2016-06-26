package org.spat.scf.server.deploy.filemonitor;

/**
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public interface IListener {
	
	void fileChanged(FileInfo fInfo);
	
}