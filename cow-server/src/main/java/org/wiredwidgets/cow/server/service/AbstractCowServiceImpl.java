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
	
	protected @Value("${rem2.url}") String REM2_URL;
		
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
