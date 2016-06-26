package org.spat.scf.client.utility;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

/**
 * XMLHelper
 *
 * @author Service Platform Architecture Team 
 */
public class XMLHelper {

    public static Element GetXmlDoc(String filePath) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
//            System.err.println(pce);
//            System.exit(1);
            pce.printStackTrace();
        }
        Document doc = null;
        try {
            File f = new File(filePath);
            doc = db.parse(f);
        } catch (Exception e) {
//            System.err.println(e);
//            System.exit(1);
            e.printStackTrace();
        }
        return (Element) doc.getDocumentElement();
    }
    
    public static Element GetXmlDocFromStr(String xmlStr) {
    	 DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = null;
         try {
             db = dbf.newDocumentBuilder();
         } catch (ParserConfigurationException pce) {
//             System.err.println(pce);
//             System.exit(1);
             pce.printStackTrace();
         }
         Document doc = null;
         try {
        	 InputStream inputStream = new ByteArrayInputStream(xmlStr.getBytes());
             doc = db.parse(inputStream);
         } catch (Exception e) {
//             System.err.println(e);
//             System.exit(1);
             e.printStackTrace();
         }
         return (Element) doc.getDocumentElement();
    }

    public static Node selectSingleNode(String express, Object source) {
        Node result = null;
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        try {
            result = (Node) xpath.evaluate(express, source, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static NodeList selectNodes(String express, Object source) {
        NodeList result = null;
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        try {
            result = (NodeList) xpath.evaluate(express, source, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return result;
    }
}
