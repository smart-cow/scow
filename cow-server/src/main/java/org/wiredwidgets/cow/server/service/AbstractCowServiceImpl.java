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
package org.wiredwidgets.cow.server.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.KnowledgeBase;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.workitem.wsht.GenericHTWorkItemHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;

/**
 *
 * @author FITZPATRICK
 */
public class AbstractCowServiceImpl {
	
		
    @Autowired
    protected KnowledgeBase kBase;
    
    @Autowired
    protected Jaxb2Marshaller marshaller;
    
    @Autowired
    protected ConversionService converter;
    
    @Autowired
    protected StatefulKnowledgeSession kSession;
    
    // @Autowired
    // protected org.jbpm.task.service.TaskClient taskClient;
    
   // @Autowired
    //protected HashMap<String,List<String>> userGroups;
    
    @Autowired
    protected GenericHTWorkItemHandler workItemHandler;
    
    //@Autowired
    //protected org.jbpm.task.service.TaskServiceSession jbpmTaskServiceSession;
    
    /*
     * Sets variables.  If a variable is new to this execution, make it permanent.
     */
    protected void setVariables(String executionId, Map<String, String> variables) {
        /*Set<String> varNames = executionService.getVariableNames(executionId);
        for (String key : variables.keySet()) {
            if (!varNames.contains(key)) {
                executionService.createVariable(executionId, key, variables.get(key), true);
            }
            else {
                executionService.setVariable(executionId, key, variables.get(key));
            }
        }*/
    }
}
