package org.wiredwidgets.cow.ac.workflowviewer.nodes;

import java.awt.Image;
import org.wiredwidgets.cow.server.api.model.v2.Task;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author MJRUSSELL
 */
public class TaskNode extends ActivityNode {

    private Task task;

    public TaskNode(Task task) {
        super(task, Children.LEAF, Lookups.singleton(task));
        this.task = task;
        setName(task.getName());
    }

    @Override
    public String getCandidateGroups() {
        String groupString = task.getCandidateGroups();
        return (groupString == null) ? "" : groupString;
    }

    @Override
    public String getAssignee() {
        String assigneeString = task.getAssignee();
        return (assigneeString == null) ? "" : assigneeString;
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/wiredwidgets/cow/ac/workflowviewer/nodes/icon/Icon_Task.png", true);
    }
}
