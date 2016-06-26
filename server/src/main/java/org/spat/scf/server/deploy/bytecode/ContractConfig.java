package org.spat.scf.server.deploy.bytecode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.spat.scf.server.deploy.bytecode.ContractInfo.SessionBean;
import org.spat.scf.server.deploy.hotdeploy.DynamicClassLoader;

public class ContractConfig {
	
	private ContractConfig() {
		
	}
	
	/**
	 * load service contract
	 * @param configPath
	 * @return
	 * @throws Exception
	 */
	public static ContractInfo loadContractInfo(String configPath, DynamicClassLoader classLoader) throws Exception{
		
		File f = new File(configPath);
		if(f != null && !f.exists()) {
			throw new Exception("contract config not exists: " + configPath);
		}
		
		ContractInfo sc = new ContractInfo();

		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse(configPath);
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
//		XPathExpression exprServiceName = xpath.compile("//contract/service-name");
		XPathExpression exprSessionBean = xpath.compile("//contract/sessionbean");
		XPathExpression exprInterface = xpath.compile("interface");
		XPathExpression exprInstanceClass = xpath.compile("instance/class");
		XPathExpression exprInstanceLookup = xpath.compile("instance/lookup");

//		Node snNode = (Node)exprServiceName.evaluate(doc, XPathConstants.NODE);
//		String serviceName = snNode.getTextContent().trim();
//		sc.setServiceName(serviceName);
		
		List<SessionBean> sbList = new ArrayList<SessionBean>();
		NodeList sessionBeanNodes = (NodeList) exprSessionBean.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < sessionBeanNodes.getLength(); i++) {
			Node interfaceNode = (Node) exprInterface.evaluate(sessionBeanNodes.item(i), XPathConstants.NODE);
			
			Map<String, String> map = new HashMap<String, String>();
			NodeList instanceClassNodes = (NodeList) exprInstanceClass.evaluate(sessionBeanNodes.item(i), XPathConstants.NODESET);
			NodeList instanceLookupNodes = (NodeList) exprInstanceLookup.evaluate(sessionBeanNodes.item(i), XPathConstants.NODESET);
			for (int j = 0; j < instanceClassNodes.getLength(); j++) {
				map.put(instanceLookupNodes.item(j).getTextContent().trim(), instanceClassNodes.item(j).getTextContent().trim());
			}
			
			String interfaceName = interfaceNode.getTextContent().trim();
			Class<?> cls = classLoader.loadClass(interfaceName);
			ClassInfo ci = ScanClass.contract(cls, true);
			
			sbList.add(new SessionBean(interfaceName, map, ci));
		}

		sc.setSessionBeanList(sbList);
		return sc;
	}
}
