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
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FileStorageResult {
    
    public String fileId;
    public String bibId;
        
}
