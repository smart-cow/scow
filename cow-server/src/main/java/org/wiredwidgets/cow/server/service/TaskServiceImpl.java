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

import static org.wiredwidgets.cow.server.transform.v2.bpmn20.Bpmn20DecisionUserTaskNodeBuilder.DECISION_VAR_NAME;
import static org.wiredwidgets.cow.server.transform.v2.bpmn20.Bpmn20ProcessBuilder.VARIABLES_PROPERTY;
import static org.wiredwidgets.cow.server.transform.v2.bpmn20.Bpmn20UserTaskNodeBuilder.TASK_OUTPUT_VARIABLES_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityNotFoundException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.I18NText;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.PeopleAssignments;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskData;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.task.api.model.ContentData;
import org.kie.internal.task.api.model.Deadline;
import org.kie.internal.task.api.model.Deadlines;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.service.HistoryActivity;
import org.wiredwidgets.cow.server.api.service.HistoryTask;
import org.wiredwidgets.cow.server.api.service.Participation;
import org.wiredwidgets.cow.server.api.service.Task;
import org.wiredwidgets.cow.server.repo.TaskRepository;

import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion.User;
//import org.jbpm.task.service.local.LocalTaskService;

/**
 *
 * @author FITZPATRICK
 */
@Transactional
@Component
public class TaskServiceImpl extends AbstractCowServiceImpl implements TaskService {
	
	@Autowired
	TaskRepository taskRepo;
	
	@Autowired
	UsersService usersService;
	
	@Autowired
	RuntimeManager runtimeManager;
	
	private static Logger log = Logger.getLogger(TaskServiceImpl.class);

