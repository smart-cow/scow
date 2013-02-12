package org.wiredwidgets.cow.ac.workflowviewer.nodes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyEditorSupport;

/**
 *
 * @author MJRUSSELL
 */
public class ActivityPropertyEditor extends PropertyEditorSupport {

    private Color backgroundColor;

    public ActivityPropertyEditor(Color backgroundColor) {
        super();
        this.backgroundColor = backgroundColor;
    }

    public ActivityPropertyEditor() {
        super();
        this.backgroundColor = Color.WHITE;
    }

    @Override
    public String getAsText() {
        return (String) getValue();
    }

    @Override
    public void paintValue(Graphics g, Rectangle box) {
        g.setColor(backgroundColor);
        g.fillRect(box.x, box.y, box.width, box.height);
        g.setColor(Color.black);
        g.drawString(getAsText(), box.x + 5, box.y + 15);
//        if (Color.WHITE != backgroundColor) {
//            g.setColor(backgroundColor);
//            g.drawString(getAsText(), box.x + 5, box.y + 15);
//        }
    }

    @Override
    public boolean isPaintable() {
        return true;
    }
}
