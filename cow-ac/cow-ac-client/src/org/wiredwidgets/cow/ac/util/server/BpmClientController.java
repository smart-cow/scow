package org.wiredwidgets.cow.ac.util.server;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.apache.log4j.Logger;
import org.openide.util.NbPreferences;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.client.RestClientException;
import org.wiredwidgets.cow.ac.options.CowSettingsPanel;
import org.wiredwidgets.cow.ac.util.server.client.BpmClient;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.service.*;

/**
 * This singleton class provides a wrapper around the BpmClient to provide for
 * easy access of the rest calls for the business processing model server (COW
 * server) as well as a single occurrence of the
 * <code>BpmClient</code> class.
 * <p/>
 * The parameters for login, as well as the hostname of the server, are
 * retrieved from the COW application options.
 * <p/>
 * The {@link #initialize()} method must be called before calls to the server
 * can be made. However, the {@link #initialize()} method is called
 * automatically whenever the server connection settings are updated. This is
 * triggered registering as a {@link PreferenceChangeListener}. So, it should
 * only need to be called explicitly the very first time. If the updated
 * settings are invalid, subsequent calls for information from the server will
 * fail and will return empty results (to assist in error handling on the UI
 * side). Call {@link #isInitialized()} to see if the connection to the server
 * is good. Other error checking and result states are provided per the
 * individual methods.
 * <p/>
 * TODO: UI callback to notify the user of errors at this level rather than
 * checks on every user's call
 *
 * @author RYANMILLER
 * @see org.wiredwidgets.cow.ac.options.CowSettingsPanel;
 */
public class BpmClientController {

    static final Logger log = Logger.getLogger(BpmClientController.class);
    private static BpmClientController instance = new BpmClientController();
    /** the class used to make REST calls to the server */
    private BpmClient client;
    /** The base url to use for all rest calls */
    private String baseURL;
    /** Name of the current user for login credentials */
    private String user;
    /** Password for the current user for login credentials */
    private String password;
    /** indicates if the client has been properly initialized */
    private Boolean initialized = false;
    /** error string to inform user connection to server has not been initialized and
     * calls cannot be made */
    private String UNINITIALIZED_ERROR_MSG = "Controller is not initialized. "
            + "Update the server connection settings and try again.";
    private List<ServerConnectionEventListener> serverEventListeners = 
            new ArrayList<ServerConnectionEventListener>();

    private BpmClientController() {
        try {
            ApplicationContext applicationContext =
                    new ClassPathXmlApplicationContext("applicationContext.xml", this.getClass());

            client = applicationContext.getBean("bpmClient", BpmClient.class);

            // register to listen for updates from the application options
            Preferences pref = NbPreferences.forModule(CowSettingsPanel.class);
            pref.addPreferenceChangeListener(new PreferenceChangeListener() {

                @Override
                public void preferenceChange(PreferenceChangeEvent evt) {
                    // ignore changes to the notification server settings, trigger 
                    // on everything else
                    if (!evt.getKey().equals("cownotification")) {
                        boolean ret = initialize();
                        if (!ret) {
                            // notify listeners of the updated bad settings
                            for (ServerConnectionEventListener l : serverEventListeners) {
                                l.serverDown();
                            }                            
                        } else {
                            // notify listeners of the updates
                            for (ServerConnectionEventListener l : serverEventListeners) {
                                l.serverUpdated();
                            }
                        }
                    }
                }
            });

        } catch (Exception e) {
            log.error("Error in constructor: " + e.getLocalizedMessage());
            // TODO there was an error loading the spring libraries or dependencies
            // alert the user in a more friendly way
            // status bar?
        }
    }

    /**
     * Returns the instance of this class for use. Be sure that the
     * "initialize()" method has been called before first use.
     *
     * @return The instance of this controller class.
     */
    public static BpmClientController getInstance() {
        return instance;
    }

    private boolean updateServerConnectionInfo() {
        Preferences pref = NbPreferences.forModule(CowSettingsPanel.class);
        baseURL = pref.get("cowserver", "");
        user = pref.get("cowuser", "");
        password = pref.get("cowuserpassword", "");

        log.debug("Pulled new connection settings.");

        // server currently doesn't require a password. it also doesn't technically 
        // require a user, but this helps enforce the application's design 
        // of a single user at a time, and all tasks information, etc that gets
        // funneled out from this class relate to that user.
        if (baseURL.equals("") || user.equals("")) {
            log.warn("Access information was not sufficient. Cannot connect.");
            log.warn("server=[" + baseURL + "]" + "user=[" + user + "]");
            user = "";
            password = "";
            return false;
        }

        if (password.equals("")) {
            log.debug("No password provided.  Using an empty string.");
        }

        client.setBaseURL(baseURL);
        // note: username and password are not used for login. would need
        // to modify BpmClient or add additional code in this class.

        return true;
    }

    /**
     * Lists the users on the server
     *
     * @return The list of users.
     */
    public List<User> getUsersOnServer() {

        if (!isInitialized()) {
            return new ArrayList<User>();
        }
        List<User> users = client.getListOfUsersOnServer();

        if (users == null) {
            return new ArrayList<User>();
        }

        return users;
    }

    /**
     * @return The username used when making requests to the server (this is
     * pulled at object creation time from the COW Application Settings.
     */
    public String getUser() {
        return user;
    }

    /**
     * Gets all Tasks which might be assigned to the user based on their groups.
     *
     * @param username Name of the user for which to retrieve tasks
     * @return The list of tasks assigned to the user.
     */
    public List<Task> getUnassignedTasksForUser(String username) {
        if (!isInitialized()) {
            return new ArrayList<Task>();
        }

        List<Task> tasks = client.getUnassignedTasksForUser(username);

        if (tasks == null) {
            return new ArrayList<Task>();
        }

        return tasks;
    }

