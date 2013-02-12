package org.wiredwidgets.cow.ac.workflowsummary.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import org.apache.log4j.Logger;
import org.wiredwidgets.cow.ac.util.server.BpmClientController;
import org.wiredwidgets.cow.ac.workflowsummary.model.ProcessStateByGroupAndUser;
import org.wiredwidgets.cow.server.api.model.v2.*;
import org.wiredwidgets.cow.server.api.model.v2.Task;
import org.wiredwidgets.cow.server.api.service.*;

/**
 * Singleton implementation of a "controller" class for process state
 * information. Handles the creation of a ProcessStateByGroupAndUser object when
 * first provided a process id string. This class communicates with the server
 * via {@link BpmClientController} for the basic information. It stores mappings
 * of all the groups and users on the system.
 * <p/>
 * TODO The class "calculates" the completion state for each user's or group's
 * work as part of a particular process using some rather wordy and lengthy
 * algorithms. This logic should be moved to the server to provide consistency
 * across UI mediums, as well as for efficiency. The whole class, after some
 * hefty refactoring, should probably be moved to the server.
 *
 * @author MJRUSSELL
 */
public class ProcessesController {

    static final Logger log = Logger.getLogger(ProcessesController.class);
    private static ProcessesController instance; //singleton implementation
    //Different maps are used for the groups and users to allow for a group and user to have
    //the same name (id)
    private Map<String, Group> groupMapping; //Maps group names to their Group class
    private Map<String, User> userMapping; //Maps user names to their User class

    private ProcessesController() {
        groupMapping = new HashMap<String, Group>();
        userMapping = new HashMap<String, User>();
    }

    /**
     * Provides access to the active instance of the ProcessController. One is
     * created if it does not yet exist.
     *
     * @return a reference to the ProcessController.
     */
    public static synchronized ProcessesController getInstance() {
        if (instance == null) {
            instance = new ProcessesController();
        }
        return instance;
    }

    /**
     * Pulls the groups and users information from the server and updates the
     * locally stored mappings.
     */
    private void refreshGroupAndUserMapping() {
        groupMapping.clear();
        for (Group g : BpmClientController.getInstance().getGroupsOnServer()) {
            groupMapping.put(g.getName(), g);
        }
        userMapping.clear();
        for (User u : BpmClientController.getInstance().getUsersOnServer()) {
            userMapping.put(u.getId(), u);
        }
    }

