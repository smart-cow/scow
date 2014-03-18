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
package org.wiredwidgets.cow.server.convert;

import static org.drools.runtime.process.ProcessInstance.STATE_ABORTED;
import static org.drools.runtime.process.ProcessInstance.STATE_ACTIVE;
import static org.drools.runtime.process.ProcessInstance.STATE_COMPLETED;
import static org.drools.runtime.process.ProcessInstance.STATE_PENDING;
import static org.drools.runtime.process.ProcessInstance.STATE_SUSPENDED;
import static org.wiredwidgets.cow.server.transform.graph.bpmn20.Bpmn20ProcessBuilder.PROCESS_INSTANCE_NAME_PROPERTY;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.definition.process.Process;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.audit.JPAProcessInstanceDbLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.task.Status;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.service.ProcessInstance;
import org.wiredwidgets.cow.server.api.service.Task;
import org.wiredwidgets.cow.server.api.service.Variable;
import org.wiredwidgets.cow.server.api.service.Variables;

/**
 * 
 * @author FITZPATRICK
 */
@Component
public class JbpmProcessInstanceLogToSc2ProcessInstance extends
		AbstractConverter<ProcessInstanceLog, ProcessInstance> {

	private static final Logger log = Logger
			.getLogger(JbpmProcessInstanceLogToSc2ProcessInstance.class);

	@Autowired(required = false)
	StatefulKnowledgeSession kSession;
	
	@Autowired(required=false)
	KnowledgeBase kbase;


	// This field is required for normal operation. It has been marked as not required so that
	// unit tests pass.
	@Autowired(required = false)
    org.jbpm.task.TaskService taskService;
	
    @Autowired
    protected ConversionService converter;

	@Override
	public ProcessInstance convert(ProcessInstanceLog source) {		
		ProcessInstance target = new ProcessInstance();
		target.setProcessDefinitionId(source.getProcessId());
		target.setKey(source.getProcessId());

		// for compatibility with REST API, preserve the JBPM 4.x convention
		// where the process instance ID = processID + "." + id
		target.setId(source.getProcessId() + "."
				+ Long.toString(source.getProcessInstanceId()));
		long parentInstanceId = source.getParentProcessInstanceId();
		if (parentInstanceId > 0) {
			ProcessInstanceLog parent = JPAProcessInstanceDbLog
					.findProcessInstance(parentInstanceId);
			target.setParentId(parent.getProcessId() + "." + parentInstanceId);
		}
		target.setStartTime(convert(source.getStart(),
				XMLGregorianCalendar.class));
		target.setEndTime(convert(source.getEnd(), XMLGregorianCalendar.class));

		switch (source.getStatus()) {
		case STATE_ABORTED:
			target.setState("aborted");
			break;
		case STATE_ACTIVE:
			target.setState("active");
			break;
		case STATE_COMPLETED:
			target.setState("completed");
			break;
		case STATE_PENDING:
			target.setState("pending");
			break;
		case STATE_SUSPENDED:
			target.setState("suspended");
			break;
		}

		// process instance name
		// WorkflowProcessInstance pi = (WorkflowProcessInstance)
		// kSession.getProcessInstance(source.getProcessInstanceId());
		target.setName(getVariable(source.getProcessInstanceId(),
				PROCESS_INSTANCE_NAME_PROPERTY));
		
		Process process = kbase.getProcess(source.getProcessId());
		if (process == null) {
			// if the process does not exist, trying to get the instance will
			// cause JBPM problems.
			log.error("No process definition for process: " + source.getProcessId());
		}
		else {
			addVariables(target, source.getProcessInstanceId());
		}
		
    	/*List<Task> tasks = taskService
    			.findAllTasksByProcessInstance(source.getProcessInstanceId());*/
		List<Status> status = Arrays.asList(Status.Ready);
		List<TaskSummary> taskSummaries = taskService
				.getTasksByStatusByProcessId(source.getProcessInstanceId(), status, "en-UK");
		
    	target.getTasks().addAll(convertTaskSummaries(taskSummaries));

		return target;
	}

	private String getVariable(Long id, String name) {
		List<VariableInstanceLog> vars = JPAProcessInstanceDbLog
			.findVariableInstances(id, name);

		String value = null;
		if (vars != null && vars.size() > 0) {
			value = vars.get(0).getValue();
		}
		return value;
	}

	private void addVariables(ProcessInstance target, long processInstanceId) {
		org.drools.runtime.process.ProcessInstance pi = null;
		try {
			pi = kSession.getProcessInstance(processInstanceId);
		} catch (Exception e) {
			// do nothing, pi will be null
		}
		if (pi == null) {
			log.error("Process instance not found: " + processInstanceId);
			return;
		}
		Variables vars = new Variables();
		if (pi instanceof WorkflowProcessInstanceImpl) {
			for (Map.Entry<String, Object> entry : ((WorkflowProcessInstanceImpl) pi)
					.getVariables().entrySet()) {
				Variable v = new Variable();
				v.setName(entry.getKey());
				v.setValue(entry.getValue().toString());
				vars.getVariables().add(v);
			}
		}
		if (vars.getVariables().size() > 0) {
			target.setVariables(vars);
		}
	}
	
	
    private static final TypeDescriptor JBPM_TASK_SUMMARY_LIST = TypeDescriptor
    		.collection(List.class, TypeDescriptor
    				.valueOf(org.jbpm.task.query.TaskSummary.class));
    private static final TypeDescriptor COW_TASK_LIST = TypeDescriptor
    		.collection(List.class, TypeDescriptor
    				.valueOf(Task.class));
	
	@SuppressWarnings("unchecked")
	private List<Task> convertTaskSummaries(List<TaskSummary> source) {
		return (List<Task>) converter.convert(source, JBPM_TASK_SUMMARY_LIST, COW_TASK_LIST);
	}

}
