package org.spat.scf.protocol.utility;

import java.io.Serializable;

import org.spat.scf.protocol.entity.Out;
import org.spat.scf.serializer.annotation.SCFMember;
import org.spat.scf.serializer.annotation.SCFSerializable;

/**
 * KeyValuePair
 *
 * @author Service Platform Architecture Team
 * 
 */
@SCFSerializable(name="RpParameter")
public class KeyValuePair implements Serializable {

	private static final long serialVersionUID = -3406649434383389222L;

	@SCFMember(name="name", sortId = 1)
    private String key;

    @SCFMember(sortId = 2)
    private Object value;
    

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        if( value instanceof Out){
            this.value = ((Out<?>)value).getOutPara();
        } else {
            this.value = value;
        }
    }

    public KeyValuePair() {
    }

    public KeyValuePair(String key, Object value) {
        this.setKey(key);
        this.setValue(value);
    }
}