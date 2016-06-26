package org.spat.scf.server.deploy.bytecode;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.junit.Test;
import org.spat.scf.server.contract.context.IProxyFactory;
import org.spat.scf.server.deploy.hotdeploy.DynamicURLClassLoader;


public class CreateManagerTest {
	static {
		//Log4jConfig.configure(RootPath.projectRootPath + "config/scf_log4j.xml");
	}

	@Test
	public void testCareteProxy() throws Exception {

//		IProxyFactory pf = (new CreateManager()).careteProxy("D:/serviceframe_v2_II/service/deploy/demo",
//															 "D:/serviceframe_v2_online/lib/",
//															 "D:/serviceframe_v2_online/service/deploy/imc/");
//		
//		System.out.println("pf:"+pf);
//		ILocalProxy proxy = pf.createProxy("TestService");
//		System.out.println("proxy:"+proxy);
//		
//		System.out.println("proxy==null:"+(proxy==null));
//		
//		
//		RequestPara rp = new RequestPara();
//		rp.setFromIP("127.0.0.1");
//		rp.setToIP("127.0.0.1");
//		
//		Request request = new Request();
//		request.setLookup("TestService");
//		request.setMethodName("returnint");
//		
//		ProtocolEntity pe = new ProtocolEntity();
//		pe.setMsgBodyEntity(request);
//		pe.setDataType(MsgBodyType.Request);
//		pe.setSerializ(Serializ.CustomBinary);
//		rp.setProtocol(pe);
//		
//		proxy.invoke(rp);
//		System.out.println("pf:"+pf);
	}
	
	@Test
	public void hotDeploy() throws Exception {
		
		DynamicURLClassLoader classLoader = new DynamicURLClassLoader();
		classLoader.addURL("D:/serviceframe_v2_online/lib/serviceframe/serviceframe-2.0.1.beta.jar");
		classLoader.addFolder("D:/serviceframe_v2_online/service/deploy/imc/");
		Class<?> cmCls = classLoader.loadClass("com.bj58.sfft.serviceframe.deploy.bytecode.CreateManager");
		
		Method createProxy = cmCls.getDeclaredMethod("careteProxy", new Class[] { String.class });
		IProxyFactory pf = (IProxyFactory)createProxy.invoke(cmCls.newInstance(), "D:/serviceframe_v2_online/service/deploy/imc/");
		System.out.println("pf:" + pf);
	}
	
	
	@Test
	public void fun() throws IOException {
		JarFile jarFile = new JarFile("D:/serviceframe_v2_online/lib/serviceframe/serviceframe-2.0.1.beta.jar"); // ����jar�ļ�
		Enumeration<JarEntry> entry = jarFile.entries();
		JarEntry jarEntry = null;
		while(entry.hasMoreElements()) {
			jarEntry = entry.nextElement();
			System.out.println(jarEntry.getName());
		}
	}
}
