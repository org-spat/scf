package org.spat.scf.client.configurator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.spat.scf.client.SCFConst;
import org.spat.scf.client.secure.KeyProfile;
import org.spat.scf.client.utility.XMLHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * ServiceConfig
 *
 * @author Service Platform Architecture Team 
 */
public final class ServiceConfig {

    private String servicename;
    private int serviceid;
    private SocketPoolProfile SocketPool;
    private ProtocolProfile protocol;
    private List<ServerProfile> servers;
    private KeyProfile SecureKey;//授权配置文件
    //新加
    private long rtime;
    
    
    public long getRtime() {
		return rtime;
	}

	public void setRtime(long rtime) {
		this.rtime = rtime;
	}

	public KeyProfile getSecureKey() {
		return SecureKey;
	}

	public void setSecureKey(KeyProfile secureKey) {
		SecureKey = secureKey;
	}

    private ServiceConfig() {
    }

    public SocketPoolProfile getSocketPool() {
        return SocketPool;
    }

    public ProtocolProfile getProtocol() {
        return protocol;
    }

    public List<ServerProfile> getServers() {
        return servers;
    }

    public int getServiceid() {
        return serviceid;
    }

    public String getServicename() {
        return servicename;
    }

    public static ServiceConfig GetConfig(String serviceName) throws Exception {
        File f = new File(SCFConst.CONFIG_PATH);
        if (!f.exists()) {
            throw new Exception("scf.config not fond:" + SCFConst.CONFIG_PATH);
        }
        Element xmlDoc = XMLHelper.GetXmlDoc(SCFConst.CONFIG_PATH);
        return createConfig(serviceName, xmlDoc);
    }
    
    public static ServiceConfig GetConfig(String serviceName, String XmlStr) throws Exception {
    	if(null == XmlStr || "".equals(XmlStr)){
    		printExceprion(5, serviceName);
    	}
    	Element xmlDoc = XMLHelper.GetXmlDocFromStr(XmlStr);
    	return createConfig(serviceName, xmlDoc);
    }
    
    public static List<String> getServerNode(String serviceName) throws Exception {
    	 File f = new File(SCFConst.CONFIG_PATH);
         if (!f.exists()) {
             throw new Exception("scf.config not fond:" + SCFConst.CONFIG_PATH);
         }

         Element xmlDoc = XMLHelper.GetXmlDoc(SCFConst.CONFIG_PATH);
    	XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        Node serviceNode = (Node) xpath.evaluate("//Service[@name='" + serviceName + "']", xmlDoc, XPathConstants.NODE);
        if(serviceNode == null){
        	printExceprion(0,serviceName);
        }
    	NodeList xnServers = (NodeList) xpath.evaluate("Loadbalance/Server/add", serviceNode, XPathConstants.NODESET);
        if(xnServers == null || xnServers.getLength() == 0){
        	printExceprion(3,serviceName);
        }
          
        List<String> servers = new ArrayList<String>();
        for (int i = 0; i < xnServers.getLength(); i++) {
        	  servers.add(xnServers.item(i).getAttributes().getNamedItem("name").getNodeValue());
        }
        return servers;
    }
    
    public static ServiceConfig createConfig(String serviceName, Element xmlDoc) throws Exception{
    	XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        Node serviceNode = (Node) xpath.evaluate("//Service[@name='" + serviceName + "']", xmlDoc, XPathConstants.NODE);
        if(serviceNode == null){
        	printExceprion(0,serviceName);
        }
        
        ServiceConfig config = new ServiceConfig();
        config.servicename = serviceNode.getAttributes().getNamedItem("name").getNodeValue();
        Node idNode = serviceNode.getAttributes().getNamedItem("id");
        if(idNode == null){
        	printExceprion(4,serviceName);
        }
        config.serviceid = Integer.parseInt(idNode.getNodeValue());
        Node xnSocketPool = (Node) xpath.evaluate("Commmunication/SocketPool", serviceNode, XPathConstants.NODE);
        if(xnSocketPool == null){
        	printExceprion(1,serviceName);
        }
        config.SocketPool = new SocketPoolProfile(xnSocketPool);

        Node xnProtocol = (Node) xpath.evaluate("Commmunication/Protocol", serviceNode, XPathConstants.NODE);
        if(xnProtocol == null){
        	printExceprion(2,serviceName);
        }
        config.protocol = new ProtocolProfile(xnProtocol);
        
        /**
         * 加载授权文件key
         */
        Node xnKey = (Node) xpath.evaluate("Secure/Key", serviceNode, XPathConstants.NODE);
        config.SecureKey = new KeyProfile(xnKey);
        
        NodeList xnServers = (NodeList) xpath.evaluate("Loadbalance/Server/add", serviceNode, XPathConstants.NODESET);
        if(xnServers == null || xnServers.getLength() == 0){
        	printExceprion(3,serviceName);
        }
        
        List<ServerProfile> servers = new ArrayList<ServerProfile>();
        for (int i = 0; i < xnServers.getLength(); i++) {
            servers.add(new ServerProfile(xnServers.item(i)));
        }
        config.servers = servers;
        config.servicename = serviceName;
        return config;
    }
    
    public static List<String> getServiceName() throws Exception {
    	File f = new File(SCFConst.CONFIG_PATH);
        if (!f.exists()) {
            throw new Exception("scf.config not fond:" + SCFConst.CONFIG_PATH);
        }

        Element xmlDoc = XMLHelper.GetXmlDoc(SCFConst.CONFIG_PATH);
        XPathFactory xpathFactory = XPathFactory.newInstance();
       XPath xpath = xpathFactory.newXPath();
       NodeList nodeList = (NodeList) xpath.evaluate("//Service",xmlDoc, XPathConstants.NODESET);
       List<String> services = new ArrayList<String>();
       for (int i = 0; i < nodeList.getLength(); i++) {
    	   services.add(nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue());
       }
       return services;
       
    }
    
    private static void printExceprion(int i, String serviceName) throws Exception{
    	switch (i) {
			case 0:
				throw new Exception(SCFConst.CONFIG_PATH + "中没有发现" + serviceName + "服务节点!");
			case 1:
				throw new Exception(SCFConst.CONFIG_PATH + "服务节点" + serviceName + "没有发现Commmunication/SocketPool配置!");
			case 2:
				throw new Exception(SCFConst.CONFIG_PATH + "服务节点" + serviceName + "没有发现Commmunication/Protocol配置!");
			case 3:
				throw new Exception(SCFConst.CONFIG_PATH + "服务节点" + serviceName + "没有发现Loadbalance/Server/add配置!");
			case 4:
				throw new Exception(SCFConst.CONFIG_PATH + "服务节点" + serviceName + "没有发现Service/id配置!");
			case 5:
				throw new Exception("没有服务节点" + serviceName + "访问权限，请添加权限!");
			default:
				break;
		}
    }
}