    /**
     * Returns the task specified by the task id
     *
     * @param taskId the id of the task to get
     * @return a Task object. will return null if there is no task with that id
     */
    public Task getTask(String taskId) {
        if (!isInitialized()) {
            return null;
        }

        return client.getTask(taskId);
    }

    /**
     * Gets all the Tasks assigned to a user.
     *
     * @param username Name of the user for which to retrieve tasks
     * @return The list of tasks assigned to the user.
     */
    public List<Task> getTasksForUser(String username) {
        if (!isInitialized()) {
            return new ArrayList<Task>();
        }

        List<Task> tasks = client.getTasksForUser(username);
        if (tasks == null) {
            return new ArrayList<Task>();
        }

        return tasks;
    }

    /**
     * Returns the history tasks for a given process
     *
     * @param id the id for the process instance
     * @param ext The ext for the process instance
     * @return the history tasks object for the process with the provided id.
     */
    public HistoryActivities getHistoryTasksForProcess(String id, String ext) {
        if (!isInitialized()) {
            return new HistoryActivities();
        }

        HistoryActivities his = client.getHistoryTasksForProcess(id, ext);

        if (his == null) {
            return new HistoryActivities();
        }

        return his;
    }

    /**
     * Lists the groups on the server
     *
     * @return A groups object
     */
    public List<Group> getGroupsOnServer() {
        if (!isInitialized()) {
            return new ArrayList<Group>();
        }

        List<Group> groups = client.getGroups();

        if (groups == null) {
            return new ArrayList<Group>();
        }

        return groups;
    }

    /**
     * Lists the active processes (workflows) on the server
     *
     * @return a ProcessInstances object
     */
    public ProcessInstances getActiveProcessInstances() {
        if (!isInitialized()) {
            return new ProcessInstances();
        }

        ProcessInstances pi = client.getActiveProcessInstances();

        if (pi == null) {
            return new ProcessInstances();
        }

        return pi;
    }

    /**
     * Returns a process object which will list the current state of all the
     * tasks in a process instance used to avoid having to split the fullId into
     * its id and ext components
     *
     * @param fullId the id.ext of the process
     * @return A process (version 2) object if it exists for the id, otherwise
     * null
     */
    public ProcessInstance getActiveProcessInstanceStatus(String fullId) {
        if (!isInitialized()) {
            return null;
        }
        
        return client.getActiveProcessInstanceStatus(fullId);
    }
    
    public ProcessInstances getActiveProcessInstancesByKey(String id){
        if (!isInitialized()) {
            return null;
        }
        
        return client.getActiveProcessInstancesByKey(id);
    }

    public Process getV2Process(String key){
        if (!isInitialized()) {
            return null;
        }
        
        return client.getV2Process(key);
    }
    
    /**
     * Instructs the server to assign the task to the active user
     *
     * @param task Task to assign
     * @param user User to which to assign the Task
     * @return success
     */
    public boolean claimTaskForUser(Task task, String user) {
        if (!isInitialized()) {
            return false;
        }
        return client.claimTaskForUser(task, user);
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
     * @return success
     */
    public boolean completeTask(Task task, String outcome, Map<String, String> notes) {
        if (!isInitialized()) {
            return false;
        }
        return client.completeTask(task, outcome, notes);
    }

    /**
     * Initializes the controller for use. Multiple calls to this method are not
     * harmful, but do invoke a test access call to the server.
     *
     * @return controller successfully initialized and calls can be made
     * @see #checkInitialized()
     */
    public boolean initialize() {

        // update the connection information with the latest values
        initialized = updateServerConnectionInfo();

        if (!initialized) {
            log.debug("Server connection settings were invalid.");
            return false;
        }

        // try a "hello world" connection to the server to make sure the 
        // settings are good
        try {
            if (!client.helloWorld()) {
                log.warn("Hello world test to the server returned a response, "
                        + "but it was not the expected response. Did the server version change?");
                // something fishy in the response, but we can still connect, so will proceed
            }
        } catch (RestClientException ex) {
            log.debug("Caught RestClientException:\n" + ex.getLocalizedMessage());
            log.error("Client-server connection has not been initialized");
            initialized = false;
            return false;
        } catch (Exception ex) {
            log.debug("Caught some error while testing server connection:\n" + ex.getLocalizedMessage());
            log.error("Client-server connection has not been initialized");
            initialized = false;
            return false;
        }

        log.info("Client-server connection initialized with lastest settings.");

        return initialized;
    }

    /**
     * Checks if the controller instance has successfully been initialized
     * (connection settings are valid)
     *
     * @return true of the controller has been initialized and calls to the
     * server can be made.
     * @see #initialize()
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Checks the initialized state and throws an exception if the class has not
     * been properly initialized. This informs the caller that requests to the
     * server cannot be made.
     */
    private void checkInitialized()
            throws IllegalStateException {
        if (!initialized) {
            throw new IllegalStateException(UNINITIALIZED_ERROR_MSG);
        }
    }

    /**
     * Register a callback for when the connection to the server has been
     * modified.
     *
     * @param listener listener to add
     */
    public synchronized void addSeverConnectionEventListener(
            final ServerConnectionEventListener listener) {
        serverEventListeners.add(listener);
    }

    /**
     * Remove a registered listener for server connection changes
     *
     * @param listener the listener to remove
     */
    public synchronized void removeServerConnectionEventListener(
            final ServerConnectionEventListener listener) {
        serverEventListeners.remove(listener);
    }
}
