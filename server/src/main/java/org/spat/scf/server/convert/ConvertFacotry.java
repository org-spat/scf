package org.spat.scf.server.convert;

import org.spat.scf.protocol.enumeration.SerializeType;
import org.spat.scf.protocol.sfp.Protocol;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;

/**
 * A convert facotry for create converter
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public class ConvertFacotry {
	
	/**
	 * java
	 */
	private static JavaConvert javaConvert = new JavaConvert();
	
	/**
	 * SCFBinary
	 */
	private static SCFBinaryConvert scfBinaryConvert = new SCFBinaryConvert();
	
	private static ILog logger = LogFactory.getLogger(ConvertFacotry.class);
	

	public static IConvert getConvert(Protocol p) {
		if(p.getSerializeType() == SerializeType.SCFBinary) {
			return scfBinaryConvert;
		} else if(p.getSerializeType() == SerializeType.JAVABinary) {
			return javaConvert;
		} 
		
		logger.error("can't get IConvert not : json ,java, customBinary ");
		return null;
	}
}