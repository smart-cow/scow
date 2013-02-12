/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.ac.workflowviewer.nodes;

import java.awt.Image;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;
import org.wiredwidgets.cow.ac.util.server.BpmClientController;
import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.api.model.v2.SubProcess;
import org.wiredwidgets.cow.server.api.service.ProcessInstance;

/**
 *
 * @author FITZPATRICK
 */
public class SubProcessNode extends ActivityNode {
    public SubProcessNode(SubProcess subProcess) {
        super(subProcess, Children.create(new ActivitiesChildFactory((Activities) (BpmClientController.getInstance().getV2Process(subProcess.getSubProcessKey())).getActivity().getValue()), false), Lookups.singleton(subProcess));
        //getSubProcessActivities(subProcess);
        setDisplayName(subProcess.getName());
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
        return ImageUtilities.loadImage("org/wiredwidgets/cow/ac/workflowviewer/nodes/icon/Icon_SubProcess.png", true);
    }
    
    private ActivitiesChildFactory getSubProcessActivities(SubProcess subProcess){
        String subID = subProcess.getSubProcessKey();
        
        
        
        return new ActivitiesChildFactory((Activities) (BpmClientController.getInstance().getV2Process(subProcess.getSubProcessKey())).getActivity().getValue());
    }
}
