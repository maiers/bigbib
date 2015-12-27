/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.comci.bigbib.entity;

import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian
 */
@XmlRootElement
public class SimilarAuthors {
 
    public List<Pair<String, Double>> relatedAuthors = new LinkedList<Pair<String, Double>>();

}
