/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spat.scf.serializer.classes;

/**
 *
 * @author Administrator
 */
public class GKeyValuePair<TKey, TValue> {

    private TKey _key;
    private TValue _value;

    public GKeyValuePair(TKey key, TValue value) {

        _key = key;
        _value = value;
    }

    /**
     * @return the _key
     */
    public TKey getKey() {
        return _key;
    }

    /**
     * @param _key the _key to set
     */
    public void setKey(TKey key) {
        this._key = key;
    }

    /**
     * @return the _value
     */
    public TValue getValue() {
        return _value;
    }

    /**
     * @param _value the _value to set
     */
    public void setValue(TValue value) {
        this._value = value;
    }
}
