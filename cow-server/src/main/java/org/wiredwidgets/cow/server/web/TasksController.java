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
package org.wiredwidgets.cow.server.web;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wiredwidgets.cow.server.api.service.HistoryTask;
import org.wiredwidgets.cow.server.api.service.HistoryTasks;
import org.wiredwidgets.cow.server.api.service.Participations;
import org.wiredwidgets.cow.server.api.service.Task;
import org.wiredwidgets.cow.server.api.service.Tasks;
import org.wiredwidgets.cow.server.listener.AmqpNotifier;
import org.wiredwidgets.cow.server.service.TaskService;
//import org.wiredwidgets.cow.server.rss.FeedFromTaskList;
// import org.wiredwidgets.cow.server.test.TestHumanVars;

/**
 * Handles REST API methods for the /tasks resource
 *
 * @author JKRANES
 */
@Controller
@RequestMapping("/tasks")
public class TasksController {

    @Autowired
    StatefulKnowledgeSession kSession;
    @Autowired
    TaskService taskService;
    @Autowired
    AmqpNotifier amqpNotifier;
    
    // @Autowired
    // protected MinaHTWorkItemHandler minaWorkItemHandler;
    
    static Logger log = Logger.getLogger(TasksController.class);

    /**
     * Create a new ad-hoc task, i.e. one not associated with any process
     * instance Note: ad-hoc tasks are considered experimental and may not
     * function as expected in all cases. The HTTP response Location header
     * provides the URL of the new task.
     *
     * @param task a task object in XML sent as the request body
     * @param request
     * @param response
     */
    @RequestMapping(method = RequestMethod.POST)
    public void createTask(@RequestBody Task task, HttpServletRequest request, HttpServletResponse response) {
        String id = this.taskService.createAdHocTask(task);
        response.setStatus(SC_CREATED); // 201
        response.setHeader("Location", request.getRequestURL() + "/active/" + id);
    }

    /**
     * Retrieve a single task by its ID
     *
     * @param id the task ID
     * @return the Task object as XML
     */
    @RequestMapping("/active/{id}")
    @ResponseBody
    public Task getTask(@PathVariable("id") String id, HttpServletResponse response) {
        
        try{            
        Task task = taskService.getTask(Long.valueOf(id));
        if (task == null) {
            response.setStatus(SC_NOT_FOUND);
            return null;
        } else {
            return task;
        }
        }catch(Exception e){
            log.error(e);
            response.setStatus(SC_NOT_FOUND);
            return null;
        }
    }

    /**
     * Mark a task as complete The choice of DELETE here is based on the fact
     * that this action causes the resource (i.e. task) to be removed from its
     * location at the specified URL. Once completed, the task will then appear
     * under the /tasks/history URI. Response: http 204 if success, 404 if the
     * task was not found (i.e. an invalid task ID or a task that was already
     * completed)
     *
     * @param id the task ID
     * @param outcome the outgoing transition for the completed task. Required
     * if the task has more than one possible outcome.
     * @param variables variable assignments for the completed task, in
     * name:value format. More than one instance of this parameter can be
     * provided (e.g. ?variable=name1:value1&variable=name2:value2 etc)
     * @param response
     */
    @RequestMapping(value = "/active/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public void completeTask(@PathVariable("id") String id, @RequestParam(value = "outcome", required = false) String outcome, @RequestParam(value = "var", required = false) String variables, HttpServletResponse response, HttpServletRequest request) throws IOException {
        // verify existence

        Task task = taskService.getTask(Long.valueOf(id));       

        if (task == null) {
            response.setStatus(SC_NOT_FOUND); // 404
            return;
        }
    	if (task.getAssignee() == null) {
    		response.sendError(SC_BAD_REQUEST, "Task is unassigned.  Cannot complete");
    		return;
    	}
        		
    	Map<String, Object> varMap = new HashMap<String, Object>();
        if (variables != null) {
            // Note: allowing Spring to create the array has some undesired behaviors in some cases.  For example
            // if the query string contains a comma, Spring treats it as multi-valued.
            // Since we don't want that, we instead use the underlying request object to get the array.
            String[] vars = request.getParameterValues("var");
            for (String variable : vars) {
                // variable is a string in the format name:value
                // Only split on the first ":" found; the value section may contain additional ":" tokens.
                String[] split = variable.split(":", 2);
                varMap.put(split[0], split[1]);
                log.debug(split[0] + "=" + split[1]);
            }
        }
        log.debug("Completing task: id=" + id + " outcome=" + outcome);
        log.debug("Vars: " + varMap);

        taskService.completeTask(Long.valueOf(id), task.getAssignee(), outcome, varMap);

        response.setStatus(SC_NO_CONTENT); // 204

            //Task t = taskService.getTask(id);
            amqpNotifier.amqpTaskPublish(task, "process", "TaskCompleted", id);
        
    }

