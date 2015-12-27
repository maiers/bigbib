/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.comci.bigbib.entity;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian
 */
@XmlRootElement
public class Pair<T0, T1> {
    
    T0 key;
    T1 value;

    public Pair(T0 key, T1 value) {
        this.key = key;
        this.value = value;
    }

    public T0 getKey() {
        return key;
    }

    public void setKey(T0 key) {
        this.key = key;
    }

    public T1 getValue() {
        return value;
    }

    public void setValue(T1 value) {
        this.value = value;
    }

}
