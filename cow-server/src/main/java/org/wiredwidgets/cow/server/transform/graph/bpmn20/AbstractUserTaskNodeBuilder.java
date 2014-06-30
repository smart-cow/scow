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

package org.wiredwidgets.cow.server.transform.graph.bpmn20;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.omg.spec.bpmn._20100524.model.ResourceAssignmentExpression;
import org.omg.spec.bpmn._20100524.model.TFormalExpression;
import org.omg.spec.bpmn._20100524.model.TPotentialOwner;
import org.omg.spec.bpmn._20100524.model.TUserTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.wiredwidgets.cow.server.api.model.v2.Task;
import org.wiredwidgets.cow.server.api.model.v2.Variable;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractUserTaskNodeBuilder<T extends Task> extends AbstractFlowNodeBuilder<T, TUserTask> {
	
	private final Logger log = LoggerFactory.getLogger(AbstractUserTaskNodeBuilder.class);
	
	public static String TASK_INPUT_VARIABLES_NAME = "Variables";
	public static String TASK_OUTPUT_VARIABLES_NAME = "Variables";	
	public static String TASK_VARIABLES_INFO = "_varsInfo";
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Override
	public void buildInternal(TUserTask t, T source, Bpmn20ProcessContext context) {           
        t.setName(source.getName());
              
        // standard JBPM inputs
        
        // A Map of ad-hoc variables is designated as input and output for every task.  
        // This supports the ability to use undeclared variables at runtime.
        addDataInputFromProperty(TASK_INPUT_VARIABLES_NAME, Bpmn20ProcessBuilder.VARIABLES_PROPERTY, t, context);
        addDataOutputFromProperty(TASK_OUTPUT_VARIABLES_NAME, Bpmn20ProcessBuilder.VARIABLES_PROPERTY, t, context);
        
        // Do we need the process instance name in the Task?
        // addDataInputFromProperty("ProcessInstanceName", Bpmn20ProcessBuilder.PROCESS_INSTANCE_NAME_PROPERTY, t, context);
        
        // JBPM maps the "Comment" value to the Description attribute of the Task object
        addDataInputFromExpression("Comment", source.getDescription(), t);
        
        // other variable inputs
        if (source.getVariables() != null) {
        	addInputOutputVariables(source.getVariables().getVariables(), t, context);
        	
        	String varsJson = getVarsMap(source);
        	
        	if (varsJson != null) {
        		addDataInputFromExpression(TASK_VARIABLES_INFO, varsJson, t);
        	}
 
        }
        
        // not used, leave it out for now
        // addDataInputFromExpression("Skippable", "false");
        
        // prepend the id to the name so we can map back to the key
        // when converting the task we will split it apart for display
        addDataInputFromExpression("TaskName", t.getId() + "/" + source.getName(), t);

        if (source.getCandidateGroups() != null) {
        	addDataInputFromExpression("GroupId", source.getCandidateGroups(), t);
        }   
        
        // handle assignment
        addPotentialOwner(t, source.getAssignee());
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

	@Override
	public TUserTask newNode() {
		return new TUserTask();
	}

	@Override
	public JAXBElement<TUserTask> createElement(TUserTask node) {
		return factory.createUserTask(node);
	}
	
	private String getVarsMap(T source) {
		Map<String, Map<String, Boolean>> varsMap = new HashMap<String, Map<String, Boolean>>();
        for (Variable var : source.getVariables().getVariables()) {
        	varsMap.put(var.getName(), new HashMap<String, Boolean>());
        	varsMap.get(var.getName()).put("output", var.isOutput());
        	varsMap.get(var.getName()).put("required", var.isRequired());
        	//varsMap.get(var.getName()).put("output", var.isModifiable();
        }
        try {
        	return objectMapper.writeValueAsString(varsMap);
        }
        catch (Exception e) {
        	log.error("Json error", e);
        	return null;
        }
		
	}
	
}
