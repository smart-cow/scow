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

import static org.wiredwidgets.cow.server.transform.graph.bpmn20.Bpmn20ProcessBuilder.PROCESS_EXIT_PROPERTY;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.jbpm.process.audit.JPAProcessInstanceDbLog;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.service.ProcessInstance;
import org.wiredwidgets.cow.server.api.service.Task;
import org.wiredwidgets.cow.server.api.service.Variable;
import org.wiredwidgets.cow.server.completion.Evaluator;
import org.wiredwidgets.cow.server.completion.EvaluatorFactory;
import org.wiredwidgets.cow.server.completion.ProcessInstanceInfo;
import org.wiredwidgets.cow.server.completion.graph.GraphCompletionEvaluator;
import org.wiredwidgets.cow.server.repo.ProcessInstanceLogRepository;
import org.wiredwidgets.cow.server.transform.graph.bpmn20.Bpmn20ProcessBuilder;


/**
 *
 * @author FITZPATRICK
 */
@Transactional
@Component
public class ProcessInstanceServiceImpl extends AbstractCowServiceImpl implements ProcessInstanceService {

    @Autowired
	ProcessInstanceLogRepository processInstanceLogRepo;
    
    @Autowired
    GraphCompletionEvaluator graphEvaluator;
    
    @Autowired
    ProcessService processService;
    
    @Autowired
    TaskService taskService;
    
    @Autowired
    EvaluatorFactory evaluatorFactory;
    
    @Autowired
    KnowledgeBase kbase;
       
