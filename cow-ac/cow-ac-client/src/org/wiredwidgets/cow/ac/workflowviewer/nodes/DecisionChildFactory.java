package org.wiredwidgets.cow.ac.workflowviewer.nodes;

import java.util.List;
import org.wiredwidgets.cow.server.api.model.v2.Option;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author MJRUSSELL
 */
public class DecisionChildFactory extends ChildFactory<Option> {

    private List<Option> options;

    public DecisionChildFactory(List<Option> options) {
        this.options = options;
    }

    @Override
    protected boolean createKeys(List<Option> listToPopulate) {
        for (Option o : options) {
            listToPopulate.add(o);
        }
        return true;
    }

    @Override
    protected Node createNodeForKey(Option key) {
        return new OptionNode(key);
    }
}
