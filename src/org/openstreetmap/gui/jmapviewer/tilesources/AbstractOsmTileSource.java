package org.openstreetmap.gui.jmapviewer.tilesources;

import java.awt.Image;

import org.openstreetmap.gui.jmapviewer.Coordinate;

/**
 * Abstract clas for OSM Tile sources
 */
public abstract class AbstractOsmTileSource extends AbstractTMSTileSource {
    
    /**
     * The OSM attribution. Must be always in line with <a href="http://www.openstreetmap.org/copyright/en">http://www.openstreetmap.org/copyright/en</a>
     */
    public static final String DEFAULT_OSM_ATTRIBUTION = "\u00a9 OpenStreetMap contributors";
    
    /**
     * Constructs a new OSM tile source
     * @param name Source name as displayed in GUI
     * @param base_url Source URL
     */
    public AbstractOsmTileSource(String name, String base_url) {
        super(name, base_url);
    }

    public int getMaxZoom() {
        return 19;
    }

    
    public boolean requiresAttribution() {
        return true;
    }

    
    public String getAttributionText(int zoom, Coordinate topLeft, Coordinate botRight) {
        return DEFAULT_OSM_ATTRIBUTION;
    }

    
    public String getAttributionLinkURL() {
        return "http://openstreetmap.org/";
    }

    
    public Image getAttributionImage() {
        return null;
    }

    
    public String getAttributionImageURL() {
        return null;
    }

    
    public String getTermsOfUseText() {
        return null;
    }

    
    public String getTermsOfUseURL() {
        return "http://www.openstreetmap.org/copyright";
    }
}
