package org.spat.scf.server.contract.context;

import org.spat.scf.protocol.serializer.SerializeBaseFactory;
import org.spat.scf.server.contract.init.IInit;
/**
 * @author HAOXB
 * 序列化注解类初始化
 */
public class SerializerClassInit implements IInit {

	@Override
	public void init() {
		try{
			SerializeBaseFactory.getInstance();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
