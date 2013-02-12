package org.wiredwidgets.cow.ac.workflowsummary.model;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.wiredwidgets.cow.server.api.service.Group;
import org.wiredwidgets.cow.server.api.service.User;

/**
 * A data model that represents a state of a given process specified by the
 * process id.ext (its "full id") where each group or user is mapped to a
 * {@link CompletionState}, an enumeration which associates a state to a display
 * color.
 * <p/>
 * TODO RYANMILLER 3/16/20112 Important note: This class doesn't contain any
 * methods to set the CompletionStates for individual groups or users. It just
 * creates HashMaps and provides accessors to those Maps. They can then be
 * updated using the reference. This should be refactored into a more object
 * oriented approach if there's ever time.
 *
 * @author MJRUSSELL, documented by RYANMILLER
 * @see org.wiredwidgets.cow.ac.workflowsummary.ui.ColorLegendDialog
 */
public class ProcessStateByGroupAndUser {

    /**
     * Enum used to define completion states and provide a mapping from states
     * to colors to be utilized by ui components.
     */
    public enum CompletionState {

        COMPLETED_OR_APPROVED(Color.GREEN),
        REJECTED_OR_INVALID(Color.RED),
        UNDER_REVIEW(Color.CYAN),
        PARTIAL_COMPLETION(Color.YELLOW),
        AWAITING_NOTIFICATION(Color.WHITE),
        UNDETERMINABLE(Color.LIGHT_GRAY);

        private CompletionState(Color color) {
            this.color = color;
        }

        /**
         * Returns the color associated with the completion state
         *
         * @return The color associated with the completion state
         */
        public Color getCompletionColor() {
            return color;
        }
        private Color color;
    }
    /** id of the process */
    protected String processId;
    /** extension of the process */
    protected String processExt;
    /** completion state of the process as a whole */
    protected CompletionState completionState;
    /** map of groups to completion states */
    protected Map<Group, CompletionState> statesGroupMap;
    /** map of users to completion states */
    protected Map<User, CompletionState> statesUserMap;

    /**
     * Wrapper for the other constructor to allow for easier building of the
     * objects when working with full process ids (which is the id plus its
     * extension).
     *
     * @param processFullId A unique process id in the specified id.ext format.
     * Things will break if not in this format.
     * @param completionState overall completion state of the process
     * @see org.wiredwidgets.cow.server.api.model.v2.Process
     */
    public ProcessStateByGroupAndUser(String processFullId, CompletionState completionState) {
        // TODO: make the parsing of the process id more robust
        this(processFullId.substring(0, processFullId.indexOf('.')),
                processFullId.substring(processFullId.indexOf('.') + 1, processFullId.length()),
                completionState);
    }

    /**
     * Creates the process state mapping for the provided process.
     *
     * @param processId id of the process (typically the workflow name)
     * @param processExt extension that when paired with the id creates a unique
     * id for the process
     * @param completionState overall completion state of the process
     */
    public ProcessStateByGroupAndUser(String processId, String processExt, CompletionState completionState) {
        this.processId = processId;
        this.processExt = processExt;
        this.completionState = completionState;
        statesGroupMap = new HashMap<Group, CompletionState>();
        statesUserMap = new HashMap<User, CompletionState>();
    }

    /**
     * Returns the process's full id in id.ext format
     *
     * @return Process full id
     */
    public String getProcessFullId() {
        return processId + "." + processExt;
    }

    /**
     * Returns the process's extension
     *
     * @return the process's ext
     */
    public String getProcessExt() {
        return processExt;
    }

    /**
     * Returns the process's id
     *
     * @return process id (not the full id)
     */
    public String getProcessId() {
        return processId;
    }

    /**
     * returns overall CompletionState of the process
     *
     * @return overall completion state of the process
     * @see CompletionState
     */
    public CompletionState getCompletionState() {
        return completionState;
    }

    /**
     * Sets the overall completion state of the process
     *
     * @param completionState completion state to set for the whole process
     */
    public void setCompletionState(CompletionState completionState) {
        this.completionState = completionState;
    }

    /**
     * Returns the mapping of groups to completion states
     *
     * @return the map of groups to states
     */
    public Map<Group, CompletionState> getStatesGroupMap() {
        return statesGroupMap;
    }

    /**
     * Returns the mapping of users to completion states
     *
     * @return the map of users to states
     */
    public Map<User, CompletionState> getStatesUserMap() {
        return statesUserMap;
    }
}
