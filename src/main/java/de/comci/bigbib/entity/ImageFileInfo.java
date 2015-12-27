/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.comci.bigbib.entity;

/**
 *
 * @author Sebastian
 */
public class ImageFileInfo extends FileInfo {
    
    public Integer width;
    public Integer height;
    public String thumbnailId;

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public String getThumbnailId() {
        return thumbnailId;
    }
    
    
    
}
