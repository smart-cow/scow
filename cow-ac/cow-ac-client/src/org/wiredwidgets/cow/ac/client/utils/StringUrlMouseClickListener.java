package org.wiredwidgets.cow.ac.client.utils;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import org.apache.log4j.Logger;
import org.openide.awt.HtmlBrowser;
import org.wiredwidgets.cow.ac.client.ui.StatusBar;

/**
 * Listens for mouse clicks <b>within a table</b> and fires off handlers 
 * to open the url value contained in a clicked cell.  This was a fairly
 * hackish way to get clickable URLs into a table.
 *
 * @author RYANMILLER based on code from OCHONG
 * @see StringUrl
 * @see org.wiredwidgets.cow.ac.client.ui.TaskDetailsPanel
 */
public class StringUrlMouseClickListener implements MouseListener {

    static final Logger log = Logger.getLogger(StringUrlMouseClickListener.class);

    @Override
    public void mouseClicked(MouseEvent e) {
        JTable table = (JTable) e.getSource();
        Point point = e.getPoint();
        int clickedRow = table.rowAtPoint(point);
        int clickedCol = table.columnAtPoint(point);
        if ((clickedCol == -1) || (clickedRow == -1)) {
            return;
        }
        if (table.getValueAt(clickedRow, clickedCol) instanceof StringUrl) {
            // retrieve the link to go to
            String sUrl = ((StringUrl) table.getValueAt(clickedRow, clickedCol)).getUrl();
            URL url;
            try {
                url = new URL(sUrl);
                log.debug("showing url for " + url);
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                // prevent this from getting called again as it works it's way up the component stack
                e.consume();
            } catch (MalformedURLException ex) {
                StatusBar.getInstance().setStatusText(
                        "\"" + sUrl + "\" is not a valid URL. Cannot open in browser.");
                log.warn("\"" + sUrl + "\" is not a valid URL. Cannot open in browser.\n\n" +
                        ex.getLocalizedMessage());
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // do nothing
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // do nothing
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO - would ideally adjust how the link looked, such as underline
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO - restore how the link looked
    }
}
