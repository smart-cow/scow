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

package org.wiredwidgets.cow.server.web;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;
import org.wiredwidgets.cow.server.api.service.HistoryTask;
import org.wiredwidgets.cow.server.api.service.HistoryTasks;
import org.wiredwidgets.cow.server.api.service.Task;
import org.wiredwidgets.cow.server.api.service.Tasks;
import org.wiredwidgets.cow.server.service.TaskService;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * Handles REST API methods for the /tasks resource
 * @author JKRANES
 */
@Controller
@RequestMapping("/tasks")
public class TasksController extends CowServerController {

    @Autowired
    TaskService taskService;

    
    static Logger log = Logger.getLogger(TasksController.class);
    
    
    /**
     * Retrieve all active tasks
     *
     * @return a Tasks object
     */
    @RequestMapping(value = "", method = GET)
    @ResponseBody
    public Tasks getAllTasks() {   
        Tasks tasks = new Tasks();
        tasks.getTasks().addAll(taskService.findAllTasks());
        return tasks;      
    }
    
    
    /**
     * Retrieve a single task by its ID
     *
     * @param id the task ID
     * @return the Task object
     */    
    @RequestMapping(value = "/{id}", method = GET)
    @ResponseBody
    public ResponseEntity<Task> getTask(@PathVariable("id") long id) {
    	Task task = taskService.getTask(id);
    	return createGetResponse(task);
    }

    
    /**
     * Mark a task as complete The choice of DELETE here is based on the fact
     * that this action causes the resource (i.e. task) to be removed from its
     * location at the specified URL. Once completed, the task will then appear
     * under the /tasks/history URI. Response: http 204 if success, 404 if the
     * task was not found (i.e. an invalid task ID or a task that was already
     * completed), http 409 if task hasn't been assigned to a user yet
     *
     * @param id the task ID
     * @param outcome the outgoing transition for the completed task. Required
     * if the task has more than one possible outcome.
     * @param variables variable assignments for the completed task, in
     * name:value format. More than one instance of this parameter can be
     * provided (e.g. ?variable=name1:value1&variable=name2:value2 etc)
     */
    @RequestMapping(value = "/{id}", method = DELETE)
    @ResponseBody
    public ResponseEntity<?> completeTask(
    		@PathVariable("id") long id, 
    		@RequestParam(value = "outcome", required = false) String outcome, 
    		@RequestParam(value = "var", required = false) String[] variables) {
    	
    	Task task = taskService.getTask(id);
    	if (task == null) {
    		return notFound();
    	}
    	if (task.getAssignee() == null) {
    		return conflict(task);
    	}
    	
    	Map<String, String> varMap = getVarMap(variables);
    	log.debug("Completing task: id=" + id + " outcome=" + outcome);
        log.debug("Vars: " + varMap);
        
    	taskService.completeTask(id, task.getAssignee(), outcome, varMap);
    	
    	return noContent();
    }
    
    
    /**
     * Convert variables passed in name1:value1&variable=name2:value format to a Map
     * @param varPairs variable in name1:value1&variable=name2:value format
     * @return Map containing the variables
     */
    private static Map<String, String> getVarMap(String[] varPairs) {
    	Map<String, String> varMap = new HashMap<String, String>();
    	if (varPairs == null) {
    		return varMap;
    	}
    	for(String varPair : varPairs) {
            // variable is a string in the format name:value
            // Only split on the first ":" found; the value section may contain additional ":" tokens.
    		String[] split = varPair.split(":", 2);
			varMap.put(split[0], split[1]);
    	}
    	return varMap;
    }
    

    
    /**
     * Assign a task to a user.
     * @param id
     * @param assignee
     * @return 404 if no task with id, 403 if assignee is not allowed to take the task
     */
    @RequestMapping(value = "/{id}/take", method = POST, params = "assignee")
    @ResponseBody
    public ResponseEntity<Task> takeTask(
    		@PathVariable("id") long id, 
    		@RequestParam("assignee") String assignee) {
    	Task task = taskService.getTask(id);
    	if (task == null) {
    		return notFound();
    	}
    	
    	try {
    		taskService.takeTask(id, assignee);
        	task = taskService.getTask(id);
        	return ok(task);
    	}
    	catch (org.jbpm.task.service.PermissionDeniedException e) {
    		return forbidden();
    	}
    }
    
    
    
