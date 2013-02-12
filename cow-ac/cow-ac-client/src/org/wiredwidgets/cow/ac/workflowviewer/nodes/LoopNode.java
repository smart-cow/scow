package org.wiredwidgets.cow.ac.workflowviewer.nodes;

import java.awt.Image;
import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.api.model.v2.Loop;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author MJRUSSELL
 */
public class LoopNode extends ActivityNode {

    private Loop loop;

    public LoopNode(Loop loop) {
        super(loop, Children.create(new ActivitiesChildFactory((Activities) (loop.getActivity().getValue())), false), Lookups.singleton(loop));
        setDisplayName(loop.getName());
        this.loop = loop;
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        try {
            Property completionStateProp = new PropertySupport.Reflection(loop.getLoopTask(), String.class, "getCompletionState", null);
            completionStateProp.setName("completion state");
            sheet.get(Sheet.PROPERTIES).put(completionStateProp);
        } catch (NoSuchMethodException ex) {
            //System.out.println(ex);
        }
        return sheet;
    }

    @Override
    public String getCandidateGroups() {
        String groupString = loop.getLoopTask().getCandidateGroups();
        return (groupString == null) ? "" : groupString;
    }

    @Override
    public String getAssignee() {
        String assigneeString = loop.getLoopTask().getAssignee();
        return (assigneeString == null) ? "" : assigneeString;
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/wiredwidgets/cow/ac/workflowviewer/nodes/icon/Icon_Loop.png", true);
    }
}