    /**
     * Recursive helper function which iterates down the activity hierarchy to
     * determine the completion status of a group or user. This function will
     * take the most recent completion status change made to an activity, except
     * for the awaiting notification state which will only be used if there are
     * no other completion states. All recursions end in either a task or an
     * exit. Exits return true if they have been completed so they can be
     * "blamed" on the decision maker who sent the workflow down that path
     *
     * @param activity activity to fill the states for
     * @param procStateByGAndU process status by group and user mapping to
     * states object. This object is recursively passed down and filled with the
     * information as it travels down the hierarchy
     * @return
     */
    private boolean fillStatesRecursively(Activity activity, ProcessStateByGroupAndUser procStateByGAndU) {
        Map<Group, ProcessStateByGroupAndUser.CompletionState> statesGroupMap = procStateByGAndU.getStatesGroupMap();
        Map<User, ProcessStateByGroupAndUser.CompletionState> statesUserMap = procStateByGAndU.getStatesUserMap();

        // The activity will be one of 
        // - more activities
        // - loop
        // - subprocess  <-- don't know what to do with this yet, so ignored
        // - decision  <-- triggers a history lookup to see who made the decision
        // - task
        // - exit
        // anything else is ignored

        if (activity instanceof Activities) {
            //If its an activities then we want to go through each activity and if early exit is still returned
            //then continue to pass it up the hierarchy.
            boolean isExitedEarly = false;
            for (JAXBElement<? extends Activity> el : ((Activities) activity).getActivities()) {
                isExitedEarly = fillStatesRecursively(el.getValue(), procStateByGAndU) || isExitedEarly;
            }
            return isExitedEarly;
        } else if (activity instanceof Loop) {
            Loop l = (Loop) activity;
            fillStatesRecursively(l.getLoopTask(), procStateByGAndU);
            if (l.getLoopTask().getCompletionState().equals("completed")) {
                fillStatesRecursively(l.getActivity().getValue(), procStateByGAndU);
            }
        } else if (activity instanceof SubProcess) {
            SubProcess s = ((SubProcess) activity);

            if (s.getCompletionState().equals("completed")) {
                procStateByGAndU.setCompletionState(ProcessStateByGroupAndUser.CompletionState.COMPLETED_OR_APPROVED);
                return true;
            }
        } else if (activity instanceof Decision) {
            Decision d = (Decision) activity;
            //Determine the state of the decision task
            fillStatesRecursively(d.getTask(), procStateByGAndU);

            if (d.getTask().getCompletionState().equals("completed")) {
                //The decision has been made, need to figure out which one and
                //send the recursion down the chosen path
                HistoryActivities historyActivitesForProcess =
                        BpmClientController.getInstance().getHistoryTasksForProcess(procStateByGAndU.getProcessId(), procStateByGAndU.getProcessExt());
                String outcome = "";
                for (HistoryActivity ha : historyActivitesForProcess.getHistoryActivities()) {
                    if (d.getTask().getKey().equals(ha.getActivityName())) {
                        outcome = ha.getTransitionNames().get(0);
                    }
                }
                for (Option o : d.getOptions()) {
                    if (o.getName().equals(outcome)) {
                        //If an option taken in a decision led to aborting early, then "blame" the decision 
                        //making user/group
                        if (fillStatesRecursively(o.getActivity().getValue(), procStateByGAndU)) {
                            Task t = d.getTask();
                            if (t.getCandidateGroups() == null) {
                                User u = userMapping.get(t.getAssignee());
                                if (u != null) {//handles case where user has been deleted
                                    statesUserMap.put(u, ProcessStateByGroupAndUser.CompletionState.REJECTED_OR_INVALID);
                                }
                            } else {
                                Group g = groupMapping.get(t.getCandidateGroups());
                                if (g != null) {//handles case where group has been deleted
                                    statesGroupMap.put(g, ProcessStateByGroupAndUser.CompletionState.REJECTED_OR_INVALID);
                                }
                            }
                        }
                    }
                }
            }
        } else if (activity instanceof Task) {
            Task t = ((Task) activity);
            if (t.getCandidateGroups() == null) {
                User u = userMapping.get(t.getAssignee());
                if (u != null) {//Handle case where user has been deleted
                    if (t.getCompletionState().equals("completed")) {
                        statesUserMap.put(u, ProcessStateByGroupAndUser.CompletionState.COMPLETED_OR_APPROVED);
                    } else if (t.getCompletionState().equals("open")) {
                        statesUserMap.put(u, ProcessStateByGroupAndUser.CompletionState.UNDER_REVIEW);
                    } else if (t.getCompletionState().equals("not started")) {
                        //If haven't reached this task yet, only add it if otherwise there would be no state
                        if (!statesUserMap.containsKey(u)) {
                            statesUserMap.put(u, ProcessStateByGroupAndUser.CompletionState.AWAITING_NOTIFICATION);
                        }
                    }
                }
            } else {
                Group g = groupMapping.get(t.getCandidateGroups());
                if (g != null) {//Handle case where group has been deleted
                    if (t.getCompletionState().equals("completed")) {
                        statesGroupMap.put(g, ProcessStateByGroupAndUser.CompletionState.COMPLETED_OR_APPROVED);
                    } else if (t.getCompletionState().equals("open")) {
                        statesGroupMap.put(g, ProcessStateByGroupAndUser.CompletionState.UNDER_REVIEW);
                    } else if (t.getCompletionState().equals("not started")) {
                        //If haven't reached this task yet, only add it if otherwise there would be no state or it was undeterminable
                        if (!statesGroupMap.containsKey(g) || statesGroupMap.get(g).equals(ProcessStateByGroupAndUser.CompletionState.UNDETERMINABLE)) {
                            statesGroupMap.put(g, ProcessStateByGroupAndUser.CompletionState.AWAITING_NOTIFICATION);
                        }
                    }
                }
            }
        } else if (activity instanceof Exit) {
            Exit e = ((Exit) activity);
            if (e.getCompletionState().equals("completed")) {
                procStateByGAndU.setCompletionState(ProcessStateByGroupAndUser.CompletionState.REJECTED_OR_INVALID);
                return true;
            }
        }
        return false;
    }

