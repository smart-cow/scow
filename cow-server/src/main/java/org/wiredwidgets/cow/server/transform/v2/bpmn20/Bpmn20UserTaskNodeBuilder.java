/**
 * Approved for Public Release: 10-4800. Distribution Unlimited. Copyright 2011
 * The MITRE Corporation, Licensed under the Apache License, Version 2.0 (the
 * "License");
 *
 * You may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.transform.v2.bpmn20;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.omg.spec.bpmn._20100524.model.Assignment;
import org.omg.spec.bpmn._20100524.model.DataInput;
import org.omg.spec.bpmn._20100524.model.DataInputAssociation;
import org.omg.spec.bpmn._20100524.model.DataOutput;
import org.omg.spec.bpmn._20100524.model.DataOutputAssociation;
import org.omg.spec.bpmn._20100524.model.InputSet;
import org.omg.spec.bpmn._20100524.model.IoSpecification;
import org.omg.spec.bpmn._20100524.model.OutputSet;
import org.omg.spec.bpmn._20100524.model.Property;
import org.omg.spec.bpmn._20100524.model.ResourceAssignmentExpression;
import org.omg.spec.bpmn._20100524.model.TDataAssociation;
import org.omg.spec.bpmn._20100524.model.TFormalExpression;
import org.omg.spec.bpmn._20100524.model.TPotentialOwner;
import org.omg.spec.bpmn._20100524.model.TUserTask;
import org.wiredwidgets.cow.server.api.model.v2.Task;
import org.wiredwidgets.cow.server.transform.v2.ProcessContext;

/**
 *
 * @author JKRANES
 */
public class Bpmn20UserTaskNodeBuilder extends Bpmn20ActivityNodeBuilder<TUserTask, Task> {
	
	public static String TASK_INPUT_VARIABLES_NAME = "Variables";
	public static String TASK_OUTPUT_VARIABLES_NAME = "Variables";
    

    // private static QName SOURCE_REF_QNAME = new QName("http://www.omg.org/spec/BPMN/20100524/MODEL","sourceRef");

    public Bpmn20UserTaskNodeBuilder(ProcessContext context, Task task) {
        super(context, new TUserTask(), task);
    }

    @Override
    protected void buildInternal() {

        Task source = getActivity();
        TUserTask t = getNode();
        t.setId(getContext().generateId("_")); // JBPM ID naming convention uses underscore prefix + sequence                
        t.setName(source.getName());
        source.setKey(t.getId());
           
        t.setIoSpecification(ioSpec);     
        ioSpec.getInputSets().add(inputSet);       
        ioSpec.getOutputSets().add(outputSet);
        
        // standard JBPM inputs
        Property varsProperty = getContext().getProcessVariable(Bpmn20ProcessBuilder.VARIABLES_PROPERTY);
        Property processNameProperty = getContext().getProcessVariable(Bpmn20ProcessBuilder.PROCESS_INSTANCE_NAME_PROPERTY);
        addDataInput(TASK_INPUT_VARIABLES_NAME, varsProperty);
        addDataOutput(TASK_OUTPUT_VARIABLES_NAME, varsProperty);
        addDataInput("ProcessInstanceName", processNameProperty);
        addDataInput("Comment", source.getDescription());
        addDataInput("Skippable", "false");
        
        // prepend the id to the name so we can map back to the key
        // when converting the task we will split it apart for display
        addDataInput("TaskName", t.getId() + "/" + source.getName());

        if (source.getCandidateGroups() != null) {
        	addDataInput("GroupId", source.getCandidateGroups());
        }   
        
        // handle assignment
        addPotentialOwner(t, source.getAssignee());

    }

    @Override
    protected JAXBElement<TUserTask> createNode() {
        return factory.createUserTask(getNode());
    }
    
    private void addGroupAssginment(TUserTask t, String groupName) {
    	
    }
      
    private void addPotentialOwner(TUserTask t, String ownerName) {
        TFormalExpression formalExpr = new TFormalExpression();
        formalExpr.getContent().add(ownerName);

        ResourceAssignmentExpression resourceExpr = new ResourceAssignmentExpression();
        resourceExpr.setExpression(factory.createFormalExpression(formalExpr));

        TPotentialOwner owner = new TPotentialOwner();
        owner.setResourceAssignmentExpression(resourceExpr);
        t.getResourceRoles().add(factory.createPotentialOwner(owner));       
    }
  

    
}