    public static Logger log = Logger.getLogger(ProcessInstanceServiceImpl.class);
    private static TypeDescriptor JBPM_PROCESS_INSTANCE_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.drools.runtime.process.ProcessInstance.class));
    private static TypeDescriptor JBPM_PROCESS_INSTANCE_LOG_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(ProcessInstanceLog.class));
    //private static TypeDescriptor JBPM_HISTORY_PROCESS_INSTANCE_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.jbpm.api.history.HistoryProcessInstance.class));
    private static TypeDescriptor COW_PROCESS_INSTANCE_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(ProcessInstance.class));

  
    @Override
    public String executeProcess(ProcessInstance instance) {
    	
    	// get a reference to the process object
    	RuleFlowProcess process = (RuleFlowProcess)kbase.getProcess(instance.getProcessDefinitionKey());
    	
        Map<String, Object> vars = new HashMap<String, Object>();
        
        // map of variables that will be used to initialize the process instance
        Map<String, Object> processVars = new HashMap<String, Object>();
        
        // generic map of non declared variables
        Map<String, Object> genericVars = new HashMap<String, Object>();
        
        if (instance.getVariables() != null) {
            for (Variable variable : instance.getVariables().getVariables()) {
                vars.put(variable.getName(), variable.getValue());
            }
        }
        // COW-65 save history for all variables
        // org.jbpm.api.ProcessInstance pi = executionService.startProcessInstanceByKey(instance.getProcessDefinitionKey(), vars);
        
        // determine which variables are declared at the process level and which go into the generic map
        for (String key : vars.keySet()) {
        	if (process.getVariableScope().findVariable(key) != null) {
        		// this is a declared variable for the process definition
        		processVars.put(key, vars.get(key));
        	}
        	else {
        		// not declared, so it goes into the generic map
        		genericVars.put(key, vars.get(key));
        	}
        }
        
        // add the generic map into our input map, using a standard variable name declared in all COW processes
        processVars.put(Bpmn20ProcessBuilder.VARIABLES_PROPERTY, genericVars);
        
        if (instance.getName() != null) {
        	// standard variable declared for all COW processes to store the instance name
        	processVars.put(Bpmn20ProcessBuilder.PROCESS_INSTANCE_NAME_PROPERTY, instance.getName());
        }
                
        org.drools.runtime.process.ProcessInstance pi = kSession.startProcess(instance.getProcessDefinitionKey(), processVars);
        instance.setId(Long.toString(pi.getId()));
        /*
         * //create the process name as a history-tracked variable if
         * (instance.getName() != null) { //
         * executionService.createVariable(pi.getId(), "_name",
         * instance.getName(), true); vars.put("_name", instance.getName()); }          *
         * setVariables(pi.getId(), vars); // COW-65
         *
         * // add the instance id as a variable so it can be passed to a
         * subprocess // executionService.createVariable(pi.getId(), "_id",
         * pi.getId(), false);
         *
         * if (instance.getPriority() != null) {
         * updateProcessInstancePriority(instance.getPriority().intValue(), pi);
         * }
         */
        
        // construct a process instance ID in the required format
        String key = pi.getProcessId() + "." + Long.toString(pi.getId());
        instance.setKey(key);
        return key;
    }

    @Transactional(readOnly = true)
    @Override
    public ProcessInstance getProcessInstance(Long id) {
    	ProcessInstance procInstance = converter.convert(
    			JPAProcessInstanceDbLog.findProcessInstance(id), ProcessInstance.class);
    	return procInstance;
    }

    //@Transactional(readOnly = true)
    @Override
    public List<ProcessInstance> findAllProcessInstances() {
        return this.getCOWProcessInstances();
    }

    @Override
    public boolean deleteProcessInstance(Long id) {
    	org.drools.runtime.process.ProcessInstance instance = kSession.getProcessInstance(id);
    	if (instance == null) {
    		return false;
    	}
    	else {
	        try {
	            kSession.abortProcessInstance(id);
	            return true;
	        } catch (IllegalArgumentException e) {
	            log.error(e);
	            return false;
	        }
    	}
    }

    @Override
    public void deleteProcessInstancesByKey(String key) {
        List<ProcessInstanceLog> procList = 
        		JPAProcessInstanceDbLog.findActiveProcessInstances(key);
        for (ProcessInstanceLog pil : procList) {
            try {
                kSession.abortProcessInstance(pil.getProcessInstanceId());
            } catch (IllegalArgumentException e) {
                log.error(e);
            }
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProcessInstance> findProcessInstancesByKey(String key) {
        List<ProcessInstanceLog> processInstances = JPAProcessInstanceDbLog
        		.findActiveProcessInstances(key);
        return this.convertProcessInstanceLogs(processInstances);
    }

    @Override
    public boolean updateProcessInstance(ProcessInstance instance) {
        return false;//throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
	public void signalProcessInstance(long id, String signal, String value) {
    	kSession.signalEvent(signal, value, id);
    }

    @Override
    public ProcessInstance getProcessInstanceStatus(Long processInstanceId) {
		ProcessInstanceLog pil = JPAProcessInstanceDbLog.findProcessInstance(processInstanceId);
		
		String exitValue = getProcessInstanceVariable(processInstanceId, PROCESS_EXIT_PROPERTY);
		org.wiredwidgets.cow.server.api.model.v2.Process process = processService.getV2Process(pil.getProcessId());

		String instanceId = process.getKey() + "." + processInstanceId;
		
		ProcessInstanceInfo info = new ProcessInstanceInfo(
				taskService.getHistoryActivities(processInstanceId), 
				pil.getStatus(), 
				getProcessInstanceVariables(processInstanceId), 
				getNodeMap(processInstanceId));
		
		
		// execute the graph based evaluator.  this will populate the completion status
		// for activity nodes based on the NodeInstanceLog
		graphEvaluator.evaluate(process, processInstanceId);

		Evaluator evaluator = evaluatorFactory.getProcessEvaluator(instanceId, process, info);
		evaluator.evaluate();
		
		ProcessInstance pi = getProcessInstance(processInstanceId);
		pi.setProcess(process);
		pi.getStatusSummaries().addAll(info.getStatusSummary());
        return pi;
    }
    
    @Override
	public Map<String, Object> getProcessInstanceStatusGraph(Long processInstanceId) {
    	ProcessInstance pi = getProcessInstanceStatus(processInstanceId);
    	Process process = pi.getProcess();
    	List<Task> tasks = pi.getTasks();
    	return processService.getProcessGraph(process, tasks);
    }

    @Override
    public List<ProcessInstance> findAllHistoryProcessInstances() {
        return this.convertProcessInstanceLogs(processInstanceLogRepo.findByStatus(2));
    }

    @Override
    public List<ProcessInstance> findHistoryProcessInstances(String key, Date endedAfter, boolean ended) {
        return this.convertProcessInstanceLogs(findJbpmHistoryProcessInstances(key, endedAfter, ended));
    }
 
    @SuppressWarnings("unchecked")
	private List<ProcessInstance> convertProcessInstances(List<org.drools.runtime.process.ProcessInstance> source) {
        return (List<ProcessInstance>) converter.convert(source, JBPM_PROCESS_INSTANCE_LIST, COW_PROCESS_INSTANCE_LIST);
    }

    @SuppressWarnings("unchecked")
    private List<ProcessInstance> convertProcessInstanceLogs(List<ProcessInstanceLog> source) {
        return (List<ProcessInstance>) converter.convert(source, JBPM_PROCESS_INSTANCE_LOG_LIST, COW_PROCESS_INSTANCE_LIST);
    }

    private List<ProcessInstance> getCOWProcessInstances() {
        //Collection<org.drools.runtime.process.ProcessInstance> processColl = kSession.getProcessInstances();
        List<ProcessInstanceLog> allProcessInstances = JPAProcessInstanceDbLog.findProcessInstances();
        List<ProcessInstanceLog> activeProcessInstances = new ArrayList<ProcessInstanceLog>();
        
        for (ProcessInstanceLog processInstance: allProcessInstances){
            if (processInstance.getEnd() == null){
                activeProcessInstances.add(processInstance);
            } 
        }
        return this.convertProcessInstanceLogs(activeProcessInstances);

    }
    
    private List<ProcessInstanceLog> findJbpmHistoryProcessInstances(String key, Date endedAfter, boolean ended) {
        List<ProcessInstanceLog> instances = new ArrayList<ProcessInstanceLog>();

        if(key != null  && !key.trim().equals("")){
            if (endedAfter != null){
                instances.addAll(processInstanceLogRepo.findByProcessIdAndStatusAndEndAfter(key, 2, endedAfter));
            } else{
                instances.addAll(processInstanceLogRepo.findByProcessIdAndStatus(key, 2));
            }
        } else {
            if (endedAfter != null){
                instances.addAll(processInstanceLogRepo.findByStatusAndEndAfter(2, endedAfter));
            } else{
                instances.addAll(processInstanceLogRepo.findByStatus(2));                
            }
        }
        return instances;
    }
    
    private String getProcessInstanceVariable(Long id, String name) {
        List<VariableInstanceLog> vars = JPAProcessInstanceDbLog.findVariableInstances(id, name);
        String value = null;       
        if (vars != null && vars.size() > 0 ){
            value = vars.get(0).getValue();
        }    	
        return value;
    }
   
    private Map<String, String> getProcessInstanceVariables(Long id) {
    	Map<String, String> vars = new HashMap<String, String>();
    	List<VariableInstanceLog> logs = JPAProcessInstanceDbLog.findVariableInstances(id);
    	for (VariableInstanceLog log : logs) {
    		vars.put(log.getVariableId(), log.getValue());
    	}
    	return vars;
    }
    
    private Map<String, Set<NodeInstanceLog>> getNodeMap(Long processInstanceId) {
    	// get all node instances for the process instance Id and put them into a map
    	// where the map key is the unique node name and the value is a sorted set in descending date/time order
    	List<NodeInstanceLog> nodes = JPAProcessInstanceDbLog.findNodeInstances(processInstanceId);
    	Map<String, Set<NodeInstanceLog>> nodeMap = new HashMap<String, Set<NodeInstanceLog>>();
    	
    	for (NodeInstanceLog nil : nodes) {
    		String nodeName = nil.getNodeName();
    		if (nodeMap.get(nodeName) == null) {
    			// create a sorted set based on the Date
    			Set<NodeInstanceLog> nodeSet = new TreeSet<NodeInstanceLog>(
    					new Comparator<NodeInstanceLog>() {
    						@Override
    						public int compare(NodeInstanceLog o1, NodeInstanceLog o2) {
    							// descending order so most recent date/time is always first
    							return o2.getDate().compareTo(o1.getDate());
    						}
    					});
    			nodeMap.put(nodeName, nodeSet);
    		}
    		nodeMap.get(nodeName).add(nil);	
    	}
    	return nodeMap;
    }
    
}
