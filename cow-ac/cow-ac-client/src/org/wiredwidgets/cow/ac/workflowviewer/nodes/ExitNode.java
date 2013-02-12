package org.wiredwidgets.cow.ac.workflowviewer.nodes;

import java.awt.Image;
import org.wiredwidgets.cow.server.api.model.v2.Exit;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author MJRUSSELL
 */
public class ExitNode extends ActivityNode {

    public ExitNode(Exit exit) {
        super(exit, Children.LEAF, Lookups.singleton(exit));
        setDisplayName(exit.getName());
    }

    @Override
    public String getCandidateGroups() {
        return "";
    }

    @Override
    public String getAssignee() {
        return "";
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/wiredwidgets/cow/ac/workflowviewer/nodes/icon/Icon_Exit.png", true);
    }
}
