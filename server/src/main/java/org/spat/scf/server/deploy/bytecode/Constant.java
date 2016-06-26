package org.spat.scf.server.deploy.bytecode;

import org.spat.scf.protocol.annotation.OperationContract;
import org.spat.scf.protocol.annotation.ServiceBehavior;
import org.spat.scf.protocol.entity.Out;
import org.spat.scf.protocol.sdp.RequestProtocol;
import org.spat.scf.protocol.utility.KeyValuePair;
import org.spat.scf.server.contract.context.IProxyFactory;
import org.spat.scf.server.contract.context.IProxyStub;
import org.spat.scf.server.contract.context.SCFContext;
import org.spat.scf.server.contract.context.SCFResponse;
import org.spat.scf.server.convert.ConvertFacotry;
import org.spat.scf.server.convert.IConvert;
import org.spat.scf.server.utility.ErrorState;
import org.spat.scf.server.utility.ServiceFrameException;

/**
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public class Constant {

	
	/**
	 * out parameter name
	 */
	public static final String OUT_PARAM = Out.class.getName();
	
	/**
	 *  IProxyStub class name
	 */
	public static final String IPROXYSTUB_CLASS_NAME = IProxyStub.class.getName();
	
	/**
	 * SCFContext class name
	 */
	public static final String SCFCONTEXT_CLASS_NAME = SCFContext.class.getName();
	
	/**
	 * SCFRequest class name
	 */
	public static final String SCFRESPONSE_CLASS_NAME = SCFResponse.class.getName();
	
	/**
	 * ServiceFrameException class name
	 */
	public static final String SERVICEFRAMEEXCEPTION_CLASS_NAME = ServiceFrameException.class.getName();
	
	/**
	 * Request protocol class name
	 */
	public static final String REQUEST_PROTOCOL_CLASS_NAME = RequestProtocol.class.getName();
	
	/**
	 * IConvert class name
	 */
	public static final String ICONVERT_CLASS_NAME = IConvert.class.getName();
	
	/**
	 * ConvertFactory class name
	 */
	public static final String CONVERT_FACTORY_CLASS_NAME = ConvertFacotry.class.getName();
	
	/**
	 * KeyValuePair protocol class name
	 */
	public static final String KEYVALUEPAIR_PROTOCOL_CLASS_NAME = KeyValuePair.class.getName();
	
	/**
	 * ErrorState class name
	 */
	public static final String ERRORSTATE_CLASS_NAME = ErrorState.class.getName();
	
	/**
	 * IProxyFactory class name
	 */
	public static final String IPROXYFACTORY_CLASS_NAME = IProxyFactory.class.getName();
	
	/**
	 * OperationContract class name
	 */
	public static final String OPERATIONCONTRACT_CLASS_NAME = OperationContract.class.getName();
	
	/**
	 * ServiceBehavior class name
	 */
	public static final String SERVICEBEHAVIOR_CLASS_NAME = ServiceBehavior.class.getName();
	
	/**
	 * ServiceContract class name
	 */
	public static final String SERVICECONTRACT_CLASS_NAME = ContractInfo.class.getName();
}