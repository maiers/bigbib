/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.comci.bigbib.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian
 */
@XmlRootElement(name="Storage")
@XmlAccessorType(XmlAccessType.FIELD)

public class StorageResult {
    
    public int saved = 0;
    public int updated = 0;
    public int ignored = 0;
        
}