    /**
     * Update an existing task
     * @param id
     * @param task
     * @param uriBuilder
     * @return 404 if doesn't exist
     */
    @RequestMapping(value = "/{id}", method = PUT)
    @ResponseBody
    public ResponseEntity<Task> updateTask(
    		@PathVariable("id") long id, 
    		@RequestBody Task task,
    		UriComponentsBuilder uriBuilder) {
    	
    	task.setId(String.valueOf(id));
    	if (taskService.getTask(id) == null) {
    		notFound();
    	}
    	taskService.updateTask(task);
    	return ok(taskService.getTask(id));
    }


    /**
     * Retrieve all assigned active tasks for a specified assignee
     *
     * @param assignee the user ID
     * @return a Tasks object
     */
    @RequestMapping(value = "",	params = "assignee", method = GET)
    @ResponseBody
    public Tasks getTasksByAssignee(@RequestParam("assignee") String assignee) {
    	Tasks tasks = new Tasks();
        tasks.getTasks().addAll(taskService.findPersonalTasks(assignee));
        return tasks;   
    }
    
    
    /**
     * Get all tasks that haven't been assigned to a user yet.
     * @return
     */
    @RequestMapping(value = "/unassigned", method = RequestMethod.GET)
    @ResponseBody
    public Tasks getUnassignedTasks() {
    	Tasks tasks = new Tasks();
        tasks.getTasks().addAll(taskService.findAllUnassignedTasks());
        return tasks;   
    }
    
    /**
     * Retrieve all active unassigned tasks for which a user is an eligible
     * candidate. This includes both tasks for which the user is directly a
     * candidate, via the candidateUser element in the process XML, or
     * indirectly, via the user's membership in a group, as indicated by a
     * candidateGroup element.
     *
     * @param candidate the user ID
     * @return a Tasks object
     */
    @RequestMapping(value = "", params = "candidate", method = GET)
    @ResponseBody
    public Tasks getUnassignedTasksByCandidate(@RequestParam("candidate") String candidate) {
    	Tasks tasks = new Tasks();
        tasks.getTasks().addAll(taskService.findGroupTasks(candidate));
        return tasks; 
    }
    
    
    
    /**
     * Retrieve all active tasks for the specified process instance ID
     *
     * @param processInstance the processInstance ID
     * @return a Tasks object
     */
    @RequestMapping(value = "", params = "processInstance", method = GET)
    @ResponseBody
    public Tasks getTasksByProcessInstance(
    		@RequestParam("processInstance") long processInstanceId) {
    	       
        Tasks tasks = new Tasks();
        tasks.getTasks().addAll(taskService.findAllTasksByProcessInstance(processInstanceId));
        return tasks;
    }
    
    
    
    /**
     * Retrieve a single HistoryTask by its ID. Note that the intent use of this
     * method is to retrieve completed tasks, therefore the behavior in the case
     * of providing the ID of an active task should be considered to be
     * 'undefined'.
     *
     * @param the task ID
     * @return a HistoryTask object as XML
     */
    @RequestMapping("/history/{id}")
    @ResponseBody
    public ResponseEntity<HistoryTask> getHistoryTask(@PathVariable("id") long id) {    
         HistoryTask task = taskService.getHistoryTask(id); 
         return createGetResponse(task);
    }


