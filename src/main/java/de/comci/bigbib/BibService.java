/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.comci.bigbib;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.FormDataParam;
import de.comci.bigbib.action.Action;
import de.comci.bigbib.entity.FileInfo;
import de.comci.bigbib.entity.FileStorageResult;
import de.comci.bigbib.entity.FilesResult;
import de.comci.bigbib.entity.ImageFileInfo;
import de.comci.bigbib.entity.PageinationResult;
import de.comci.bigbib.entity.StorageResult;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.enterprise.context.RequestScoped;
import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.bson.types.ObjectId;
import org.imgscalr.Scalr;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXParser;
import org.jbibtex.Key;
import org.jbibtex.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Sebastian
 */
@RequestScoped
@Path("bib")
public class BibService extends PersistenceService {

    private static final String USER_AGENT = "Mozilla/5.001 (windows; U; NT4.0; en-US; rv:1.0) Gecko/25250101";
    private static final Logger LOG = Logger.getLogger(BibService.class.getName());

    @POST
    @Produces("application/json")
    public Response addBibtex(String bibtex) {

        StringReader reader = new StringReader(bibtex);
        BibTeXParser parser = new BibTeXParser();

        try {

            List<BibTeXEntry> notSaved = new LinkedList<BibTeXEntry>();
            List<BibTeXEntry> saved = new LinkedList<BibTeXEntry>();

            BibTeXDatabase bibdb = parser.parse(reader);

            for (BibTeXEntry e : bibdb.getEntries().values()) {
                BasicDBObject obj = new BasicDBObject();
                obj.append("key", e.getKey().getValue());
                obj.append("type", e.getType().getValue());
                obj.append("inserted", Calendar.getInstance().getTime());
                obj.append("hash", e.hashCode());
                BasicDBObject fields = new BasicDBObject();
                for (Key key : e.getFields().keySet()) {
                    fields.append(key.getValue(), e.getField(key).toUserString());
                }
                obj.append("fields", fields);

                try {
                    WriteResult save = bibtexc.save(obj);
                    saved.add(e);
                    lookForPdf(obj.getObjectId("_id"), e.getField(BibTeXEntry.KEY_TITLE).toUserString());
                } catch (MongoException.DuplicateKey ex) {
                    notSaved.add(e);
                }

            }

            StorageResult result = new StorageResult();
            result.ignored = notSaved.size();
            result.saved = saved.size();

            return Response.ok(result).build();

        } catch (IOException ex) {
            Logger.getLogger(BibService.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().entity(ex.getMessage()).build();
        } catch (ParseException ex) {
            Logger.getLogger(BibService.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().entity(ex.getMessage()).build();
        }

    }

    private void lookForPdf(ObjectId pubId, String title) {
        System.out.println("_looking_for_pdf_" + title);

        try {
            title = URLEncoder.encode("\"" + title + "\"", "utf-8");
            Document doc = Jsoup.connect(String.format("https://www.google.de/search?q=%s+filetype%%3Apdf&safe=off&ie=UTF-8", title)).userAgent(USER_AGENT).get();
            Elements select = doc.select("ol li.g a");
            int count = 5;
            for (Element e : select) {
                String stringURL = e.attr("href");

                stringURL = Utils.getQueryParams(stringURL).get("q").get(0);

                try {
                    final URL url = new URL(stringURL);
                    URLConnection connection = url.openConnection();

                    String contentType = null;
                    try {
                        contentType = connection.getContentType().toString();
                    } catch (NullPointerException ex) {
                        System.out.println("no content type provided");
                        continue;
                    }

                    if (!contentType.equals("application/pdf")) {
                        System.out.println("_not_a_pdf_file_" + contentType);
                        continue;
                    }

                    addFile(url.getFile(), contentType,
                            connection.getInputStream(),
                            new BasicDBObject("bibId", pubId)
                            .append("type", "file")
                            .append("source", "google")
                            .append("url", stringURL));

                    if (--count < 0) {
                        break;
                    }

                } catch (IOException ex) {
                    System.err.println("coult not handle url " + stringURL);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(BibService.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @GET
    @Path("{bid}")
    @Produces("application/json")
    public Response getBib(final @PathParam("bid") ObjectId id) {

        return Response.ok(getSingleBib(id)).build();
    }

    public PeristentBibTexEntry getSingleBib(ObjectId id) {

        DBObject findOne = bibtexc.findOne(new BasicDBObject("_id", id));
        if (findOne == null) {
            return null;
        }
        return new PeristentBibTexEntry(findOne);

    }

    @DELETE
    @Path("{bid}")
    @Produces("application/json")
    public Response deleteBib(final @PathParam("bid") ObjectId id) {

        try {
            actionDeleteBib(id);
            return Response.ok().build();
        } catch (NoSuchElementException ex) {
            return Response.status(404).build();
        }
    }

    public void actionDeleteBib(ObjectId id) {

        final BasicDBObject getById = new BasicDBObject("_id", id);

        if (bibtexc.count(getById) != 1) {
            throw new NoSuchElementException();
        };

        // clear pub
        WriteResult remove = bibtexc.remove(getById);

        // clear files
        gridFS.remove(new BasicDBObject("metadata.bibId", id));

    }

    public PageinationResult<PeristentBibTexEntry> getBibEntries(int offset, 
            int pagesize, 
            String query, 
            List<String> facetAuthors,
            List<String> facetYears,
            List<String> facetJournals) {

        // query local database
        BasicDBObject dbQuery = getDBQuery(query, facetAuthors, facetYears, facetJournals);
        DBCursor cursor = bibtexc.find(dbQuery);
        cursor.limit(pagesize);
        cursor.skip(offset * pagesize);

        List<PeristentBibTexEntry> entries = new LinkedList<PeristentBibTexEntry>();

        while (cursor.hasNext()) {
            entries.add(new PeristentBibTexEntry(cursor.next()));
        }

        // create result representation
        PageinationResult<PeristentBibTexEntry> page = new PageinationResult<PeristentBibTexEntry>(pagesize);
        page.setTotalSize(cursor.count());
        page.page = offset;
        page.items = entries;
        return page;
    }

    private BasicDBObject getDBQuery(String query, List<String> facetAuthors, List<String> facetYears, List<String> facetJournals) {
        BasicDBObject dbQuery = new BasicDBObject();
        BasicDBList combination = new BasicDBList();
        if (query != null && !query.trim().isEmpty()) {
            final BasicDBList queryOr = new BasicDBList();
            Pattern p = Pattern.compile(query, Pattern.CASE_INSENSITIVE);
            queryOr.add(new BasicDBObject("fields.author", p));
            queryOr.add(new BasicDBObject("fields.journal", p));
            queryOr.add(new BasicDBObject("fields.title", p));
            queryOr.add(new BasicDBObject("fields.year", p));
            queryOr.add(new BasicDBObject("fields.abstract", p));
            combination.add(new BasicDBObject("$or", queryOr));
        }
        
        createStringFacetQuery(facetAuthors, combination, "fields.author");
        
        if (facetYears != null && !facetYears.isEmpty()) {            
            combination.add(new BasicDBObject("fields.year", new BasicDBObject("$in", facetYears)));
        }
        
        if (facetJournals != null && !facetJournals.isEmpty()) {            
            combination.add(new BasicDBObject("fields.journal", new BasicDBObject("$in", facetJournals)));
        }
        
        if (!combination.isEmpty()) {
            dbQuery.append("$and", combination);
        }

        LOG.info(dbQuery.toString());

        return dbQuery;
    }

    private void createStringFacetQuery(List<String> facet, BasicDBList combination, String fieldName) {
        if (facet != null && !facet.isEmpty()) {
            BasicDBList listOr = new BasicDBList();
            for (String f : facet) {
                listOr.add(new BasicDBObject(fieldName, Pattern.compile(f, Pattern.CASE_INSENSITIVE)));
            }
            combination.add(new BasicDBObject("$or", listOr));
        }
    }

    @GET
    @Produces("application/json")
    public Response getBib(
            final @QueryParam("page") @DefaultValue("0") int offset,
            final @QueryParam("size") @DefaultValue("100") int pagesize,
            final @QueryParam("query") String query,
            final @QueryParam("facet-author") List<String> facetAuthors,
            final @QueryParam("facet-year") List<String> facetYears,
            final @QueryParam("facet-journal") List<String> facetJournals) {

        LOG.info(query);
        LOG.info(facetAuthors.toString());
        LOG.info(facetYears.toString());
        LOG.info(facetJournals.toString());

        return Response.ok(getBibEntries(offset, pagesize, query, facetAuthors, facetYears, facetJournals))
                .header("Content-Type", "application/json; charset=utf-8").build();

    }

    @GET
    @Path("facets")
    @Produces("application/json")
    public Response getFacets(
            final @QueryParam("query") String query,
            final @QueryParam("facet-authors") List<String> facetAuthors,
            final @QueryParam("facet-years") List<Integer> facetYears,
            final @QueryParam("facet-journals") List<String> facetJournals) {

        BasicDBObject dbQuery = getDBQuery(query, facetAuthors, null, null);

        // get year
        List<DBObject> ops = new LinkedList<DBObject>();
        if (query != null && !query.isEmpty()) {
            ops.add(new BasicDBObject("$match", dbQuery));
        }
        ops.add(new BasicDBObject("$group", new BasicDBObject("_id", "$fields.year").append("count", new BasicDBObject("$sum", 1))));
        ops.add(new BasicDBObject("$sort", new BasicDBObject("_id", 1)));

        AggregationOutput aggregate = bibtexc.aggregate(ops.get(0), ops.subList(1, ops.size()).toArray(new DBObject[ops.size() - 1]));
        Map<Integer, Integer> years = new HashMap<Integer, Integer>();
        for (DBObject o : aggregate.results()) {
            if (o.get("_id") == null) {
                years.put(-1, (Integer) o.get("count"));
            } else {
                years.put(new Integer((String) o.get("_id")), (Integer) o.get("count"));
            }
        }

        // authors
        Map<String, Integer> authors = getFacet(dbQuery, "facet-authors", 10);

        // journals
        Map<String, Integer> journals = getFacet(dbQuery, "facet-journals", 10);

        Facets facets = new Facets();
        facets.year = years;
        facets.author = authors;
        facets.journal = journals;
        facets.numItems = bibtexc.count(dbQuery);

        return Response.ok(facets).header("Content-Type", "application/json; charset=utf-8").build();

    }

    private Map<String, Integer> getFacet(BasicDBObject dbQuery, String target, int limit) {
        // authors
        Map<String, Integer> list = new HashMap<String, Integer>();
        int maxCount = 0;
        String tmpCollectionName = "tmp-facet-authors" + (int) (Math.random() * 10000);
        try {
            MapReduceOutput mapReduce = bibtexc.mapReduce(
                    Utils.getMapFunction(target),
                    Utils.getReduceFunction(target),
                    tmpCollectionName,
                    MapReduceCommand.OutputType.REPLACE,
                    dbQuery);
            for (DBObject o : mapReduce.getOutputCollection().find().sort(new BasicDBObject("value", -1)).limit(limit)) {
                final int count = ((Double) o.get("value")).intValue();
                maxCount = Math.max(count, maxCount);
                list.put((String) o.get("_id"), count);
            }
            mapReduce.drop();
        } catch (IOException ex) {
            Logger.getLogger(BibService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    @DELETE
    @Path("{bid}/files/{fid}")
    public Response deleteBibFile(
            final @PathParam("bid") ObjectId bibId,
            final @PathParam("fid") ObjectId fileId) {

        gridFS.remove(new BasicDBObject("_id", fileId).append("metadata.bibId", bibId));
        gridFS.remove(new BasicDBObject("metadata.pdfId", fileId));
        return Response.ok().build();
    }

    @GET
    @Path("{bid}/images/{fid}")
    public Response getBibImageStream(
            final @PathParam("bid") ObjectId bibId,
            final @PathParam("fid") ObjectId fileId) {

        return getBibFileStream(bibId, fileId);
    }

    @GET
    @Path("{bid}/files/{fid}")
    public Response getBibFileStream(
            final @PathParam("bid") ObjectId bibId,
            final @PathParam("fid") ObjectId fileId) {

        final GridFSDBFile file = gridFS.findOne(new BasicDBObject("_id", fileId).append("metadata.bibId", bibId));

        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                InputStream input = file.getInputStream();
                byte[] b = new byte[10];
                while (input.read(b) != -1) {
                    output.write(b);
                }
                output.flush();
            }
        };

        return Response.ok(stream, file.getContentType()).header("Content-Length", file.getLength()).build();
    }

    @POST
    @Consumes(value = "multipart/form-data")
    @Path("{id}/files")
    public Response postBibFile(
            final @PathParam("id") ObjectId bibId,
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
            @FormDataParam("file") FormDataBodyPart bodyPart,
            @FormDataParam("file") FormDataMultiPart multiPart) {

        ObjectId fileId = null;

        try {

            try {

                fileId = addFile(contentDispositionHeader.getFileName(),
                        bodyPart.getMediaType().toString(),
                        fileInputStream,
                        new BasicDBObject("bibId", bibId)
                        .append("type", "file")
                        .append("source", "user")
                        .append("subtype", "user-upload"));

            } finally {

                try {
                    fileInputStream.close();
                } catch (IOException ex) {
                    Logger.getLogger(BibService.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

        } catch (Exception ex) {
            Logger.getLogger(BibService.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().entity(ex.getMessage()).build();
        }

        FileStorageResult result = new FileStorageResult();
        result.bibId = bibId.toString();
        result.fileId = fileId.toString();
        return Response.ok(result).build();

    }

    private void extractImages(ObjectId fileId) throws IOException {

        GridFSDBFile gridFile = gridFS.findOne(fileId);
        ObjectId bibId = (ObjectId) gridFile.getMetaData().get("bibId");

        if (!gridFile.getContentType().equals("application/pdf")) {
            throw new IllegalStateException("can only extract images from pdf documents");
        }

        Set<Integer> hashes = new HashSet<Integer>();

        PDDocument doc = PDDocument.load(gridFile.getInputStream());
        List<PDPage> pages = doc.getDocumentCatalog().getAllPages();
        for (PDPage page : pages) {
            PDResources resources = page.getResources();
            Map<String, PDXObject> xObjects = resources.getXObjects();
            for (Entry<String, PDXObject> e : xObjects.entrySet()) {
                if (PDXObjectImage.class.isAssignableFrom(e.getValue().getClass())) {
                    PDXObjectImage m = (PDXObjectImage) e.getValue();

                    int hash = getHash(m.getRGBImage());

                    if (m.getWidth() <= 20 || m.getHeight() <= 20) {
                        System.out.println("__ ignoring image for being too small");
                        continue;
                    }

                    if (hashes.contains(hash)) {
                        System.out.println("__ ignoring image for same hash");
                        continue;
                    }

                    String suffix = m.getSuffix();
                    final String contentType = URLConnection.guessContentTypeFromName("image." + suffix);
                    final String filename = e.getKey() + "." + suffix;

                    // thumbnail
                    BufferedImage t = Scalr.resize(m.getRGBImage(), Scalr.Method.SPEED, 200);
                    GridFSInputFile gridThumb = gridFS.createFile();
                    gridThumb.setContentType(contentType);
                    gridThumb.setFilename(filename);
                    gridThumb.setMetaData(new BasicDBObject("bibId", bibId)
                            .append("type", "thumbnail")
                            .append("pdfId", fileId)
                            .append("width", t.getWidth())
                            .append("height", t.getHeight()));
                    OutputStream outputStream = gridThumb.getOutputStream();
                    ImageIO.write(t, contentType, outputStream);
                    outputStream.close();
                    ObjectId thumbId = (ObjectId) gridThumb.getId();

                    // full img                    
                    GridFSInputFile gridImg = gridFS.createFile();
                    gridImg.setContentType(contentType);
                    gridImg.setFilename(filename);
                    gridImg.setMetaData(new BasicDBObject("bibId", bibId)
                            .append("type", "image")
                            .append("source", "pdf")
                            .append("pdfId", fileId)
                            .append("width", m.getWidth())
                            .append("height", m.getHeight())
                            .append("thumbId", thumbId));
                    outputStream = gridImg.getOutputStream();
                    m.write2OutputStream(outputStream);
                    outputStream.close();

                    // 
                    hashes.add(hash);
                }
            }
        }

        doc.close();
    }

    @GET
    @Produces("application/json")
    @Path("{id}/images")
    public Response getBibImages(final @PathParam("id") ObjectId bibId) {

        FilesResult<ImageFileInfo> r = new FilesResult<ImageFileInfo>();
        r.files = getImageList(bibId);
        return Response.ok(r).build();
    }

    public List<ImageFileInfo> getImageList(ObjectId bibId) {

        List<GridFSDBFile> find = gridFS.find(
                new BasicDBObject("metadata.bibId", bibId)
                .append("metadata.type", "image"),
                new BasicDBObject("metadata.pdfId", 1));

        List<ImageFileInfo> i = new LinkedList<ImageFileInfo>();
        for (GridFSDBFile f : find) {
            ImageFileInfo e = new ImageFileInfo();
            e.name = f.getFilename();
            e.size = f.getLength();
            e.id = f.getId().toString();
            e.contentType = f.getContentType();
            e.width = (Integer) f.getMetaData().get("width");
            e.height = (Integer) f.getMetaData().get("height");
            e.thumbnailId = ((ObjectId) f.getMetaData().get("thumbId")).toString();
            i.add(e);
        }

        return i;

    }

    @GET
    @Produces("application/json")
    @Path("{id}/files")
    public Response getBibFiles(final @PathParam("id") ObjectId bibId) {

        FilesResult<FileInfo> r = new FilesResult<FileInfo>();
        r.files = getFileInfo(bibId);
        return Response.ok(r).build();
    }

    public List<FileInfo> getFileInfo(ObjectId bibId) {

        List<GridFSDBFile> find = gridFS.find(
                new BasicDBObject("metadata.bibId", bibId)
                .append("metadata.type", "file"),
                new BasicDBObject("metadata.type", 1));

        List<FileInfo> i = new LinkedList<FileInfo>();
        for (GridFSDBFile f : find) {
            FileInfo e = new FileInfo();
            e.name = f.getFilename();
            e.size = f.getLength();
            e.id = f.getId().toString();
            e.contentType = f.getContentType();
            i.add(e);
        }

        return i;

    }

    private int getHash(BufferedImage rgbImage) {

        int hash = 0;
        Raster r = rgbImage.getRaster();
        int[] pixel = new int[4];
        for (int x = 0; x < rgbImage.getWidth(); x++) {
            for (int y = 0; y < rgbImage.getHeight(); y++) {
                hash += Arrays.hashCode(r.getPixel(x, y, pixel));
            }
        }
        return hash % 2 ^ 16;

    }

    private ObjectId addFile(final String filename, final String mimeType, final InputStream fileInputStream, final DBObject metaData) {

        ObjectId fileId;
        GridFSInputFile gridFile = gridFS.createFile(fileInputStream);
        gridFile.setFilename(filename);
        gridFile.setContentType(mimeType);
        gridFile.setMetaData(metaData);
        gridFile.save();
        if (mimeType.equals("application/pdf")) {
            try {
                extractImages((ObjectId) gridFile.get("_id"));
            } catch (IOException ex) {
                System.err.println("_error_extracting_images_" + ex.getMessage());
            }
        }
        fileId = (ObjectId) gridFile.getId();
        return fileId;

    }

    @GET
    @Path("{id}/actions/{action}")
    @Produces("application/json")
    public Response executeBibAction(
            final @PathParam("id") ObjectId id,
            final @PathParam("action") Actions action) {

        if (action == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        action.getHandler().setPublications(this).setPublication(getSingleBib(id)).action();

        return Response.ok().build();
    }

    public enum Actions {

        qpaper(new Action() {

            @Override
            public void action() {
                publications.lookForPdf(new ObjectId(publication.getStringId()), publication.getStringFields().get("title"));
            }

        });

        private Action handler;

        private Actions(Action actionHandler) {
            handler = actionHandler;
        }

        public Action getHandler() {
            return handler;
        }
    }

}