    /**
     * Recursive helper function which iterates down the activity hierarchy to
     * determine the completion status of a group or user. This function will
     * take the most recent completion status change made to an activity, except
     * for the awaiting notification state which will only be used if there are
     * no other completion states. All recursions end in either a task or an
     * exit. Exits return true if they have been completed so they can be
     * "blamed" on the decision maker who sent the workflow down that path
     *
     * @param activity activity to fill the states for
     * @param procStateByGAndU process status by group and user mapping to
     * states object. This object is recursively passed down and filled with the
     * information as it travels down the hierarchy
     * @return
     */
    private boolean fillStates(List<StatusSummary> statusSummaries, ProcessStateByGroupAndUser procStateByGAndU) {
        Map<Group, ProcessStateByGroupAndUser.CompletionState> statesGroupMap = procStateByGAndU.getStatesGroupMap();
        Map<User, ProcessStateByGroupAndUser.CompletionState> statesUserMap = procStateByGAndU.getStatesUserMap();
        
        for (StatusSummary statusSummary : statusSummaries) {
            if (statusSummary.getType().equals("group") && statusSummary.getCount() > 0) {
                Group g = groupMapping.get(statusSummary.getName());
                if (statusSummary.getStatus().equals("open")){
                    statesGroupMap.put(g, ProcessStateByGroupAndUser.CompletionState.UNDER_REVIEW);
                } else if (statusSummary.getStatus().equals("completed") && !statesGroupMap.get(g).equals(ProcessStateByGroupAndUser.CompletionState.UNDER_REVIEW)){
                    statesGroupMap.put(g, ProcessStateByGroupAndUser.CompletionState.COMPLETED_OR_APPROVED);
                } else if (statusSummary.getStatus().equals("planned") && !statesGroupMap.get(g).equals(ProcessStateByGroupAndUser.CompletionState.UNDER_REVIEW) && !statesGroupMap.get(g).equals(ProcessStateByGroupAndUser.CompletionState.COMPLETED_OR_APPROVED)){
                    statesGroupMap.put(g, ProcessStateByGroupAndUser.CompletionState.AWAITING_NOTIFICATION);
                } 
            } else if (statusSummary.getType().equals("user") && statusSummary.getCount() > 0) {
                User u = userMapping.get(statusSummary.getName());
                if (statusSummary.getStatus().equals("open")){
                    statesUserMap.put(u, ProcessStateByGroupAndUser.CompletionState.UNDER_REVIEW);
                } else if (statusSummary.getStatus().equals("completed") && !statesUserMap.get(u).equals(ProcessStateByGroupAndUser.CompletionState.UNDER_REVIEW)){
                    statesUserMap.put(u, ProcessStateByGroupAndUser.CompletionState.COMPLETED_OR_APPROVED);
                } else if (statusSummary.getStatus().equals("planned") && !statesUserMap.get(u).equals(ProcessStateByGroupAndUser.CompletionState.UNDER_REVIEW) && !statesUserMap.get(u).equals(ProcessStateByGroupAndUser.CompletionState.COMPLETED_OR_APPROVED)){
                    statesUserMap.put(u, ProcessStateByGroupAndUser.CompletionState.AWAITING_NOTIFICATION);
                } 
            }
        }

        return false;
    }

