package org.spat.scf.maven;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * @goal deploy
 */
public class DeployMojo extends AbstractSCFMojo {

	public void execute() throws MojoExecutionException {
		Deployer deployer = new Deployer();
		deployer.deploy(this);
	}
}
