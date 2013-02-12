package org.wiredwidgets.cow.ac.workflowviewer.nodes;

import java.util.List;
import javax.xml.bind.JAXBElement;
import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.Decision;
import org.wiredwidgets.cow.server.api.model.v2.Exit;
import org.wiredwidgets.cow.server.api.model.v2.Loop;
import org.wiredwidgets.cow.server.api.model.v2.SubProcess;
import org.wiredwidgets.cow.server.api.model.v2.Task;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author MJRUSSELL
 */
public class ActivitiesChildFactory extends ChildFactory<Activity> {

    private Activities activities;

    public ActivitiesChildFactory(Activities activities) {
        this.activities = activities;
    }

    @Override
    protected Node createNodeForKey(Activity key) {
        if (key instanceof Activities) {
            return new ActivitesNode((Activities) key);
        } else if (key instanceof SubProcess) {
            return new SubProcessNode((SubProcess) key);
        } else if (key instanceof Loop) {
            return new LoopNode((Loop) key);
        } else if (key instanceof Decision) {
            return new DecisionNode((Decision) key);
        } else if (key instanceof Task) {
            return new TaskNode(((Task) key));
        } else if (key instanceof Exit) {
            return new ExitNode((Exit) key);
        } else {
            //shouldn't reach?
            return null;
        }
    }

    @Override
    protected boolean createKeys(List<Activity> list) {
        for (JAXBElement<? extends Activity> el : activities.getActivities()) {
            list.add(el.getValue());
        }
        return true;
    }
}
