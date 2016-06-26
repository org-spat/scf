package org.spat.scf.client.configurator;

import java.nio.charset.Charset;

import javax.naming.directory.NoSuchAttributeException;

import org.spat.scf.protocol.enumeration.CompressType;
import org.spat.scf.protocol.enumeration.SerializeType;
import org.spat.scf.protocol.serializer.SerializeBase;
import org.w3c.dom.Node;

/**
 * ProtocolProfile
 *
 * @author Service Platform Architecture Team 
 */
public class ProtocolProfile {

    private SerializeType serializerType;
    private SerializeBase serialize;
    public Charset Encoder;
    public byte serviceID;
    public CompressType compress;

    public ProtocolProfile(Node node) throws Exception {
        Node attrSer = node.getAttributes().getNamedItem("serialize");
        if (attrSer == null) {
            throw new ExceptionInInitializerError("Not find attrbuts:" + node.getNodeName() + "[@'serialize']");
        }
        String value = attrSer.getNodeValue().trim().toLowerCase();
        if (value.equalsIgnoreCase("java")) {
            serializerType = SerializeType.JAVABinary;
        } else if (value.equalsIgnoreCase("json")) {
            serializerType = SerializeType.JSON;
        } else if (value.equalsIgnoreCase("xml")) {
            serializerType = SerializeType.XML;
        } else if (value.equalsIgnoreCase("scf")) {
            serializerType = SerializeType.SCFBinary;
        } else {
            throw new NoSuchAttributeException("Protocol not supported " + value + "!");
        }
        this.serialize = SerializeBase.getInstance(serializerType);
        attrSer = node.getAttributes().getNamedItem("encoder");
        if (attrSer == null) {
            this.Encoder = Charset.forName("UTF-8");
        } else {
            this.Encoder = Charset.forName(attrSer.getNodeValue());
        }
        this.serialize.setEncoder(this.Encoder);
        serviceID = Byte.parseByte(node.getParentNode().getParentNode().getAttributes().getNamedItem("id").getNodeValue());//TODO 待检验
        compress = (CompressType) Enum.valueOf(CompressType.class, node.getAttributes().getNamedItem("compressType").getNodeValue());
    }

    public Charset getEncoder() {
        return Encoder;
    }

    public CompressType getCompress() {
        return compress;
    }

    public SerializeBase getSerializer() {
        return serialize;
    }

    public SerializeType getSerializerType() {
        return serializerType;
    }

    public byte getServiceID() {
        return serviceID;
    }
}
