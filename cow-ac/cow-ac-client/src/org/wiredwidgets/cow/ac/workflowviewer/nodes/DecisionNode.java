package org.wiredwidgets.cow.ac.workflowviewer.nodes;

import java.awt.Image;
import org.wiredwidgets.cow.server.api.model.v2.Decision;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author MJRUSSELL
 */
public class DecisionNode extends ActivityNode {

    private Decision decision;

    public DecisionNode(Decision decision) {
        super(decision, Children.create(new DecisionChildFactory(decision.getOptions()), false), Lookups.singleton(decision));
        setDisplayName(decision.getName());
        this.decision = decision;
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        try {
            Property completionStateProp = new PropertySupport.Reflection(decision.getTask(), String.class, "getCompletionState", null);
            completionStateProp.setName("completion state");
            sheet.get(Sheet.PROPERTIES).put(completionStateProp);
        } catch (NoSuchMethodException ex) {
            //System.out.println(ex);
        }
        return sheet;
    }

    @Override
    public String getCandidateGroups() {
        String groupString = decision.getTask().getCandidateGroups();
        return (groupString == null) ? "" : groupString;
    }

    @Override
    public String getAssignee() {
        String assigneeString = decision.getTask().getAssignee();
        return (assigneeString == null) ? "" : assigneeString;
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/wiredwidgets/cow/ac/workflowviewer/nodes/icon/Icon_Decision.png", true);
    }
}