    /**
     * Retrieves a set of HistoryTasks selected by various criteria. Parameters
     * are optional and will be applied if provided to narrow the set of tasks
     * returned. This method will retrieve completed tasks only. Note:
     * HistoryTasks and HistoryActivities are very similar, a key distinction
     * being that HistoryTasks includes ad-hoc tasks. If ad-hoc tasks are not
     * needed, in some cases HistoryActivities may be more useful.
     *
     * @param assignee the task assignee at the time of completion
     * @param start start date as YYYY-MM-DD
     * @param end end date as YYYY-MM-DD
     * @return a HistoryTasks object as XML
     * @see
     * ProcessInstancesController#getProcessInstanceActivities(java.lang.String,
     * java.lang.String, javax.servlet.http.HttpServletResponse)
     */
    @RequestMapping(value = "/history", method = RequestMethod.GET, params = "!process")
    @ResponseBody
    public HistoryTasks getHistoryTasks(@RequestParam(value = "assignee", required = false) String assignee, @RequestParam(value = "start", required = false) @DateTimeFormat(iso = ISO.DATE) Date start, @RequestParam(value = "end", required = false) @DateTimeFormat(iso = ISO.DATE) Date end) {
         /*HistoryTasks tasks = new HistoryTasks();
         tasks.getHistoryTasks().addAll(taskService.getHistoryTasks(assignee, start, end)); 
         return tasks;*/
        try{
            SimpleRetryPolicy retry = new SimpleRetryPolicy();
            retry.setMaxAttempts(50);
            RetryTemplate retryTemplate = new RetryTemplate();               
            retryTemplate.setRetryPolicy(retry);
            final String assign = assignee; 
            final Date s = start; 
            final Date e = end; 
            HistoryTasks result = retryTemplate.execute(new RetryCallback<HistoryTasks>() {                     
                public HistoryTasks doWithRetry(RetryContext context) { 
                    HistoryTasks tasks = new HistoryTasks();
                    tasks.getHistoryTasks().addAll(taskService.getHistoryTasks(assign, s, e));
                    return tasks;
                }                       
            });
            return result;
        }catch(Exception e){
            log.info("ERROR in getHistoryTasks = " + e);            
            log.error(e);
        }
        return new HistoryTasks();
    }

    /**
     * 
     * @param process the process instance ID
     * @return
     */
    @RequestMapping(value = "/history", method = RequestMethod.GET, params = "process")
    @ResponseBody
    public HistoryTasks getHistoryTasks2(@RequestParam(value = "process") String process) {
    	long id = convertProcessInstanceKeyToId(process);
    	
    	HistoryTasks tasks = new HistoryTasks();
    	tasks.getHistoryTasks().addAll(taskService.getHistoryTasks(id));
    	return tasks;
    }

    
    
    
/**************************************************************************************
	Methods below have been deprecated. They are either deprecated because their functionality
	is now at a new end point, or the feature was removed when updating JBPM version.
	If the method is now at new end point, the new method should be listed in the 
	Javadoc comment.

**************************************************************************************/
    
    
    
    /**
	 * @deprecated use {@link #getAllTasks()} instead
	 * @return
	 */
	@Deprecated
	@RequestMapping(value = "/active", method = GET)
	@ResponseBody
	public Tasks getAllTasksOld() {
		log.warn("Deprecated method called: getAllTasksOld()");
		return getAllTasks();
	}


	/**
	 * Create a new ad-hoc task, i.e. one not associated with any process
	 * instance Note: ad-hoc tasks are considered experimental and may not
	 * function as expected in all cases. The HTTP response Location header
	 * provides the URL of the new task.
	 *
	 * @param task a task object in XML sent as the request body
	 * @param task
	 * @param uriBuilder
	 * @deprecated AFAIK no one uses this
	 */
	@Deprecated
	@RequestMapping(method = POST)
	@ResponseBody
	public ResponseEntity<Task> createTask(@RequestBody Task task, 
			UriComponentsBuilder uriBuilder) {
		log.warn("Deprecated method called: createTask(Task, UriComponentsBuilder");
		String id = taskService.createAdHocTask(task);
		return getCreatedResponse("/tasks/active/{id}", id, uriBuilder, 
				taskService.getTask(Long.valueOf(id)));
	}


	/**
	 * 
	 * @param id
	 * @return
	 * @deprecated use {@link #getTask(long)} instead
	 */
	@Deprecated
	@RequestMapping(value = "/active/{id}", method = GET)
	@ResponseBody
	public ResponseEntity<Task> getTaskOld(@PathVariable("id") long id) {
		log.warn("Deprecated method called: getTaskOld(long)");
		return getTask(id);
	}


	/**
	 * 
	 * @param id
	 * @param outcome
	 * @param variables
	 * @return
	 * @deprecated use {@link #completeTask(long, String, String[])} instead
	 */
	@Deprecated
	@RequestMapping(value = "/active/{id}", method = DELETE)
	@ResponseBody
	public ResponseEntity<?> completeTaskOld(
			@PathVariable("id") long id, 
			@RequestParam(value = "outcome", required = false) String outcome, 
			@RequestParam(value = "var", required = false) String[] variables) {
		
		log.warn("Deprecated method called: completeTaskOld(long, String, String[])");
		return completeTask(id, outcome, variables);
	}


