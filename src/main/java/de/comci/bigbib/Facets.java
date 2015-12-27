/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.comci.bigbib;

import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.FIELD)
public class Facets {
    
    Map<Integer, Integer> year;
    Map<String, Integer> author;
    Map<String, Integer> journal;
    long numItems;
    
}
