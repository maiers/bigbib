/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.comci.bigbib;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonProperty;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.jbibtex.Value;

/**
 *
 * @author Sebastian
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.NONE)
public class PeristentBibTexEntry extends BibTeXEntry {

    private ObjectId id;
    
    public PeristentBibTexEntry(Key type, Key key) {
        super(type, key);
    }
    
    static Map<String, Key> keyMapping = new HashMap<String, Key>();
    static {
        keyMapping.put("address", BibTeXEntry.KEY_ADDRESS);
        keyMapping.put("annote", BibTeXEntry.KEY_ANNOTE);
        keyMapping.put("author", BibTeXEntry.KEY_AUTHOR);
        keyMapping.put("booktitle", BibTeXEntry.KEY_BOOKTITLE);
        keyMapping.put("chapter", BibTeXEntry.KEY_CHAPTER);
        keyMapping.put("crossref", BibTeXEntry.KEY_CROSSREF);
        keyMapping.put("doi", BibTeXEntry.KEY_DOI);
        keyMapping.put("edition", BibTeXEntry.KEY_EDITION);
        keyMapping.put("editor", BibTeXEntry.KEY_EDITOR);
        keyMapping.put("eprint", BibTeXEntry.KEY_EPRINT);
        keyMapping.put("howpublished", BibTeXEntry.KEY_HOWPUBLISHED);
        keyMapping.put("institution", BibTeXEntry.KEY_INSTITUTION);
        keyMapping.put("journal", BibTeXEntry.KEY_JOURNAL);
        keyMapping.put("key", BibTeXEntry.KEY_KEY);
        keyMapping.put("month", BibTeXEntry.KEY_MONTH);
        keyMapping.put("note", BibTeXEntry.KEY_NOTE);
        keyMapping.put("number", BibTeXEntry.KEY_NUMBER);
        keyMapping.put("organization", BibTeXEntry.KEY_ORGANIZATION);
        keyMapping.put("pages", BibTeXEntry.KEY_PAGES);
        keyMapping.put("publisher", BibTeXEntry.KEY_PUBLISHER);
        keyMapping.put("school", BibTeXEntry.KEY_SCHOOL);
        keyMapping.put("series", BibTeXEntry.KEY_SERIES);
        keyMapping.put("title", BibTeXEntry.KEY_TITLE);
        keyMapping.put("type", BibTeXEntry.KEY_TYPE);
        keyMapping.put("url", BibTeXEntry.KEY_URL);
        keyMapping.put("volume", BibTeXEntry.KEY_VOLUME);
        keyMapping.put("year", BibTeXEntry.KEY_YEAR);
    }
    
    public PeristentBibTexEntry(DBObject persistentObject) {
        super(
            new Key((String) persistentObject.get("type")), 
            new Key((String) persistentObject.get("key"))
        );
        BasicDBObject fields = (BasicDBObject) persistentObject.get("fields");
        id =  (ObjectId) persistentObject.get("_id");
        for (String key : fields.keySet()) {
            if (keyMapping.containsKey(key)) {                
                this.addField(keyMapping.get(key), new StringValue(fields.getString(key), StringValue.Style.BRACED));
            } else {
                this.addField(new Key(key), new StringValue(fields.getString(key), StringValue.Style.BRACED));
            }
        }        
    }
    
    @JsonProperty("id")
    @XmlElement(name="id")
    public String getStringId() {
        return id.toString();
    }

    @JsonProperty("key")
    @XmlElement(name="key")
    public String getStringKey() {
        return super.getKey().getValue();
    }

    @JsonProperty("type")
    @XmlElement(name="type")
    public String getStringType() {
        return super.getType().getValue();
    }

    @JsonProperty("fields")
    @XmlElement(name="fields")
    public Map<String, String> getStringFields() {
        Map<String, String> fields = new HashMap<String, String>();
        for (Entry<Key,Value> e : getFields().entrySet()) {
            if (e.getKey() != null && e.getValue() != null) {
                fields.put(e.getKey().getValue(), e.getValue().toUserString());
            }
        }
        return fields;
    }

    @Override
    public String toString() {
        return String.format("[%s:%s] %s: %s (%s)", 
                this.getType(), 
                this.getKey(), 
                this.getField(KEY_AUTHOR).toUserString(), 
                this.getField(KEY_TITLE).toUserString(), 
                this.getField(KEY_YEAR).toUserString());
    }
    
}