	/**
	 * 
	 * @param candidate
	 * @return
	 * @deprecated use {@link #getUnassignedTasksByCandidate(String)} instead
	 */
	@Deprecated
	@RequestMapping(value = "/active", params = "candidate", method = GET)
	@ResponseBody
	public Tasks getUnassignedTasksByCandidateOld(@RequestParam("candidate") String candidate) {
		log.warn("Deprecated method called: getUnassignedTasksByCandidateOld(String)");
		return getUnassignedTasksByCandidate(candidate);
	}


	/**
	 * 
	 * @param id
	 * @param assignee
	 * @return 
	 * @deprecated use {@link #takeTask(long, String)}
	 */
	@Deprecated
	@RequestMapping(value = "/active/{id}", method = POST, params = "assignee")
	@ResponseBody
	public ResponseEntity<Task> takeTaskOld(
			@PathVariable("id") long id, 
			@RequestParam("assignee") String assignee) {
		log.warn("Deprecated method called: takeTaskOld(long, String)");
		return takeTask(id, assignee);
	}


	/**
	 * Retrieve all active tasks for the specified process key
	 *
	 * @param processKey the process key
	 * @return a Tasks object as XML
	 */
	@Deprecated
	@RequestMapping(value = "/active", params = "processKey")
	@ResponseBody
	public ResponseEntity<?> getTasksByProcessKey(@RequestParam("processKey") String processKey) {
		log.warn("Deprecated method called: getTasksByProcessKey(String)");
	    /*
	     Tasks tasks = new Tasks();
	     //tasks.getTasks().addAll(taskService.findAllTasksByProcessKey(processKey));
	     return tasks;
	     */
		return notImplemented();
	}


	/**
	 * Updates an existing task with new properties. Specified properties will
	 * be updated, others will be left with their previous values. This could be
	 * used to update priority, due date, etc.
	 *
	 * @param id the task ID
	 * @param task
	 * @param response
	 * @deprecated use {@link #updateTask(long, Task, UriComponentsBuilder)}
	 */
	@Deprecated
	@RequestMapping(value = "/active/{id}", method = POST)
	@ResponseBody
	public ResponseEntity<Task> updateTaskPost(
			@PathVariable("id") long id, 
			@RequestBody Task task, 
			UriComponentsBuilder uriBuilder) {
		log.warn("Deprecated method called: updateTaskPost(long, Task, UriComponentsBuilder)");
		return updateTask(id, task, uriBuilder);
	}


	/**
	 * 
	 * @param processInstance
	 * @return
	 * @deprecated use {@link #getTasksByProcessInstance(long)} instead
	 */
	@Deprecated
	@RequestMapping(value = "/active", params = "processInstance", method = GET)
	@ResponseBody
	public Tasks getTasksByProcessInstanceOld(
			@RequestParam("processInstance") String processInstance) {
		log.warn("Deprecated method called: getTasksByProcessInstanceOld(String)");      
	    long id = convertProcessInstanceKeyToId(processInstance);
	    return getTasksByProcessInstance(id);
	}


	/**
	 * Retrieve all active unassigned tasks
	 *
	 * @return a Tasks object as XML
	 * @deprecated use {@link #getUnassignedTasks()}
	 */
	@Deprecated
	@RequestMapping(value = "/active", params = "unassigned=true")
	@ResponseBody
	public Tasks getUnassignedTasksActive() {
		log.warn("Deprecated method called: getUnassignedTasksActive()");
		return getUnassignedTasks();
	}


	/*
     * NOTE: The /participations methods expose underlying JBPM functionality
     * for Participations, but the usefulness of the underlying feature is
     * questionable. For example, adding a user as 'owner' or 'candidate' for a
     * task does NOT cause that task to appear on the person's task list.
     */
    @Deprecated
    @RequestMapping(value = "/participations/{taskId}")
    @ResponseBody
    public ResponseEntity<?> getParticipations(@PathVariable("taskId") String id) {
    	log.warn("Deprecated method called: getParticipations(String)");
        /*
         * Participations p = new Participations();
         * p.getParticipations().addAll(this.taskService.getTaskParticipations(id));
         * return p;
         */
        //return new Participations();//throw new UnsupportedOperationException("Not supported yet.");
    	return notImplemented();
    }


