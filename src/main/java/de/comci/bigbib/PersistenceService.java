/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.comci.bigbib;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.gridfs.GridFS;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.jbibtex.BibTeXEntry;

/**
 *
 * @author Sebastian
 */
public abstract class PersistenceService {
    
    protected DBCollection bibtexc;
    protected MongoClient client;
    protected DB db;
    protected GridFS gridFS;
    protected DBCollection authorsc;

    @PreDestroy
    public void destroy() {
        client.close();
    }

    @PostConstruct
    public void init() {
        try {            
            client = new MongoClient();
            db = client.getDB("bigbib");
            gridFS = new GridFS(db);
            bibtexc = db.getCollection("bibtex");
            authorsc = db.getCollection("authors");
            BasicDBObject fields = new BasicDBObject("fields", 1);
            BasicDBObject options = new BasicDBObject("unique", true).append("name", "ix-unique-fields");
            bibtexc.ensureIndex(fields, options);
            bibtexc.ensureIndex(new BasicDBObject("key", 1));
            bibtexc.ensureIndex(new BasicDBObject("hash", 1), new BasicDBObject("unique", true).append("name", "ix-unique-hash").append("sparse", true));
            bibtexc.ensureIndex(new BasicDBObject("type", 1).append("key", 1));
        } catch (UnknownHostException ex) {
            Logger.getLogger(BibService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * 
     * @param next
     * @return
     * @deprecated Use {@link PeristentBibTexEntry}
     */
    @Deprecated
    protected BibTeXEntry bibTeXEntryFromDBObject(DBObject next) {
        return new PeristentBibTexEntry(next);
    }
    
}
