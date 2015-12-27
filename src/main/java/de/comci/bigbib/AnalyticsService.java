/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.comci.bigbib;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import de.comci.bigbib.entity.Pair;
import de.comci.bigbib.entity.SimilarAuthors;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.jbibtex.BibTeXEntry;

/**
 *
 * @author Sebastian
 */
@Stateless
@Path("analytics")
public class AnalyticsService extends PersistenceService {
    
    @GET
    @Path("related/authors/pub/{id}")
    @Produces("application/json")
    public Response getRelatedAuthors(
            final @PathParam("id") ObjectId pubId) {
        
        try {
            updateAuthors();
            
            DBObject findOne = bibtexc.findOne(pubId);        
            PeristentBibTexEntry pub = new PeristentBibTexEntry(findOne);
            
            String author = pub.getField(BibTeXEntry.KEY_AUTHOR).toUserString();
            
            String[] authors = author.split("and");
            for (int i = 0; i < authors.length; i++) {
                authors[i] = authors[i].trim();
            }
            
            MapReduceCommand cmd = new MapReduceCommand(
                    authorsc, 
                    Utils.getMapFunction("coauthors"), 
                    Utils.getReduceFunction("coauthors"),
                    String.format("coauthors-%s", Math.floor(Math.random() * 1000000)), 
                    MapReduceCommand.OutputType.REPLACE, 
                    new BasicDBObject("_id", new BasicDBObject("$in", authors)));
            
            MapReduceOutput coauthors = authorsc.mapReduce(cmd);
            
            
            DBCursor limit = coauthors.getOutputCollection().find().sort(new BasicDBObject("value", -1)).limit(10);
            SimilarAuthors sa = new SimilarAuthors();
            while (limit.hasNext()) {
                DBObject next = limit.next();
                sa.relatedAuthors.add(new Pair<String, Double>(next.get("_id").toString(), ((Double)next.get("value"))));                
            }
            coauthors.getOutputCollection().drop();
            
            return Response.ok(sa).build();
            
        } catch (IOException ex) {
            Logger.getLogger(AnalyticsService.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }
    
    @GET
    @Path("related/publications/pub/{id}")
    public Response getRelatedPublications(
            final @PathParam("id") ObjectId pubId) {
        
        DBObject findOne = bibtexc.findOne(pubId);
        
        BibTeXEntry pub = bibTeXEntryFromDBObject(findOne);
        
        System.out.println("__" + pub);
        
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    }
    
    public void updateAuthors() {
        try {
            MapReduceCommand cmd = new MapReduceCommand(bibtexc, Utils.getMapFunction("authors"), Utils.getReduceFunction("authors"), "authors", MapReduceCommand.OutputType.REPLACE, null);
            bibtexc.mapReduce(cmd);
        } catch (IOException ex) {
            Logger.getLogger(AnalyticsService.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
}
