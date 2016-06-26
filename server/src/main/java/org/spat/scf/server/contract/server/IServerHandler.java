package org.spat.scf.server.contract.server;

import org.spat.scf.server.contract.context.SCFContext;

public interface IServerHandler {

	public void writeResponse(SCFContext context);

}