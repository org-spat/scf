package org.spat.scf.client.proxy.component;

import org.spat.scf.client.loadbalance.Server;
import org.spat.scf.client.logger.ILog;
import org.spat.scf.client.logger.LogFactory;
import org.spat.scf.client.utility.AsyncDetectHelper;
import org.spat.scf.protocol.enumeration.SDPType;
import org.spat.scf.protocol.exception.ThrowErrorHelper;
import org.spat.scf.protocol.sdp.ExceptionProtocol;
import org.spat.scf.protocol.sdp.ResponseProtocol;
import org.spat.scf.protocol.sfp.Protocol;
/**
 * a abstract class for description callback funcation
 * 
 * @author Service Platform Architecture Team 
 */

public abstract class ReceiveHandler {
	static final ILog logger = LogFactory.getLogger(ReceiveHandler.class);
	final CallBackExecutor callBack = CallBackHelper.getInstance();
	
	private Server server;
	
	public void setServer(Server server) {
		this.server = server;
	}
	
	public void notify(final byte[] buffer)throws Exception {
		callBack.callBackExe.execute(new Runnable() {
			@Override
			public void run() {
				try{
					InvokeResult result = null;
					Protocol receiveP = Protocol.fromBytes(buffer);
					if (receiveP == null) {
						throw new Exception("userdatatype error!");
					}
					if (receiveP.getSDPType() == SDPType.Response) {
						ResponseProtocol rp = (ResponseProtocol)receiveP.getSdpEntity();
						result = new InvokeResult(rp.getResult(), rp.getOutpara());
					}else if (receiveP.getSDPType() == SDPType.Exception) {
						ExceptionProtocol ep = (ExceptionProtocol)receiveP.getSdpEntity();
						result = new InvokeResult(ThrowErrorHelper.throwServiceError(ep.getErrorCode(), ep.getErrorMsg()), null);
					} else if(receiveP.getSDPType() == SDPType.Reset){ /**服务重启*/
						AsyncDetectHelper.detectRebootServer(server);
						logger.debug("Time " + System.currentTimeMillis() + " server receive the reboot protocol , server " + server.getAddress() + "is marked as Reboot state!!");	
			        } else {
						result = new InvokeResult(new Exception("userdatatype error!"), null);
					}
					if (result.getResult() != null) {
						callBack(result.getResult());
					}
				}catch (Exception e) {
					e.printStackTrace();
					try{
						callBack(new InvokeResult(new Exception(e.getMessage()), null));
					}catch(Exception ex){
						ex.printStackTrace();
					}
				}
			}
		});
	}
	
	public abstract void callBack(Object obj);
	
}