    /**
     * Sets all the groups and users found in the process to undeterminable.
     *
     * @param activity
     * @param procStateByGAndU
     */
    private void setInitialGroupAndUserStatesRecurs(Activity activity, ProcessStateByGroupAndUser procStateByGAndU) {
        if (activity instanceof Activities) {
            for (JAXBElement<? extends Activity> el : ((Activities) activity).getActivities()) {
                setInitialGroupAndUserStatesRecurs(el.getValue(), procStateByGAndU);
            }
        } else if (activity instanceof Loop) {
            Loop l = (Loop) activity;
            setInitialGroupAndUserStatesRecurs(l.getLoopTask(), procStateByGAndU);
            setInitialGroupAndUserStatesRecurs(l.getActivity().getValue(), procStateByGAndU);
        } else if (activity instanceof SubProcess) {
            //TODO: How to handle a sub process?
        } else if (activity instanceof Decision) {
            Decision d = ((Decision) activity);
            setInitialGroupAndUserStatesRecurs(d.getTask(), procStateByGAndU);
            for (Option o : d.getOptions()) {
                setInitialGroupAndUserStatesRecurs(o.getActivity().getValue(), procStateByGAndU);
            }
        } else if (activity instanceof Task) {
            Task t = ((Task) activity);
            if (t.getCandidateGroups() == null) {
                User u = userMapping.get(t.getAssignee());
                if (u != null) {
                    procStateByGAndU.getStatesUserMap().put(u, ProcessStateByGroupAndUser.CompletionState.UNDETERMINABLE);
                }
            } else {
                Group g = groupMapping.get(t.getCandidateGroups());
                if (g != null) {
                    procStateByGAndU.getStatesGroupMap().put(g, ProcessStateByGroupAndUser.CompletionState.UNDETERMINABLE);
                }
            }
        }
    }

    /**
     * Sets all the groups and users found in the process to undeterminable.
     *
     * @param activity
     * @param procStateByGAndU
     */
    private void setInitialGroupAndUserStates(List<StatusSummary> statusSummaries, ProcessStateByGroupAndUser procStateByGAndU) {
        for (StatusSummary statusSummary : statusSummaries) {
            if (statusSummary.getType().equals("group")) {
                Group g = groupMapping.get(statusSummary.getName());
                if (g != null) {
                    procStateByGAndU.getStatesGroupMap().put(g, ProcessStateByGroupAndUser.CompletionState.UNDETERMINABLE);
                }
            } else if (statusSummary.getType().equals("user")) {
                User u = userMapping.get(statusSummary.getName());
                if (u != null) {
                    procStateByGAndU.getStatesUserMap().put(u, ProcessStateByGroupAndUser.CompletionState.UNDETERMINABLE);
                }
            }
        }
    }

    /**
     * Function that returns a {@link ProcessStateByGroupAndUser} data object
     * for a given process full id (id.ext) with the process's overall
     * <code>CompletionState</code> set as well as state information for each
     * user and group involved with the process. This could be considered the
     * primary user method of the class.
     * <p/>
     * TODO: explain criteria of how states are determined
     *
     * @param processFullId Process instance id string in the id.ext format
     * @return a ProcessStateByGroupAndUser object with the current states
     * mapping for the process and all users and groups involved in that
     * process. Will return
     * <code>null</code> if nothing is returned from the server
     *
     * @see ProcessStateByGroupAndUser
     */
    public ProcessStateByGroupAndUser getProcessState(String processFullId) {
        //TODO: every time? Can we get a notification for groups and users changing?
        refreshGroupAndUserMapping();


        org.wiredwidgets.cow.server.api.service.ProcessInstance procStatus = BpmClientController.getInstance().getActiveProcessInstanceStatus(processFullId);
        ProcessStateByGroupAndUser procStateByGAndU;

        if (procStatus == null) {
            log.error("A null process status was returned from the server for "
                    + processFullId
                    + ". Either server is down or there is an error with the process.");
            return null;
        }


        if (procStatus.getProcess().getActivity().getValue().getCompletionState().equals("completed")) {
            procStateByGAndU = new ProcessStateByGroupAndUser(processFullId, ProcessStateByGroupAndUser.CompletionState.COMPLETED_OR_APPROVED);
        } else {
            procStateByGAndU = new ProcessStateByGroupAndUser(processFullId, ProcessStateByGroupAndUser.CompletionState.AWAITING_NOTIFICATION);
        }

        setInitialGroupAndUserStates(procStatus.getStatusSummaries(), procStateByGAndU);
        //setInitialGroupAndUserStatesRecurs(procStatus.getActivity().getValue(), procStateByGAndU);
        fillStates(procStatus.getStatusSummaries(), procStateByGAndU);
        //fillStatesRecursively(procStatus.getActivity().getValue(), procStateByGAndU);
        return procStateByGAndU;
    }
}