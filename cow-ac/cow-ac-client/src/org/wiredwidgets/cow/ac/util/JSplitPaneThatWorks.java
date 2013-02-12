package org.wiredwidgets.cow.ac.util;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

/**
 * A JSplitPane on which you can reasonably set a proportional divider. Must 
 * still be called at runtime once the JSplitPane has been populated.
 * 
 * @author git://gist.github.com/1021984.git
 * @see https://gist.github.com/1021984
 */
public class JSplitPaneThatWorks {

    public static JSplitPane fixDividerLocation(final JSplitPane splitter,
            final double proportion) 
    {
        if (splitter.isShowing()) {
            if (splitter.getWidth() > 0 && splitter.getHeight() > 0) {
                splitter.setDividerLocation(proportion);
            } else {
                splitter.addComponentListener(new ComponentAdapter() {

                    @Override
                    public void componentResized(ComponentEvent ce) {
                        splitter.removeComponentListener(this);
                        fixDividerLocation(splitter, proportion);
                    }
                });
            }
        } else {
            splitter.addHierarchyListener(new HierarchyListener() {

                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0
                            && splitter.isShowing()) {
                        splitter.removeHierarchyListener(this);
                        fixDividerLocation(splitter, proportion);
                    }
                }
            });
        }
        return splitter;
    }

}
