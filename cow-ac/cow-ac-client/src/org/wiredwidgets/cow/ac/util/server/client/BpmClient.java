package org.wiredwidgets.cow.ac.util.server.client;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openide.util.Exceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.wiredwidgets.cow.ac.util.server.BpmClientErrorHandler;
import org.wiredwidgets.cow.server.api.service.*;

/**
 * A client class which provides atomic REST-based calls to the BPM Server.
 *
 * @author MJRUSSELL code adapted from RYANMILLER
 * @see org.wiredwidgets.cow.ac.util.server.BpmClientController
 */
final public class BpmClient {

    // XXX
    private class GroupWithEquality extends Group {

        public GroupWithEquality(Group g) {
            this.id = g.getId();
            this.name = g.getName();
            this.type = g.getType();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof Group) {
                if (this.id.equals(((Group) obj).getId())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

    // XXX
    private class UserWithEquality extends User {

        public UserWithEquality(User u) {
            this.email = u.getEmail();
            this.firstName = u.getFirstName();
            this.id = u.getId();
            this.lastName = u.getLastName();
            this.memberships = u.getMemberships();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof User) {
                if (this.id.equals(((User) obj).getId())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        //XXX Using only the ID means that a map with both users and groups 
        // that had the same ID will collide
        // Ideally the Jaxb generated classes should be creating their own
        // equals and hash methods
        // Could you use the class name as part of the hash?
        public int hashCode() {
            return id.hashCode();
        }
    }
    static final Logger log = Logger.getLogger(BpmClient.class);
    /** The base url to use for all rest calls */
    private String baseURL;
    @Autowired
    private final RestTemplate restTemplate;

    /**
     * Constructs a service client based on the current login information
     * available in the COW application settings. If these values change, a new
     * client will need to be obtained. All REST server calls, however, use the
     * input arguments to the wrapped methods. This allows one set of login
     * information to be used to update any user's tasks-- a powerful but
     * dangerous feature, but it's the current implementation on the server
     * side.
     *
     * @param restTemplate
     */
    public BpmClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

        // note: this is wired in applicationContext.xml and shouldn't be needed,
        // but kept here for example
        //this.restTemplate.setErrorHandler(new BpmClientErrorHandler());
    }

    /**
     * Adjusts the base url prefixed to calls to the server. This will break
     * things if authentication is required since it does not leverage the
     * username or password information. Kept primarily for testing purposes.
     *
     * @param baseURL A new base url (e.g. "http://localhost.mitre.org:8080").
     * If null or empty, the base url is not changed.
     * @throws IllegalArgumentException baseURL is null or empty
     */
    public void setBaseURL(String baseURL) {
        if (baseURL == null || baseURL.equals("")) {
            throw new IllegalArgumentException("base url cannot be null or empty");
        }
        this.baseURL = baseURL;
    }

    /**
     * The base URL prefixed to server calls
     *
     * @return
     */
    public String getBaseURL() {
        return baseURL;
    }

    /**
     * The name of this bean. Used by the Spring framework and <b>referenced in
     * the applicationContext.xml</b>.
     *
     * @return The name of the bean
     */
    protected String getBeanName()
            throws RestClientException {
        return "bpmService";  // don't change this without updating applicationContext.xml
    }

    /**
     * Lists the groups on the server
     *
     * @return A groups object
     */
    public List<Group> getGroups()
            throws RestClientException {
        Groups groups = null;

        try {
            groups = restTemplate.getForObject(baseURL + "/groups", Groups.class);
        } catch (RestClientException e) {
            log.error("Error caught while getting groups");
            throw e;
        } catch (Exception e) {
            log.error("Error caught while getting groups: " + e.getLocalizedMessage());
        }

        List<Group> equalityGroups = new ArrayList<Group>();
        for (Group g : groups.getGroups()) {
            equalityGroups.add(new GroupWithEquality(g));
        }

        return equalityGroups;
    }

    /**
     * Lists the users on the server
     *
     * @return A Users object
     */
    public List<User> getListOfUsersOnServer()
            throws RestClientException {
        Users users = null;

        try {
            users = restTemplate.getForObject(baseURL + "/users", Users.class);
        } catch (RestClientException e) {
            log.error("Error caught while getting users");
            throw e;
        } catch (Exception e) {
            log.error("Error caught while getting users: " + e.getLocalizedMessage());
        }

        List<User> equalityUsers = new ArrayList<User>();
        for (User u : users.getUsers()) {
            equalityUsers.add(new UserWithEquality(u));
        }

        return equalityUsers;
    }

    /**
     * Gets all the Tasks assigned to a user. Based on the way the server is
     * currently implemented, it need not be the same user
     *
     * @param username Name of the user for which to retrieve tasks
     * @return The list of tasks assigned to the user. Null if username is blank
     * or null.
     */
    public List<Task> getTasksForUser(String username)
            throws RestClientException {
        List<Task> tasklist = null;

        if (username == null || username.equals("")) {
            return null;
        }

        try {
            Tasks tasks = restTemplate.getForObject(baseURL + "/tasks/active?assignee={user}", Tasks.class, username);
            tasklist = tasks.getTasks();
            log.debug("Returned " + tasklist.size() + " tasks for user " + username);
        } catch (RestClientException e) {
            log.error("Error caught while getting assigned tasks for user " + username);
            throw e;
        } catch (Exception e) {
            log.error("Error caught while getting assigned tasks for user " + username + ":\n" + e.getLocalizedMessage());
        }

        return tasklist;
    }

    /**
     * Gets all Tasks which might be assigned to the user based on their groups.
     *
     * @param username Name of the user for which to retrieve tasks
     * @return The list of tasks assigned to the user. Null if username is blank
     * or null.
     */
    public List<Task> getUnassignedTasksForUser(String username)
            throws RestClientException {
        List<Task> tasklist = null;

        if (username == null || username.equals("")) {
            return null;
        }

        try {
            Tasks tasks = restTemplate.getForObject(baseURL + "/tasks/active?candidate={user}", Tasks.class, username);
            tasklist = tasks.getTasks();

            log.debug("Returned " + tasklist.size() + " unassigned tasks available for user " + username);
        } catch (RestClientException e) {
            log.error("Error caught while getting unassigned tasks available for user " + username);
            throw e;
        } catch (Exception e) {
            log.error("Error caught while getting unassigned tasks available for user " + username + ": " + e.getLocalizedMessage());
        }

        return tasklist;
    }

    /**
     * TODO: Apply start and end ranges. Untested.
     *
     * @param username Name of the user for which to retrieve Task history
     * @return A list of HistoryTask objects defining the user's actions. Null
     * if username is blank or null.
     */
    public List<HistoryTask> getTaskHistoryForUser(String username)
            throws RestClientException {
        List<HistoryTask> taskhistory = null;

        if (username == null || username.equals("")) {
            return null;
        }

        try {
            HistoryTasks tasks = restTemplate.getForObject(baseURL + "/tasks/history?assignee={user}", HistoryTasks.class, username);
            taskhistory = tasks.getHistoryTasks();
            log.debug("Returned " + taskhistory.size() + " historical tasks for user " + username);
        } catch (RestClientException e) {
            log.error("Error caught while getting task  history for user " + username);
            throw e;
        } catch (Exception e) {
            log.error("Error caught while getting task history for user " + username + ": " + e.getLocalizedMessage());
        }

        return taskhistory;
    }

    /**
     * Instructs the server to assign the task to the active user
     *
     * @param task Task to assign
     * @param user User to which to assign the Task
     * @return success
     */
    public boolean claimTaskForUser(Task task, String user)
            throws RestClientException {
        try {
            restTemplate.postForLocation(
                    baseURL + "/tasks/active/{task_id}?assignee={user}",
                    "", // expect no response
                    task.getId(), user);
        } catch (RestClientException e) {
            log.error("Error while assigning task \"" + task.getId()
                    + "\" to user \"" + user);
            throw e;
        } catch (Exception e) {
            log.error("Error while assigning task \"" + task.getId()
                    + "\" to user \"" + user + "\": " + e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    /**
     * Sends an update to the server indicating a task has been completed. No
     * error checking is performed.
     *
     * @param task Task to complete
     * @param outcome The selected Task outcome. Optional, if a task has no
     * outcomes to choose from.
     * @param notes Optional notes (key -> value format) to store with the
     * <em>workflow</em> (not the task!)
     * @return Success
     */
    public boolean completeTask(Task task, String outcome, Map<String, String> notes)
            throws RestClientException {

        // TODO: would ideally check input and ensure outcome and the map values 
        // will play nicely as a rest call.

        // build the outcome portion of the request
        String outcomeString = "";
        if (outcome == null) {
            // TODO check that an outcome is provided if needed by the task
        } else {
            // TODO check that outcome is a valid option for the task
            outcomeString = "?outcome=" + outcome;
        }

        // build the notes portion of the request (no notes are ok)
        String notesString = "";
        if (notes != null) {
            for (Map.Entry<String, String> e : notes.entrySet()) {
                    notesString += "&var=" + e.getKey() + ":" + e.getValue();
            }
            // adjust the leading token if there was not an outcome string
            if (outcomeString.equals("")) {
                notesString = notesString.replaceFirst("&", "?");
            }
        }

        try {
            // note: don't use the string version of this call, or the 
            // ? and &s get encoded, so we construct the full string ourselves 
            // (new issue when moving from Spring 3.0.6 to 3.1.0)
            restTemplate.delete(baseURL + "/tasks/active/" + task.getId() + outcomeString + notesString);
            log.debug("Updated task record for task " + task.getId());
        } catch (RestClientException e) {
            log.error("Error while updating task \"" + task.getId());
            throw e;
        } catch (Exception e) {
            log.error("Error while updating task \"" + task.getId() + "\": " + e.getLocalizedMessage());
            return false;
        }

        return true;
    }

    /**
     * Lists the active processes (workflows) on the server
     *
     * @return a ProcessInstances object
     *
     */
    public ProcessInstances getActiveProcessInstances()
            throws RestClientException {
        ProcessInstances pris = null;

        try {
            pris = restTemplate.getForObject(baseURL + "/processInstances/active", ProcessInstances.class);
        } catch (RestClientException e) {
            log.error("Error caught while getting active processes instances");
            throw e;
        } catch (Exception e) {
            log.error("Error caught while getting active processes instances: " + e.getLocalizedMessage());
        }

        return pris;
    }

    /**
     * Returns a process object which will list the current state of all the
     * tasks in a process instance used to avoid having to split the fullId into
     * its id and ext components
     *
     * @param fullId the id.ext of the process
     * @return A process (version 2) object if it exists, otherwise null
     */
    public org.wiredwidgets.cow.server.api.service.ProcessInstance getActiveProcessInstanceStatus(String fullId)
            throws RestClientException {
        org.wiredwidgets.cow.server.api.service.ProcessInstance proc = null;

        try {
            proc = restTemplate.getForObject(baseURL + "/processInstances/active/{fullId}/status", org.wiredwidgets.cow.server.api.service.ProcessInstance.class, fullId);
        } catch (RestClientException e) {
            log.error("Error caught while getting active process status");
            throw e;
        } catch (Exception e) {
            log.error("Error caught while getting active process status: " + e.getLocalizedMessage());
        }

        return proc;
    }

    public ProcessInstances getActiveProcessInstancesByKey(String id)
            throws RestClientException {
        ProcessInstances procInst = null;

        try {
            procInst = restTemplate.getForObject(baseURL + "/processInstances/active/{id}", ProcessInstances.class, id);
        } catch (RestClientException e) {
            log.error("Error caught while getting active process status");
            throw e;
        } catch (Exception e) {
            log.error("Error caught while getting active process status: " + e.getLocalizedMessage());
        }
        
        return procInst;
    }
    
    public org.wiredwidgets.cow.server.api.model.v2.Process getV2Process(String key)
            throws RestClientException {
        org.wiredwidgets.cow.server.api.model.v2.Process proc = null;
        
        try {
            proc = restTemplate.getForObject(baseURL + "/processes/{key}?format=v2", org.wiredwidgets.cow.server.api.model.v2.Process.class, key);
        } catch (RestClientException e) {
            log.error("Error caught while getting active process status");
            throw e;
        } catch (Exception e) {
            log.error("Error caught while getting active process status: " + e.getLocalizedMessage());
        }
        
        return proc;
    }
    
    
    /**
     * Returns the history tasks for a given process
     *
     * @param id the id for the process instance
     * @param ext The ext for the process instance
     * @return A history tasks object
     */
    public HistoryActivities getHistoryTasksForProcess(String id, String ext)
            throws RestClientException {
        HistoryActivities ha = null;
        try {
            ha = restTemplate.getForObject(baseURL + "/processInstances/active/{id}.{ext}/activities", HistoryActivities.class, id, ext);
        } catch (RestClientException e) {
            log.error("Error caught while getting history tasks for process: " + id + "." + ext);
            throw e;
        } catch (Exception e) {
            log.error("Error caught while getting history tasks for process: " + id + "." + ext + ": " + e.getLocalizedMessage());
        }

        return ha;
    }

    public Task getTask(String taskId)
            throws RestClientException {
        Task task = null;
        try {
            task = restTemplate.getForObject(baseURL + "/tasks/active/{id}", Task.class, taskId);
        } catch (RestClientException e) {
            log.error("Error caught while getting task: " + taskId);
            throw e;
        } catch (Exception e) {
            log.error("Error caught while getting task: " + taskId + ": " + e.getLocalizedMessage());
        }

        return task;
    }

    /**
     *
     * Performs a simple call to the server to make sure a response is received,
     * thus validating the connection settings. If this doesn't work, exceptions
     * are thrown and the user is notified and can suspect subsequent calls to
     * fail.
     *
     * @return call success
     * @throws RestClientException
     */
    public boolean helloWorld()
            throws RestClientException {

        String resp = restTemplate.getForObject(baseURL + "/hello", String.class);

        log.debug("Called \"" + baseURL + "/hello\" and received back " + resp);

        if (!resp.equals("Hello")) {
            return false;
        }

        return true;
    }
}
