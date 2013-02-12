package org.wiredwidgets.cow.ac.workflowviewer.nodes;

import java.awt.Image;
import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author MJRUSSELL
 */
public class ActivitesNode extends ActivityNode {

    public ActivitesNode(Activities activities) {
        super(activities, Children.create(new ActivitiesChildFactory(activities), false), Lookups.singleton(activities));
        setDisplayName(activities.getName());
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
        return ImageUtilities.loadImage("org/wiredwidgets/cow/ac/workflowviewer/nodes/icon/Icon_List.png", true);
    }
}
