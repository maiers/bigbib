/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.comci.bigbib.action;

import de.comci.bigbib.AnalyticsService;
import de.comci.bigbib.BibService;
import de.comci.bigbib.PeristentBibTexEntry;

/**
 *
 * @author Sebastian
 */
public abstract class Action {
    
    protected BibService publications;
    protected AnalyticsService analytics;
    protected PeristentBibTexEntry publication;

    public Action setPublications(BibService publications) {
        this.publications = publications;
        return this;
    }

    public Action setAnalytics(AnalyticsService analytics) {
        this.analytics = analytics;
        return this;
    }
    
    public abstract void action();

    public Action setPublication(PeristentBibTexEntry singleBib) {
        this.publication = singleBib;
        return this;
    }
        
}
