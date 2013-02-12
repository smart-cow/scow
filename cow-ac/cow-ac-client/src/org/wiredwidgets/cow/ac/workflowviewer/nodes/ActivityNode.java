package org.wiredwidgets.cow.ac.workflowviewer.nodes;

import java.awt.Image;
import java.lang.reflect.InvocationTargetException;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.Exit;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;

/**
 *
 * @author MJRUSSELL
 */
public abstract class ActivityNode extends AbstractNode {

    private Activity activity;

    public ActivityNode(Activity activity, Children children, Lookup lookup) {
        super(children, lookup);
        this.activity = activity;
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();

        Property completionStateProp = new PropertySupport.ReadOnly<String>("completionStateProp", String.class, "Completion State", "") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return getCompletionState();
            }
        };

        Property descriptionProp = new PropertySupport.ReadOnly<String>("descriptionProp", String.class, "Description", "") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return getDescription();
            }
        };

        Property candidateGroupProp = new PropertySupport.ReadOnly<String>("candidateGroupsProp", String.class, "Group", "") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return getCandidateGroups();
            }
        };

        Property assigneeProp = new PropertySupport.ReadOnly<String>("assigneeProp", String.class, "Assigned User", "") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return getAssignee();
            }
        };

        completionStateProp.setValue("suppressCustomEditor", Boolean.TRUE);
        descriptionProp.setValue("suppressCustomEditor", Boolean.TRUE);
        candidateGroupProp.setValue("suppressCustomEditor", Boolean.TRUE);
        assigneeProp.setValue("suppressCustomEditor", Boolean.TRUE);

        set.put(completionStateProp);
        set.put(descriptionProp);
        set.put(candidateGroupProp);
        set.put(assigneeProp);

        sheet.put(set);
        return sheet;
    }

    public String getCompletionState() {
        String completion = activity.getCompletionState();
        if (completion.equals("open")) {
            return "In Progress";
        } else if (completion.equals("planned")) {
            return "Planned";
        } else if (completion.equals("contingent")) {
            return "Contingent";
        } else if (completion.equals("precluded")) {
            return "Precluded";
        }else if (completion.equals("completed")) {
            if (activity instanceof Exit) {
                return "Rejected/Invalid";
            } else {
                return "Completed/Approved";
            }
        } else {
            return "";
        }
    }

    public String getDescription() {
        return activity.getDescription();
    }

    abstract public String getCandidateGroups();

    abstract public String getAssignee();

    @Override
    abstract public Image getIcon(int type);

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
}
