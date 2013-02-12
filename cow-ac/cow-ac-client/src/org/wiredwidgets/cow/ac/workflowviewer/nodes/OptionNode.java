package org.wiredwidgets.cow.ac.workflowviewer.nodes;

import java.awt.Image;
import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.api.model.v2.Option;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author MJRUSSELL
 */
public class OptionNode extends ActivityNode {

    public OptionNode(Option o) {
        //Can there be option without activites below it???
        super(o.getActivity().getValue(), Children.create(new ActivitiesChildFactory((Activities) o.getActivity().getValue()), false), Lookups.singleton(o));
        setDisplayName(o.getName());
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/wiredwidgets/cow/ac/workflowviewer/nodes/icon/folder_closed.png", true);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage("org/wiredwidgets/cow/ac/workflowviewer/nodes/icon/folder_open.png", true);
    }

    @Override
    public String getCandidateGroups() {
        return "";
    }

    @Override
    public String getAssignee() {
        return "";
    }
}
