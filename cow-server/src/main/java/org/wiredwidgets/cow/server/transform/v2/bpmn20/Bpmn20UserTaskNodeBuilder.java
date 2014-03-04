/**
 * Approved for Public Release: 10-4800. Distribution Unlimited.
 * Copyright 2014 The MITRE Corporation,
 * Licensed under the Apache License,
 * Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.transform.v2.bpmn20;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import org.omg.spec.bpmn._20100524.model.ResourceAssignmentExpression;
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
    	setId();

        Task source = getActivity();
        TUserTask t = getNode();                
        t.setName(source.getName());
           
        t.setIoSpecification(ioSpec);     
        ioSpec.getInputSets().add(inputSet);       
        ioSpec.getOutputSets().add(outputSet);
        
        // standard JBPM inputs
        addDataInputFromProperty(TASK_INPUT_VARIABLES_NAME, Bpmn20ProcessBuilder.VARIABLES_PROPERTY);
        addDataOutputFromProperty(TASK_OUTPUT_VARIABLES_NAME, Bpmn20ProcessBuilder.VARIABLES_PROPERTY);
        addDataInputFromProperty("ProcessInstanceName", Bpmn20ProcessBuilder.PROCESS_INSTANCE_NAME_PROPERTY);
        addDataInputFromExpression("Comment", source.getDescription());
        
        // other variable inputs
        if (source.getVariables() != null) {
        	addInputOutputVariables(source.getVariables().getVariables());
        }
        
        // not used, leave it out for now
        // addDataInputFromExpression("Skippable", "false");
        
        // prepend the id to the name so we can map back to the key
        // when converting the task we will split it apart for display
        addDataInputFromExpression("TaskName", t.getId() + "/" + source.getName());

        if (source.getCandidateGroups() != null) {
        	addDataInputFromExpression("GroupId", source.getCandidateGroups());
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
    
    public static Set<String> getSystemVariableNames() {
    	Set<String> varNames = new HashSet<String>();
    	varNames.add(TASK_INPUT_VARIABLES_NAME);
    	varNames.add("ProcessInstanceName");
    	varNames.add("Options");
    	varNames.add("Comment");
    	varNames.add("TaskName");    	
    	varNames.add("ActorId");   
    	varNames.add("GroupId"); 
    	return varNames;
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