    /**
     * Assign a task to a user, or return a task to unassigned status. A task
     * can only be assigned if is currently in an unassigned state, otherwise an
     * error will result. Note: the UI client should be prepared to gracefully
     * handle a race condition, as two users may try to take the same task at
     * nearly the same time: in this case, one will succeed and the other will
     * receive an error response. Response: http 204 or an error code.
     *
     * @param id the ID of the task
     * @param assignee the person taking the task. Use a blank value
     * (?assignee=) to return the task to unassigned status
     * @param response
     */
    @RequestMapping(value = "/active/{id}", method = RequestMethod.POST, params = "assignee")
    public void takeTask(@PathVariable("id") String id, @RequestParam("assignee") String assignee, HttpServletResponse response) {
        // a request with a blank query string, e.g. ?assignee=, results in an empty string value
        if (assignee.equals("")) {
            taskService.removeTaskAssignment(Long.valueOf(id));
        } else {
            taskService.takeTask(Long.valueOf(id), assignee);
        }
        response.setStatus(SC_NO_CONTENT); // 204

        Task task = taskService.getTask(Long.valueOf(id));
        amqpNotifier.amqpTaskPublish(task, "process", "TaskTaken", id);
    }

    /**
     * Updates an existing task with new properties. Specified properties will
     * be updated, others will be left with their previous values. This could be
     * used to update priority, due date, etc.
     *
     * @param id the task ID
     * @param task
     * @param response
     */
    @RequestMapping(value = "/active/{id}", method = RequestMethod.POST)
    public void updateTask(@PathVariable("id") String id, @RequestBody Task task, HttpServletResponse response) {
        // use ID from the URL
        
        if (task.getId() == null || !task.getId().equals(id)) {
            task.setId(id); 
        } 
        this.taskService.updateTask(task);
        response.setStatus(SC_NO_CONTENT);
       
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Retrieve all active tasks
     *
     * @return a Tasks object as XML
     */
    @RequestMapping("/active")
    @ResponseBody
    public Tasks getAllTasks() {
        Tasks tasks = new Tasks();
        try{        
        tasks.getTasks().addAll(taskService.findAllTasks()); 
        //return tasks;
        }catch(Exception e){
            log.error(e);
        }
        return tasks;
    }

    /**
     * Retrieve all assigned active tasks for a specified assignee
     *
     * @param assignee the user ID
     * @return a Tasks object as XML
     */
    @RequestMapping(value = "/active", params = "assignee")
    @ResponseBody
    public Tasks getTasksByAssignee(@RequestParam("assignee") String assignee) {
        Tasks tasks = new Tasks();
        try{        
        tasks.getTasks().addAll(taskService.findPersonalTasks(assignee));        
        }catch(Exception e){
            log.error(e);            
        }
        return tasks;
    }

    /**
     * Retrieve all assigned active tasks for a specified assignee in rss format
     *
     * @param assignee and format=rss specified as query parameters
     * @return response contains a string with rss feed
     */
    @RequestMapping(value = "/active", params = {"format=rss", "assignee"})
    @ResponseBody
    public String getTasksForRSS(@RequestParam("assignee") String assignee, HttpServletRequest request, HttpServletResponse response) {
        /*
         * FeedFromTaskList fList = new FeedFromTaskList(); String feed =
         * fList.buildFeedByAssignee(assignee,
         * request.getRequestURL().toString(), request.getQueryString(),
         * taskService); response.setContentType("application/xml;
         * charset=UTF-8"); return feed;
         */
        return "Not supported yet.";
    }

    /**
     * Retrieve all active unassigned tasks
     *
     * @return a Tasks object as XML
     */
    @RequestMapping(value = "/active", params = "unassigned=true")
    @ResponseBody
    public Tasks getUnassignedTasks() {
        Tasks tasks = new Tasks();
        try{        
            tasks.getTasks().addAll(taskService.findAllUnassignedTasks());
        //return tasks;
        }catch(Exception e){
            log.error(e);
        }
        return tasks;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Retrieve all active unassigned tasks for which a user is an eligible
     * candidate. This includes both tasks for which the user is directly a
     * candidate, via the candidateUser element in the process XML, or
     * indirectly, via the user's membership in a group, as indicated by a
     * candidateGroup element.
     *
     * @param candidate the user ID
     * @return a Tasks object as XML
     */
    @RequestMapping(value = "/active", params = "candidate")
    @ResponseBody
    public Tasks getUnassignedTasksByCandidate(@RequestParam("candidate") String candidate) {
        Tasks tasks = new Tasks();
        try{        
        tasks.getTasks().addAll(taskService.findGroupTasks(candidate));        
        }catch(Exception e){
            log.error(e);
        }
        return tasks;
    }

    /**
     * Retrieve all active tasks for the specified process instance ID
     *
     * @param processInstance the processInstance ID
     * @return a Tasks object as XML
     */
    @RequestMapping(value = "/active", params = "processInstance")
    @ResponseBody
    public Tasks getTasksByProcessInstance(@RequestParam("processInstance") String processInstance) {
        Tasks tasks = new Tasks();
        tasks.getTasks().addAll(taskService.findAllTasksByProcessInstance(Long.decode(processInstance)));
        return tasks;
    }

    /**
     * Retrieve all active tasks for the specified process key
     *
     * @param processKey the process key
     * @return a Tasks object as XML
     */
    @RequestMapping(value = "/active", params = "processKey")
    @ResponseBody
    public Tasks getTasksByProcessKey(@RequestParam("processKey") String processKey) {
        
         Tasks tasks = new Tasks();
         //tasks.getTasks().addAll(taskService.findAllTasksByProcessKey(processKey));
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
    public HistoryTask getHistoryTask(@PathVariable("id") String id, HttpServletResponse response) {
        /*
         * HistoryTask task = taskService.getHistoryTask(id); if (task == null)
         * { response.setStatus(HttpServletResponse.SC_NOT_FOUND); return null;
         * } else { return task; }
         */
        return new HistoryTask();//throw new UnsupportedOperationException("Not supported yet.");
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
         HistoryTasks tasks = new HistoryTasks();
         tasks.getHistoryTasks().addAll(taskService.getHistoryTasks(assignee, start, end)); 
         return tasks;
    }

    /**
     * 
     * @param process the process instance ID
     * @return
     */
    @RequestMapping(value = "/history", method = RequestMethod.GET, params = "process")
    @ResponseBody
    public HistoryTasks getHistoryTasks(@RequestParam(value = "process", required = true) String process) {       
         HistoryTasks tasks = new HistoryTasks();
         Long processInstanceId = Long.valueOf(process.split("\\.")[1]);
         tasks.getHistoryTasks().addAll(taskService.getHistoryTasks(processInstanceId));
         return tasks;
         
    }


    /*
     * NOTE: The /participations methods expose underlying JBPM functionality
     * for Participations, but the usefulness of the underlying feature is
     * questionable. For example, adding a user as 'owner' or 'candidate' for a
     * task does NOT cause that task to appear on the person's task list.
     */
    @RequestMapping(value = "/participations/{taskId}")
    @ResponseBody
    public Participations getParticipations(@PathVariable("taskId") String id) {
        /*
         * Participations p = new Participations();
         * p.getParticipations().addAll(this.taskService.getTaskParticipations(id));
         * return p;
         */
        return new Participations();//throw new UnsupportedOperationException("Not supported yet.");
    }

    @RequestMapping(value = "/participations/{taskId}", method = RequestMethod.POST, params = "group")
    public void addGroupParticipation(@PathVariable("taskId") String taskId, @RequestParam("group") String group, @RequestParam("type") String type, HttpServletResponse response) {
        /*
         * this.taskService.addTaskParticipatingGroup(taskId, group, type);*/
        response.setStatus(SC_NO_CONTENT); // 204
    }

    @RequestMapping(value = "/participations/{taskId}", method = RequestMethod.DELETE, params = "group")
    public void deleteGroupParticipation(@PathVariable("taskId") String taskId, @RequestParam("group") String group, @RequestParam("type") String type, HttpServletResponse response) {
        /*
         * this.taskService.removeTaskParticipatingGroup(taskId, group, type);*/
        response.setStatus(SC_NO_CONTENT); // 204
    }

    @RequestMapping(value = "/participations/{taskId}", method = RequestMethod.POST, params = "user")
    public void addUserParticipation(@PathVariable("taskId") String taskId, @RequestParam("user") String user, @RequestParam("type") String type, HttpServletResponse response) {
        /*
         * this.taskService.addTaskParticipatingUser(taskId, user, type);*/
        response.setStatus(SC_NO_CONTENT); // 204
    }

    @RequestMapping(value = "/participations/{taskId}", method = RequestMethod.DELETE, params = "user")
    public void deleteUserParticipation(@PathVariable("taskId") String taskId, @RequestParam("user") String user, @RequestParam("type") String type, HttpServletResponse response) {
        /*
         * this.taskService.removeTaskParticipatingUser(taskId, user, type);*/
        response.setStatus(SC_NO_CONTENT); // 204
    }

    @RequestMapping(value = "/orphaned")
    @ResponseBody
    public Tasks findOrphanedTasks() {
        /*
         * Tasks tasks = new Tasks();
         * tasks.getTasks().addAll(taskService.findOrphanedTasks()); return
         * tasks;
         */
        return new Tasks();//throw new UnsupportedOperationException("Not supported yet.");
    }
}
