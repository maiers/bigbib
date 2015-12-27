/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.comci.bigbib.entity;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PageinationResult<T> {
    
    public int page;
    public int pageSize;
    public int totalPages;
    public int totalSize;
    public List<T> items;

    public PageinationResult(int pagesize) {
        this.pageSize = pagesize;
    }

    public void setTotalSize(int count) {
        totalSize = count;
        totalPages = (int)Math.ceil(1.0d * totalSize / pageSize);
    }
    
}