    /**
	 * 
	 * @param assignee
	 * @return
	 * @deprecated use {@link #getTasksByAssignee(String)} instead
	 */
	@Deprecated
	@RequestMapping(value =	"/active", params = "assignee", method = GET)
	@ResponseBody
	public Tasks getTasksByAssigneeOld(@RequestParam("assignee") String assignee) {
		log.warn("Deprecated method called: getTasksByAssigneeOld(String)");
		return getTasksByAssignee(assignee);
	}


	@Deprecated
    @RequestMapping(value = "/participations/{taskId}", method = RequestMethod.POST, 
    			params = "group")
    public ResponseEntity<?> addGroupParticipation(
    		@PathVariable("taskId") String taskId, 
    		@RequestParam("group") String group, 
    		@RequestParam("type") String type) {
    	log.warn("Deprecated method called: addGroupParticipation(String, String, String)");
        /*
         * this.taskService.addTaskParticipatingGroup(taskId, group, type);*/
        //response.setStatus(SC_NO_CONTENT); // 204
    	return notImplemented();
    }

    
    /**
	 * Retrieve all assigned active tasks for a specified assignee in rss format
	 *
	 * @param assignee and format=rss specified as query parameters
	 * @return response contains a string with rss feed
	 */
	@Deprecated
	@RequestMapping(value = "/active", params = {"format=rss", "assignee"})
	@ResponseBody
	public ResponseEntity<?> getTasksForRSS(@RequestParam("assignee") String assignee) {
		log.warn("Deprecated method called: getTasksForRSS(String)");
	    /*
	     * FeedFromTaskList fList = new FeedFromTaskList(); String feed =
	     * fList.buildFeedByAssignee(assignee,
	     * request.getRequestURL().toString(), request.getQueryString(),
	     * taskService); response.setContentType("application/xml;
	     * charset=UTF-8"); return feed;
	     */
	    return notImplemented();
	}


	@Deprecated
    @RequestMapping(value = "/participations/{taskId}", method = RequestMethod.DELETE, 
    		params = "group")
    public ResponseEntity<?> deleteGroupParticipation(
    		@PathVariable("taskId") String taskId, 
    		@RequestParam("group") String group, 
    		@RequestParam("type") String type) {
    	log.warn("Deprecated method called: deleteGroupParticipation(String, String, String)");
        /*
         * this.taskService.removeTaskParticipatingGroup(taskId, group, type);*/
        //response.setStatus(SC_NO_CONTENT); // 204
    	return notImplemented();
    }

    
    @Deprecated
    @RequestMapping(value = "/participations/{taskId}", method = RequestMethod.POST, 
    		params = "user")
    public ResponseEntity<?> addUserParticipation(
    		@PathVariable("taskId") String taskId, 
    		@RequestParam("user") String user, 
    		@RequestParam("type") String type) {
    	log.warn("Deprecated method called: addUserParticipation(String, String, String)");
        /*
         * this.taskService.addTaskParticipatingUser(taskId, user, type);*/
        //response.setStatus(SC_NO_CONTENT); // 204
    	return notImplemented();
    }

    
    @Deprecated
    @RequestMapping(value = "/participations/{taskId}", method = RequestMethod.DELETE, 
    		params = "user")
    public ResponseEntity<?> deleteUserParticipation(
    		@PathVariable("taskId") String taskId, 
    		@RequestParam("user") String user, 
    		@RequestParam("type") String type) {
    	log.warn("Deprecated method called: deleteUserParticipation(String, String, String)");
        /*
         * this.taskService.removeTaskParticipatingUser(taskId, user, type);*/
        //response.setStatus(SC_NO_CONTENT); // 204
    	return notImplemented();
    }
    
    
    @Deprecated
    @RequestMapping(value = "/orphaned")
    @ResponseBody
    public ResponseEntity<?> findOrphanedTasks() {
    	log.warn("Deprecated method called: findOrphanedTasks()");
        /*
         * Tasks tasks = new Tasks();
         * tasks.getTasks().addAll(taskService.findOrphanedTasks()); return
         * tasks;
         */
        //return new Tasks();//throw new UnsupportedOperationException("Not supported yet.");
    	return notImplemented();
    }
}