    //private static TypeDescriptor JBPM_PARTICIPATION_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.jbpm.api.task.Participation.class));
    private static TypeDescriptor COW_PARTICIPATION_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Participation.class));
    private static TypeDescriptor JBPM_TASK_SUMMARY_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.kie.api.task.model.TaskSummary.class));
    private static TypeDescriptor JBPM_TASK_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.kie.api.task.model.Task.class));
    private static TypeDescriptor COW_TASK_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Task.class));
    //private static TypeDescriptor JBPM_HISTORY_TASK_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.jbpm.api.history.HistoryTask.class));
    private static TypeDescriptor COW_HISTORY_TASK_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(HistoryTask.class));
    // private static TypeDescriptor JBPM_HISTORY_ACTIVITY_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.jbpm.api.history.HistoryActivityInstance.class));
    private static TypeDescriptor COW_HISTORY_ACTIVITY_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(HistoryActivity.class));

    @Transactional(readOnly = true)
    @Override
    public List<Task> findPersonalTasks(String assignee) {
        List<TaskSummary> tempTasks = new ArrayList<TaskSummary>();
        List<TaskSummary> tasks = new ArrayList<TaskSummary>();
        
        // first check to see if the user is valid, otherwise JBPM is not happy
        org.wiredwidgets.cow.server.api.service.User user = usersService.findUser(assignee);
        if (user == null) {
        	// not a user in the system, so cannot have any tasks!
        	return new ArrayList<Task>();
        }
        
		RuntimeEngine engine = runtimeManager.getRuntimeEngine(EmptyContext.get());
		KieSession ksession = engine.getKieSession();
		org.kie.api.task.TaskService taskClient = engine.getTaskService();

        //tempTasks.addAll(jbpmTaskService.getTasksAssignedAsPotentialOwner(assignee, "en-UK"));
        tempTasks.addAll(taskClient.getTasksAssignedAsPotentialOwner(assignee, "en-UK"));
        
       for (TaskSummary task : tempTasks){
            if (task.getStatus() == Status.Reserved && (task.getActualOwner() != null && task.getActualOwner().getId().equals(assignee))){
                tasks.add(task);
            }
        }
        
        return this.convertTaskSummarys(tasks);
    }

    @Override
    public String createAdHocTask(Task task) {
        org.kie.api.task.model.Task newTask = this.createOrUpdateTask(task);
        return Long.toString(newTask.getId());
    }

    @Transactional(readOnly = true)
    @Override
    public List<Task> findAllTasks() {
    	//List<org.kie.api.task.model.Task> tasks = (List<org.kie.api.task.model.Task>) jbpmTaskService.query("select t from Task t where t.taskData.status in ('Created', 'Ready', 'Reserved', 'InProgress')", Integer.MAX_VALUE,0);
        
    	// TODO: does this really produce the same results?
        List<org.kie.api.task.model.TaskSummary> tasks = getTaskService().getTasksByVariousFields(null, true);
        return this.convertTaskSummarys(tasks);
    }

    @Transactional(readOnly = true)
    @Override
    public Task getTask(Long id) {
    	try {
    		org.kie.api.task.model.Task task = getTaskService().getTaskById(id);
    		return converter.convert(task, Task.class);
    	}
    	catch (EntityNotFoundException e) {
    		log.error("Task with id: " + id + " not found.");
    		return null;
    	}
    }

    @Override
	@Transactional(readOnly = true)
    public HistoryTask getHistoryTask(Long id) {
    	// org.kie.api.task.model.Task task = taskRepo.findOne(id);
    	TaskImpl task = taskRepo.findOne(id);
    	return converter.convert(task, HistoryTask.class);
    }

    @Override
    public void completeTask(Long id, String assignee, String outcome, Map<String, ?> variables) {
    	// should be handled upstream in controller
    	assert(assignee != null);
    	
        log.debug(assignee + " starting task with ID: " + id);
        //org.kie.api.task.model.Task task = jbpmTaskService.getTask(id);
        
        org.kie.api.task.TaskService taskClient = getTaskService();
        
        org.kie.api.task.model.Task task = taskClient.getTaskById(id);
        
        // convert to COW task so we can verify the decision
        Task cowTask = converter.convert(task,  Task.class);
        
        if (cowTask.getOutcomes() != null && cowTask.getOutcomes().size() > 0) {
        	// This is a decision task!
        	if (outcome == null) {
        		throw new RuntimeException("ERROR: no decision provided for a Decision task");
        	}
        	if (!cowTask.getOutcomes().contains(outcome)) {
        		throw new RuntimeException("ERROR: decision value " + outcome + " is not a valid choice.");
        	}
        }
        
        //Content inputContent = jbpmTaskService.getContent(task.getTaskData().getDocumentContentId());
        Content inputContent = taskClient.getContentById(task.getTaskData().getDocumentContentId());
        
        Map<String, Object> inputMap = (Map<String, Object>) ContentMarshallerHelper.unmarshall(inputContent.getContent(), null);
        
        for (Map.Entry<String, Object> entry : inputMap.entrySet()){
            log.debug(entry.getKey() + " = " + entry.getValue());
        }
        
        Map<String, Object> outputMap = new HashMap<String, Object>();
        
        // put Outcome into the outputMap 
        // The InputMap contains a variable that tells us what key to use
        if (inputMap.get(DECISION_VAR_NAME) != null) {
        	log.debug("Decision outcome: " + outcome);
        	outputMap.put((String)inputMap.get(DECISION_VAR_NAME), outcome);
        }        
               
        Map<String, Object> outputVarsMap = new HashMap<String, Object>();   
        
        // NOTE: obtaining the map from the Task results in a copy of the map as of the
        // time when the task became available.  It's possible that in the meantime (e.g. due to
        // a parallel task) the map has been altered.  
        // Map<String, Object> inputVarsMap = (Map<String, Object>) inputMap.get(TASK_INPUT_VARIABLES_NAME);
              
        // So, instead, we get the current values directly from the process instance, rather than the values copied into the task
        
        Long processInstanceId = task.getTaskData().getProcessInstanceId();
        Map<String, Object> inputVarsMap = null;
        
        WorkflowProcessInstance pi = (WorkflowProcessInstance) kSession.getProcessInstance(processInstanceId);
        if (pi != null) {
        	inputVarsMap = (Map<String, Object>) pi.getVariable(VARIABLES_PROPERTY);
        }
        else {
        	log.error("ProcessInstance not found: " + processInstanceId);
        }
        
        if (inputVarsMap != null) {
        	// initialize the output map with the input values
        	log.debug("Copying input map: " + inputVarsMap);
        	outputVarsMap.putAll(inputVarsMap);
        }
        
        
        //Copy variables from cow task to output variables
        org.wiredwidgets.cow.server.api.service.Variables cowVariables = cowTask.getVariables();
        if (cowVariables != null) {
	        for(org.wiredwidgets.cow.server.api.service.Variable var : 
	        		cowVariables.getVariables()) {
	        	String varName = var.getName();
	        	if (inputMap.containsKey(varName)) {
	        		outputVarsMap.put(varName, inputMap.get(varName));
	        	}
	        	else {
	        		outputVarsMap.put(varName, var.getValue());
	        	}
	        }
        }
        
        if (variables != null && variables.size() > 0) {
        	log.debug("Adding variables: " + variables);
        	// update with any new or modified values
        	outputVarsMap.putAll(variables);
    	}
        
        if (outputVarsMap.size() > 0) {
        	log.debug("Adding map to output");
        	outputMap.put(TASK_OUTPUT_VARIABLES_NAME, outputVarsMap);   
        }
       
        // start the task
        if (task.getTaskData().getStatus().equals(Status.Reserved)) {
            // change status to InProgress
            log.debug("Starting task...");
            //jbpmTaskService.start(id, assignee); 
            taskClient.start(id, assignee); 
        }        
        
        // TODO: since we're passing the variables map further down, maybe we don't need to pass it here?  Test this.
        // ContentData contentData = ContentMarshallerHelper.marshal(outputMap, null);
        log.debug("Completing task...");
        //jbpmTaskService.complete(id, assignee, contentData);
        taskClient.complete(id, assignee, outputMap);
        
        // note that we have to pass the variables again.        
        kSession.getWorkItemManager().completeWorkItem(task.getTaskData().getWorkItemId(), outputMap);
    
    }

    @Transactional(readOnly = true)
    @Override
    public List<Task> findAllUnassignedTasks() {
        List<TaskSummary> tempTasks;
        List<TaskSummary> tasks = new ArrayList<TaskSummary>();
        
        //tempTasks = jbpmTaskService.getTasksAssignedAsPotentialOwner("Administrator", "en-UK");
        tempTasks = getTaskService().getTasksAssignedAsPotentialOwner("Administrator", "en-UK");
        
        for (TaskSummary task : tempTasks){
            if (task.getStatus() == Status.Ready){
                tasks.add(task);
            }
        }
        
        return this.convertTaskSummarys(tasks);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Task> findGroupTasks(String user) {
    	
        // first check to see if the user is valid, otherwise JBPM is not happy
        org.wiredwidgets.cow.server.api.service.User u = usersService.findUser(user);
        if (u == null) {
        	// not a user in the system, so cannot have any tasks!
        	return new ArrayList<Task>();
        }    	
    	
        List<TaskSummary> tempTasks = new ArrayList<TaskSummary>();
        List<TaskSummary> tasks = new ArrayList<TaskSummary>();
        
        //tempTasks.addAll(jbpmTaskService.getTasksAssignedAsPotentialOwner(user, "en-UK"));
        tempTasks.addAll(getTaskService().getTasksAssignedAsPotentialOwner(user, "en-UK"));
        
        for (TaskSummary task : tempTasks){
            if (task.getStatus() == Status.Ready){
                tasks.add(task);
            }
        }
                
        return this.convertTaskSummarys(tasks);
    }

    @Override
    public void takeTask(Long taskId, String userId) {
    	//jbpmTaskService.claim(taskId, userId);
        getTaskService().claim(taskId, userId);
    }
    
    @Override
    public void updateTask(Task task) {
        this.createOrUpdateTask(task);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Task> findAllTasksByProcessInstance(Long id) {
        //List<Status> status = Arrays.asList(Status.Completed, Status.Created, Status.Error, Status.Exited, Status.Failed, Status.InProgress, Status.Obsolete, Status.Ready, Status.Reserved, Status.Suspended);
    	List<Status> status = Arrays.asList(Status.Ready);
        //return this.convertTaskSummarys(jbpmTaskService.getTasksByStatusByProcessId(id, status, "en-UK"));
        return this.convertTaskSummarys(getTaskService().getTasksByStatusByProcessInstanceId(id, status, "en-UK"));
    }

    @Transactional(readOnly = true)
    @Override
    public List<Task> findAllTasksByProcessKey(Long id) {
        return new ArrayList<Task>();//throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addTaskParticipatingGroup(Long taskId, String groupId, String type) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addTaskParticipatingUser(Long taskId, String userId, String type) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Participation> getTaskParticipations(Long taskId) {
        return new ArrayList<Participation>();//throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeTaskParticipatingGroup(Long taskId, String groupId, String type) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeTaskParticipatingUser(Long taskId, String userId, String type) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeTaskAssignment(Long taskId) {

        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Transactional(readOnly = true)
    @Override
    public List<HistoryTask> getHistoryTasks(String assignee, Date startDate, Date endDate) {  	
    	return convertHistoryTasks(taskRepo.findByTaskDataActualOwnerAndTaskDataCompletedOnBetween(
    			new User(assignee), startDate, endDate));
    }

    @Transactional(readOnly = true)
    @Override
    public List<HistoryTask> getHistoryTasks(Long processInstanceId) {
        return convertHistoryTasks(taskRepo.findByTaskDataProcessInstanceId(processInstanceId));
    }

    @Transactional(readOnly = true)
    @Override
    public List<HistoryActivity> getHistoryActivities(Long processInstanceId) {
    	return convertHistoryActivities(taskRepo.findByTaskDataProcessInstanceId(processInstanceId));
    }

    @Transactional(readOnly = true)
    @Override
    public List<Task> findOrphanedTasks() {
    	// TODO: implement
        return new ArrayList<Task>();
    }

    @Transactional(readOnly = true)
    @Override
    public Activity getWorkflowActivity(String processInstanceId, String key) {
        Activity activity = null;
        return activity;//throw new UnsupportedOperationException("Not supported yet.");
    }

    private Date convert(XMLGregorianCalendar source) {
        return source.toGregorianCalendar().getTime();
    }

    /*
     * private List<Participation>
     * convertParticipations(List<org.jbpm.api.task.Participation> source) {
     * return (List<Participation>) converter.convert(source,
     * JBPM_PARTICIPATION_LIST, COW_PARTICIPATION_LIST); }
     */
    @SuppressWarnings("unchecked")
	private List<Task> convertTaskSummarys(List<org.kie.api.task.model.TaskSummary> source) {
        return (List<Task>) converter.convert(source, JBPM_TASK_SUMMARY_LIST, COW_TASK_LIST);
    }
    
    @SuppressWarnings("unchecked")
    private List<Task> convertTasks(List<org.kie.api.task.model.Task> source) {
        return (List<Task>) converter.convert(source, JBPM_TASK_LIST, COW_TASK_LIST);
    }
  
    @SuppressWarnings("unchecked")
	private List<HistoryTask> convertHistoryTasks(List<org.kie.api.task.model.Task> source) {
		return (List<HistoryTask>) converter.convert(source, JBPM_TASK_LIST, COW_HISTORY_TASK_LIST);
	}

    @SuppressWarnings("unchecked")
    private List<HistoryActivity> convertHistoryActivities(List<org.kie.api.task.model.Task> source) { 
    	return (List<HistoryActivity>) converter.convert(source, JBPM_TASK_LIST, COW_HISTORY_ACTIVITY_LIST);
    }
     
    
    //TODO: Check if you can update a task. Can you update task by just adding a task with the same ID?
    private org.kie.api.task.model.Task createOrUpdateTask(Task source) {

    	org.kie.api.task.model.Task target;
        boolean newTask = false;
        if (source.getId() == null) {
            newTask = true;
            target = new org.kie.api.task.model.Task();
        } else {
        	//target = jbpmTaskService.getTask(Long.valueOf(source.getId()));
                target = taskClient.getTask(Long.valueOf(source.getId()));
        }
        if (target == null) {
            return null;
        }
        if (source.getAssignee() != null) {
            PeopleAssignments pa = new PeopleAssignments();
            List<OrganizationalEntity> orgEnt = new ArrayList<OrganizationalEntity>();
            org.jbpm.task.User oe = new org.jbpm.task.User();
            oe.setId(source.getAssignee());
            pa.setTaskInitiator(oe);
            orgEnt.add(oe);
            pa.setPotentialOwners(orgEnt);
            target.setPeopleAssignments(pa);

        }
        if (source.getDescription() != null) {
            List<I18NText> desc = new ArrayList<I18NText>();
            desc.add(new I18NText("en-UK", source.getDescription()));
            target.setDescriptions(desc);
        }
        if (source.getDueDate() != null) {
            Deadlines deadlines = new Deadlines();
            List<Deadline> dls = new ArrayList<Deadline>();
            Deadline dl = new Deadline();
            dl.setDate(this.convert(source.getDueDate()));
            dls.add(dl);
            deadlines.setEndDeadlines(dls);
            target.setDeadlines(deadlines);
        }
        if (source.getName() != null) {
            List<I18NText> names = new ArrayList<I18NText>();
            names.add(new I18NText("en-UK", source.getName()));
            target.setNames(names);
        }
        if (source.getPriority() != null) {
            target.setPriority(source.getPriority());
        }

        TaskData td = new TaskData();
        target.setTaskData(td);
        /*
         * if (source.getProgress() != null) {
         * target.setProgress(source.getProgress()); }
         */

        // convert variables
        /*
         * if (source.getVariables() != null &&
         * source.getVariables().getVariables().size() > 0) { Map<String,
         * Object> variables = new HashMap<String, Object>(); for (Variable
         * variable : source.getVariables().getVariables()) {
         * variables.put(variable.getName(), variable.getValue()); }
         * this.taskService.setVariables(target.getId(), variables); }
         */

        if (newTask) {
            //BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
            //taskClient.addTask(target, null, addTaskResponseHandler);
        	//jbpmTaskService.addTask(target, null);
                taskClient.addTask(target, null);
        }

        return target;
    }
    
    private org.kie.api.task.TaskService getTaskService() {
		RuntimeEngine engine = runtimeManager.getRuntimeEngine(EmptyContext.get());
		return engine.getTaskService();
    }
    
    private KieSession getKieSession() {
		RuntimeEngine engine = runtimeManager.getRuntimeEngine(EmptyContext.get());
		return engine.getKieSession();
    }
}
