/**
 * Approved for Public Release: 10-4800. Distribution Unlimited.
 * Copyright 2011 The MITRE Corporation,
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

package org.wiredwidgets.cow.server.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.service.HistoryActivity;
import org.wiredwidgets.cow.server.api.service.HistoryTask;
import org.wiredwidgets.cow.server.api.service.Participation;
import org.wiredwidgets.cow.server.api.service.Task;

/**
 *
 * @author JKRANES
 */
public interface TaskService {

    /**
     * Find all tasks for the specified user.  The returned tasks
     * will include both tasks for which the user is the assignee, and tasks
     * for which the user is a candidate.
     * @param assignee
     * @return list of tasks
     */
    List<Task> findPersonalTasks(String assignee);

    List<Task> findAllTasks();

    Task getTask(Long id);

    // HistoryTask getHistoryTask(String id);

    void completeTask(Long id, String assignee, String outcome, Map<String, Object> variables);

    List<Task> findAllUnassignedTasks();

    List<Task> findGroupTasks(String user);

    void takeTask(Long taskId, String userId);

    List<Task> findAllTasksByProcessInstance(Long id);

    List<Task> findAllTasksByProcessKey(Long id);

    /**
     * Create an ad-hoc task, i.e. one not associated with any process
     * @param task the task
     * @return the task Id
     */
    String createAdHocTask(Task task);

    void addTaskParticipatingGroup(Long taskId, String groupId, String type);

    void addTaskParticipatingUser(Long taskId, String userId, String type);

    List<Participation> getTaskParticipations(Long taskId);

    void removeTaskParticipatingGroup(Long taskId, String groupId, String type);

    void removeTaskParticipatingUser(Long taskId, String userId, String type);

    void removeTaskAssignment(Long taskId);

    /**
     * Retrieve historical (completed) tasks by completion date range
     * @param assignee
     * @param startDate
     * @param endDate
     * @return
     */
    List<HistoryTask> getHistoryTasks(String assignee, Date startDate, Date endDate);

    void updateTask(Task task);

    List<HistoryTask> getHistoryTasks(Long processInstanceId);
    
    List<HistoryActivity> getHistoryActivities(Long processInstanceId);

    /**
     * 'Orphaned' tasks include: 
     * (a) Tasks assigned to non existent users, and
     * (b) Tasks attached to candidate-groups where the group(s) have no members.
     * @return list of orphaned tasks
     */
    List<Task> findOrphanedTasks();

    Activity getWorkflowActivity(String processInstanceId, String key);

}
