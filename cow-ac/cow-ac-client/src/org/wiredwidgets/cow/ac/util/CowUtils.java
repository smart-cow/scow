package org.wiredwidgets.cow.ac.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Enumeration;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

/**
 * Collection of static helper methods used throughout the COW application.
 * @author RYANMILLER
 */
public final class CowUtils {

    /**
     * private constructor to prevent anything but utility access calls.
     */
    private CowUtils() {
        // private constructor
    }

    /**
     * Utility method to return the selected radio button in a button group.
     * @return Selected JRadioButton or null if no button is selected.
     */
    public static JRadioButton getSelection(final ButtonGroup group) {
        for (Enumeration<?> e=group.getElements(); e.hasMoreElements(); ) {
            JRadioButton b = (JRadioButton)e.nextElement();
            if (b.getModel() == group.getSelection()) {
                return b;
            }
        }
        return null;
    }

    /**
     * Sets a component's location to the center of the screen according to the
     * default Toolkit's screen size.
     * @param comp the component whose location to set
     */
    public static void setLocationToCenterOfScreen(final Component comp) {
        final Dimension screenSize = getScreenSize();
        final Dimension frameSize = comp.getSize();

        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        comp.setLocation((screenSize.width - frameSize.width) / 2,
            (screenSize.height - frameSize.height) / 2);
    }

    /**
     * Sets the child component's location to the center of the parent's location
     * @param parent Parent component (may not be null)
     * @param child Child component (may not be null)
     */
    public static void setLocationToCenterOfComponent(final Component parent, final Component child)
    {
        Point parentPoint = parent.getLocation();
        Dimension parentDim = parent.getSize();
        Dimension childDim = child.getSize();
        child.setLocation(parentPoint.x + (parentDim.width - childDim.width)/2, parentPoint.y + (parentDim.height - childDim.height)/2);
    }

    /**
     * Grabs the size of the screen using the default Toolkit.
     * @return Size of the screen as a Dimension.
     */
    private static Dimension getScreenSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }
    
    /**
     * Checks whether or not a String is a URL according to a regular expression,
     * not as defined by the java class.
     *
     * @param url The String to check
     * @return true if it is a URL, otherwise false
     * @see http://stackoverflow.com/questions/163360/regular-expresion-to-match-urls-java
     */
    public static boolean isUrl(final String url) {
        // the giant regex from Jason's web implementation (not sure where he got it) was for javascript.  Rather 
        // than risk screwing up converting it, the following is from 
        // http://stackoverflow.com/questions/163360/regular-expresion-to-match-urls-java
        String urlregex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

        if (url.matches(urlregex)) {
            //System.out.println(url + " is a url");
            return true;
        }
        //System.out.println(url + " is not a url");
        return false;
    };
}
