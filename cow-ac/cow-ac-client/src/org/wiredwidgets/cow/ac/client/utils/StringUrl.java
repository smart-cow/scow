package org.wiredwidgets.cow.ac.client.utils;

/**
 * A simple container class for two strings: one as a URL to a resource
 * and another as a string identifier for the resource.
 * @author ryanmiller
 */
public class StringUrl {
    
    private String text;
    private String url;

    /**
     * Create a StringUrl with an identifier and the url (link)
     * @param text A short identifier 
     * @param url  The url (link) to the resource as would normally be put into
     * Windows Explorer or a browser.
     */
    public StringUrl(String text, String url) {
        this.text = text;
        this.url = url;
    }
    
    /**
     * Returns the string identifier portion of the StringUrl.
     * @return identifier
     */
    public String getText() {
        return text;
    }
    
    /**
     * Returns the url portion of the StringUrl.
     * @return the url (or link)
     */
    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return text;
    }
}
