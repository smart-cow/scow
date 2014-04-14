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

package org.wiredwidgets.cow.server.convert;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.jbpm.task.Content;
import org.jbpm.task.utils.ContentMarshallerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.service.Task;
import org.wiredwidgets.cow.server.api.service.Variable;
import org.wiredwidgets.cow.server.api.service.Variables;
import org.wiredwidgets.cow.server.transform.graph.bpmn20.AbstractUserTaskNodeBuilder;
import org.wiredwidgets.cow.server.transform.graph.bpmn20.DecisionTaskNodeBuilder;
import org.wiredwidgets.cow.server.transform.graph.bpmn20.UserTaskNodeBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 *
 * @author FITZPATRICK
 */
@Component
public class JbpmTaskSummaryToSc2Task extends AbstractConverter<org.jbpm.task.query.TaskSummary, Task> {

    @Autowired(required=false)
    org.jbpm.task.TaskService taskClient;
    
    @Autowired
    ObjectMapper objectMapper;
    
    private final Logger log = LoggerFactory.getLogger(JbpmTaskSummaryToSc2Task.class);

    @Override
    public Task convert(org.jbpm.task.query.TaskSummary source) {
    	try {
    		return doConvert(source);
    	}
    	catch (Exception e) {
    		log.error("Error converting task ID {}", source.getId(), e);
    		return null;
    	}
    }
    
    private Task doConvert(org.jbpm.task.query.TaskSummary source) {

        Task target = new Task();

        target.setDescription(source.getDescription());

        if (source.getActualOwner() != null){
            target.setAssignee(source.getActualOwner().getId());
        }

        if (source.getCreatedOn() != null) {
            target.setCreateTime(convert(source.getCreatedOn(), XMLGregorianCalendar.class));
        }

        if (source.getExpirationTime() != null) {
            target.setDueDate(convert(source.getExpirationTime(), XMLGregorianCalendar.class));
        }
        target.setId(String.valueOf(source.getId()));
        
        if (source.getName() != null){
            String[] parts = source.getName().split("/");
            target.setActivityName(parts[0]); // corresponds to "key" in workflow
            if (parts.length == 2) {	
	            target.setName(parts[1]); // used for display to the user
            }
            else {
            	log.error("Expecting task name in [key]/[name] format, but was: {}", source.getName());
            	target.setName(parts[0]); // something is broken here, not sure what to do.
            }
        }
        
        target.setState(source.getStatus().name());
        
        target.setPriority(new Integer(source.getPriority()));
        target.setProcessInstanceId(source.getProcessId() + "." + Long.toString(source.getProcessInstanceId()));

        org.jbpm.task.Task task = taskClient.getTask(source.getId());
        if (task == null) {
        	log.error("Unable to find task ID {}", source.getId());
        	return null;
        }

        Content content = taskClient.getContent(task.getTaskData().getDocumentContentId());
        
        Map<String, Object> map = null;
        if (content != null) {
	         map = (Map<String, Object>) ContentMarshallerHelper.unmarshall(
	        		content.getContent(), null);  
        }
        else {
        	log.info("No content found for task ID: {} content ID {}", task.getId(), task.getTaskData().getDocumentContentId() );
        	map = new HashMap<String, Object>();
        }
        
        // add task outcomes using the "Options" variable from the task
        String optionsString = (String) map.get(DecisionTaskNodeBuilder.OPTIONS);
        if (optionsString != null) {     
	        String[] options = ( (String) map.get("Options") ).split(",");
	        target.getOutcomes().addAll(Arrays.asList(options));
        }
        
        // get variables info
        
        Map<String, Map<String, Boolean>> varsInfoMap = new HashMap<String, Map<String, Boolean>>();
        String varsJson = (String)map.get(AbstractUserTaskNodeBuilder.TASK_VARIABLES_INFO);
        if (varsJson != null && !varsJson.isEmpty()) {
	        try {
	        	varsInfoMap = objectMapper.readValue(varsJson, Map.class);
	        }
	        catch (Exception e) {
	        	log.error("Json parsing exception", e);
	        }
        }
        
        // get ad-hoc variables map
       
        Map<String, Object> contentMap = (Map<String, Object>) map.get(AbstractUserTaskNodeBuilder.TASK_INPUT_VARIABLES_NAME);
        if (contentMap != null) {
	        for (Entry<String, Object> entry : contentMap.entrySet()) {
	        	log.debug(entry.getKey() + "=" + entry.getValue());
	        	// ad hoc variables are not required and are modifiable
	        	addVariable(target, entry.getKey(), entry.getValue(), false, true);
	        }
        }
        else {
        	log.debug("No Content found for task");
        }
        
        // all other non system variables
        Set<String> systemVarNames = UserTaskNodeBuilder.getSystemVariableNames();
        for (String key : map.keySet()) {
        	if (! systemVarNames.contains(key)) {
        		log.debug("Additional var: {}", key);
        		boolean required = false;
        		boolean modifiable = true;
        		if (varsInfoMap.containsKey(key) && varsInfoMap.get(key).get("output").equals(Boolean.FALSE)) {
        			// this is an input only declared variable
        			modifiable = false;
        		}
        		if (varsInfoMap.containsKey(key) && varsInfoMap.get(key).get("required").equals(Boolean.TRUE)) {
        			required = true;
        		}
        		addVariable(target, key, map.get(key), required, modifiable);
        	}
        }
        return target;
    }    
    
    private void addVariable(Task task, String key, Object value, boolean required, boolean modifiable) {
        Variable var = new Variable();
        var.setName(key);
        // Support strings only.  Other types will cause ClassCastException
        try {
            var.setValue((String)value);
        } catch (ClassCastException e) {
            var.setValue("Variable type " + value.getClass().getName() + " is not supported");
        }    
        var.setModifiable(modifiable);
        var.setRequired(required);
        addVariable(task, var);
    }

    private void addVariable(Task task, Variable var) {
        if (task.getVariables() == null) {
            task.setVariables(new Variables());
        }
        task.getVariables().getVariables().add(var);
    }
    
    /*
     * Fix for COW-132
     * Tasks in parallel structures may cause sub-executions  with IDs in the form key.number.number.number
     * In this case we only want to look at the top level process execution.  
     */
    private String getTopLevelExecutionId(String executionId) {
        String[] parts = executionId.split("\\.");
        return parts[0] + "." + parts[1];
    }
}
